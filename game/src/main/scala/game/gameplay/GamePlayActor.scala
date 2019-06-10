package game.gameplay

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import java.time.ZonedDateTime

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSystem, Cancellable, Props}
import akka.event.{DiagnosticLoggingAdapter, Logging}
import game.core.Player.PlayerId
import game.core._
import game.standardtrach.actions.buildersFactory
import jvmapi.models.{CardTree, CardTreeOrNode, GameState => GameStateApi, PlayedCard => PlayedCardApi}
import jvmapi.messages._
import game.gameplay.GamePlayActor._
import game.gameplay.modelsconverters._

class GamePlayActor(gamePlayId: Long, server: ActorRef)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  override val log: DiagnosticLoggingAdapter = Logging(this)
  
  override def preStart() = {
    log.mdc(Map("actorSufix" -> s"[gamePlayId=$gamePlayId]"))
    log.debug("I am ready")
  }
  
  private def sendGameStateUpdateMsg(target: ActorRef, updateId: Long, state: GameState, timer: Timer) =
    target ! GameStateUpdateMsg(
        gamePlayId = gamePlayId,
        updateId = updateId,
        gameState = state,
        timeOfComingEvaluation = timer match {
          case NoTimer => None
          case RunningTimer(_, time: ZonedDateTime) => Some(time)
        })

  def receive = checkForInitialGameState

  private def checkForInitialGameState: Receive = {
    case state: GameState =>
      context.become(checkForStartingCardRequest(state, 0))
      log.debug("I have got a game state.")
    case _ =>
      log.debug("I am waiting for a game state")
      sender ! WaitingForGameState
  }

  private def checkForStartingCardRequest(state: GameState, updateId: Long) = checkForCardRequest(
    state,
    updateId,
    true,
    Some(state.roundsManager.currentPlayer.id),
    state.playersMap.keys.toSet,
    NoTimer)

  private def checkForCardRequest(
    state: GameState,
    updateId: Long,
    updateToSend: Boolean,
    fromPlayerOpt: Option[PlayerId],
    waitingFor: Set[PlayerId],
    timer: Timer): Receive = {

    def cancelTimer() = timer match {
      case timer: RunningTimer => timer.cancellable.cancel()
      case _ => {}
    }

    if (updateToSend)
      sendGameStateUpdateMsg(server, updateId, state, timer)

    {
      case TimeToEvaluate(`updateId`) =>
        nextRound(Table.evaluate(state), updateId)

      case msg: GamePlayMsg => if (msg.gamePlayId == gamePlayId) msg match {
        case msg: GamePlayUpdateMsg => if (msg.updateId == updateId) msg match {

          case pcm: PlayedCardsRequestMsg =>
            // verify that all played cards in pcm are owned by one player
            verifyPlayedCardsRequest(pcm)(state) match {
              case None =>
                log.debug("Played cards are not owned by one player")
              case Some(cn) => (fromPlayerOpt match {
                // if we expect played cards request only from the certain player,
                // verify that it is his/her request
                case Some(playerId) => if (pcm.playerId == playerId)
                  Some(cn)
                else {
                  log.debug("Played cards are not owned by the player whose id is in the message")
                  None
                }
                case None => Some(cn)
              }) match {
                case None =>
                  log.debug("Not attached becauce init verification")
                // if the request is accepted due to the fact who played it,
                // try to attached played cards to the tree
                case Some(cn) =>
                  Table.attach(cn, state) match {
                    // if played cards attached successfully create a new update
                    case (newState, true) =>
                      cancelTimer()
                      log.debug("Attached")
                      context.become(checkForCardRequest(newState, updateId + 1, true, None, waitingFor, Timer(5.seconds, self, TimeToEvaluate(updateId + 1))))
                    case _ =>
                      log.debug("Not attached")
                  }
              }
            }

          case nam: NoActionRequestMsg =>
            val newWaitingFor = waitingFor - nam.playerId
            if (newWaitingFor != waitingFor) {
              log.debug(s"NoActionRequest registered for updateId=$updateId")
              if (newWaitingFor.isEmpty) {
                cancelTimer()
                self ! TimeToEvaluate(updateId)
              } else
                context.become(checkForCardRequest(state, updateId, false, fromPlayerOpt, newWaitingFor, timer))
            }
            
          case hem: HandExchangeRequestMsg =>
            fromPlayerOpt match {
              // HandExchangeRequest is available only at the beginning of a round and only for the player on move.
              // Verify that it is his/her request.
              case Some(playerId) => if (hem.playerId == playerId) {
                implicit val gameState = state
                verifyHandExchangeRequest(hem) match {
                  case None => {}
                  case Some(handExchange) =>
                    HandExchange.exchange(handExchange) match {
                      case (newState, true) =>
                        log.debug("Exchanged")
                        nextRound(newState, updateId)
                      case _ =>
                        log.debug("Not exchanged")
                    }
                }
              }
              else
                log.debug("HandExchangeRequest is available only of the player on move.")
              case None => log.debug("HandExchangeRequest is available only at the beginning of a round.")
            }
        }

        case _: GameStateRequestMsg =>
          sendGameStateUpdateMsg(sender(), updateId, state, timer)
      }
    }
  }
  
  private def nextRound(state: GameState, updateId: Long): Unit = state.withNextRound match {
    case ns: NormalState =>
      context.become(checkForStartingCardRequest(ns, updateId + 1))
    case es: EndState =>
      context.become(gameEnded(es, updateId + 1))
      /* imitation that server requested game state update and game play result.
       * It causes sending game state update and game play result to the server. */
      self.tell(GameStateRequestMsg(gamePlayId=gamePlayId), server)
  }
  
  private def gameEnded(endState: EndState, updateId: Long): Receive = {
    case msg: GamePlayMsg => if (msg.gamePlayId == gamePlayId) msg match {
      case _: GameStateRequestMsg =>
        sender ! GameStateUpdateMsg(
          gamePlayId = gamePlayId,
          updateId = updateId,
          gameState = endState)
        // send gameplay result also:
        self.tell(GamePlayResultRequestMsg(gamePlayId=gamePlayId), server)
          
      case _: GamePlayResultRequestMsg =>
        val winnerId = endState match {
          case gw: GameWin => gw.winner.id
          case _: NoOneAlive => -1
        }
        sender ! GamePlayResultMsg(
          gamePlayId = gamePlayId,
          winnerId = winnerId)
          
      case _ => {}
    }
  }

  /**
   * Returns Some(@cardNode) if all played cards from @pcm request are played by one player who has all of them in his hand. 
   */
  private def verifyPlayedCardsRequest(pcm: PlayedCardsRequestMsg)(implicit state: GameState): Option[CardNode] = try {

    def verifyWhoPlayed(node: CardTreeOrNode): Boolean = pcm.playerId == node.playedCard.whoPlayedId && node.childrenNodes.forall(verifyWhoPlayed(_))

    // verify that every played card was played by the player from the request message.
    if (verifyWhoPlayed(pcm.played)) {
      // convert jvmapi.models.CardNode to game.core.CardNode
      val cardNode: CardNode = pcm.played

      pcm.played match { // if played tree is rooted, then check if its id equals to -1
        case tree: CardTree => if (tree.id != -1) throw new Exception("Played CardTree should have id = -1")
        case _ => {}
      }

      /**
       * Returns all played cards from subtree of @node.
       */
      def getCards(node: CardNode): Seq[PlayedCard[_]] = node.children.foldLeft[Seq[PlayedCard[_]]](Seq(node.playedCard)) { case (seq, child) => seq ++ getCards(child) }
      
      // verify that the player owns all the cards from the tree.
      if (!getCards(cardNode).forall(pc => pc.player.owns(pc.card.asInstanceOf[Card])))
        throw new Exception("The player does not own some card from the played tree")

      Some(cardNode)
    } else
      None
  } catch {
    case e: Exception => None
  }
  
  private def verifyHandExchangeRequest(hem: HandExchangeRequestMsg)(implicit state: GameState): Option[HandExchange] = try {
    val handExchange: HandExchange = hem
    // verify that all of cards to exchange are owned by player.
    if (handExchange.cards.forall(handExchange.player.owns(_)))
      Some(handExchange)
    else
      None
  } catch {
    case e: Exception => None
  }
}

object GamePlayActor {
  
  case object WaitingForGameState
  
  case class TimeToEvaluate(updateId: Long)

  trait Timer

  case object NoTimer extends Timer

  case class RunningTimer(cancellable: Cancellable, time: ZonedDateTime) extends Timer

  object Timer {
    def apply(timeout: FiniteDuration, actorRef: ActorRef, msg: Any)(implicit context: ActorContext, ec: ExecutionContext) =
      RunningTimer(context.system.scheduler.scheduleOnce(timeout, actorRef, msg), ZonedDateTime.now.plusNanos(timeout.toNanos))
  }
}
