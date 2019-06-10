package game.gameplay

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import java.time.ZonedDateTime

import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ActorSystem, Cancellable, Props}
import akka.event.{DiagnosticLoggingAdapter, Logging}
import game.core.Player.PlayerId
import game.core._
import game.standardtrach.actions.{ buildersFactory, AllCardTreesEvaluation, NextRound, HandExchange, PlayingCards }
import jvmapi.models.{CardTree, CardTreeOrNode, GameState => GameStateApi, PlayedCard => PlayedCardApi}
import jvmapi.messages._
import game.gameplay.GamePlayActor._
import game.gameplay.modelsconverters._
import game.standardtrach.actions.PlayingCards
import game.standardtrach.actions.HandExchange

class GamePlayActor(gamePlayId: Long, server: ActorRef)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  override val log: DiagnosticLoggingAdapter = Logging(this)
  
  override def preStart() = {
    log.mdc(Map("actorSufix" -> s"[gamePlayId=$gamePlayId]"))
    log.debug("I am ready")
  }

  def receive = checkForInitialGameState

  private def checkForInitialGameState: Receive = {
    case state: GameState =>
      context.become(checkForStartingActionRequest(state, 0))
      log.debug("I have got a game state.")
    case _ =>
      log.debug("I am waiting for a game state")
      sender ! WaitingForGameState
  }

  private def checkForStartingActionRequest(state: GameState, updateId: Long) = checkForCardRequest(
    state,
    updateId,
    true,
    state.playersMap.keys.toSet,
    NoTimer)

  private def checkForCardRequest(
    state: GameState,
    updateId: Long,
    updateToSend: Boolean,
    waitingFor: Set[PlayerId],
    timer: Timer): Receive = {

    def cancelTimer() = timer match {
      case timer: RunningTimer => timer.cancellable.cancel()
      case _ => {}
    }

    if (updateToSend)
      sendGameStateUpdateMsg(server, updateId, state, timer)

    // Proper receive partial function
    {
      case TimeToEvaluate(`updateId`) =>
        val newState = NextRound(AllCardTreesEvaluation(state).state).state
        nextRound(newState, updateId)

      case msg: GamePlayMsg => if (msg.gamePlayId == gamePlayId) msg match {
        case msg: GamePlayUpdateMsg => if (msg.updateId == updateId) msg match {

          case pcm: PlayedCardsRequestMsg =>
            try {
              pcm.played match { // if played tree is rooted, then check if its id equals to -1
                case tree: CardTree =>
                  if (tree.id != -1) throw new Exception("played CardTree should have id = -1")
                case _ => {}
              }

              implicit val initialState = state
              val newState = new PlayingCards(pcm.played, state.player(pcm.playerId)).state
              cancelTimer()
              log.debug(s"PlayedCardsRequest: Attached played cards by player ${pcm.playerId}")
              context.become(checkForCardRequest(newState, updateId + 1, true, waitingFor, Timer(5.seconds, self, TimeToEvaluate(updateId + 1))))
            } catch {
              case e: Exception => log.debug(s"PlayedCardsRequest: ${e.getMessage}")
            }

          case nam: NoActionRequestMsg =>
            val newWaitingFor = waitingFor - nam.playerId
            if (newWaitingFor != waitingFor) {
              log.debug(s"NoActionRequest registered for updateId=$updateId")
              if (newWaitingFor.isEmpty) {
                cancelTimer()
                self ! TimeToEvaluate(updateId)
              } else
                context.become(checkForCardRequest(state, updateId, false, newWaitingFor, timer))
            }
            
          case hem: HandExchangeRequestMsg =>
            try {
              implicit val initialState = state
              val newState = new HandExchange(state.player(hem.playerId), hem.cardsIdsToExchange.map(state.card(_))).state
              log.debug("HandExchangeRequest: cards exchanged")
              nextRound(newState, updateId)
            } catch {
              case e: Exception => log.debug(s"HandExchangeRequest: ${e.getMessage}")
            }
          }

        case _: GameStateRequestMsg =>
          sendGameStateUpdateMsg(sender(), updateId, state, timer)
      }
    }
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
  
  private def sendGameStateUpdateMsg(target: ActorRef, updateId: Long, state: GameState, timer: Timer): Unit =
    target ! GameStateUpdateMsg(
        gamePlayId = gamePlayId,
        updateId = updateId,
        gameState = state,
        timeOfComingEvaluation = timer match {
          case NoTimer => None
          case RunningTimer(_, time: ZonedDateTime) => Some(time)
        })
  
  private def nextRound(state: GameState, updateId: Long): Unit = state.withNextRound match {
    case ns: NormalState =>
      context.become(checkForStartingActionRequest(ns, updateId + 1))
    case es: EndState =>
      context.become(gameEnded(es, updateId + 1))
      /* imitation that server requested game state update and game play result.
       * It causes sending game state update and game play result to the server. */
      self.tell(GameStateRequestMsg(gamePlayId=gamePlayId), server)
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
