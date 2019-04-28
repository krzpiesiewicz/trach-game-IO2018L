package game.gameplay

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props, Cancellable, ActorContext }
import akka.event.{ Logging, DiagnosticLoggingAdapter }

import game.core.Player.PlayerId
import game.core.{ GameState, Table, CardNode, PlayedCard, Card, Player }
import game.standardtrach.actions.buildersFactory

import jvmapi.models.{ GameState => GameStateApi }
import jvmapi.models.{ PlayedCard => PlayedCardApi }
import jvmapi.models.CardTreeOrNode
import jvmapi.messages._

import game.gameplay.GamePlayActor._
import game.gameplay.modelsconverters._

class GamePlayActor(gamePlayId: Long, server: ActorRef)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  override val log: DiagnosticLoggingAdapter = Logging(this)
  
  override def preStart() = {
    log.mdc(Map("actorSufix" -> s"[gamePlayId=$gamePlayId]"))
    log.debug("I am ready")
  }
  
  private def sendGameStateUpdateMsg(target: ActorRef, updateId: Long, table: Table) =
    target ! GameStateUpdateMsg(gamePlayId = gamePlayId, updateId = updateId, gameState = table)

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
    Table(state),
    updateId,
    true,
    Some(state.roundsManager.currentPlayer.id),
    state.playersMap.keys.toSet,
    NoTimer)

  private def checkForCardRequest(
    table: Table,
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
      sendGameStateUpdateMsg(server, updateId, table)

    {
      case TimeToEvaluate(`updateId`) =>
        val state = table.evaluate.withNextRound
        context.become(checkForStartingCardRequest(state, updateId + 1))

      case msg: GamePlayMsg => if (msg.gamePlayId == gamePlayId) msg match {
        case msg: GamePlayUpdateMsg => if (msg.updateId == updateId) msg match {

          case pcm: PlayedCardsRequestMsg =>
            // verify that all played cards in pcm are owned by one player
            verifyPlayedCardsRequest(pcm)(table.state) match {
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
                  table.attach(cn) match {
                    // if played cards attached successfully create a new update
                    case (newTable, true) =>
                      cancelTimer()
                      log.debug("Attached")
                      context.become(checkForCardRequest(newTable, updateId + 1, true, None, waitingFor, Timer(30.seconds, self, TimeToEvaluate(updateId + 1))))
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
                context.become(checkForCardRequest(table, updateId, false, fromPlayerOpt, newWaitingFor, timer))
            }
        }

        case _: GameStateRequestMsg =>
          sendGameStateUpdateMsg(sender(), updateId, table)
      }
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

      /**
       * Returns all played cards from subtree of @node.
       */
      def getCards(node: CardNode): Seq[PlayedCard[_]] = node.children.foldLeft[Seq[PlayedCard[_]]](Seq(node.playedCard)) { case (seq, child) => seq ++ getCards(child) }
      
      // verify that the player owns all the cards from the tree.
      if (getCards(cardNode).forall(pc => pc.player.owns(pc.card.asInstanceOf[Card])))
        Some(cardNode)
      else
        None
    } else
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

  case class RunningTimer(cancellable: Cancellable) extends Timer

  object Timer {
    def apply(timeout: FiniteDuration, actorRef: ActorRef, msg: Any)(implicit context: ActorContext, ec: ExecutionContext) =
      RunningTimer(context.system.scheduler.scheduleOnce(timeout, actorRef, msg))
  }
}
