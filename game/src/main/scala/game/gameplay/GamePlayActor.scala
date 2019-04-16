package game.gameplay

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.event.Logging

import game.core.{ GameState, Table }
import game.standardtrach.actions.buildersFactory

import game.gameplay.modelsapi.GameStateApi
import game.gameplay.modelsapi.PlayedCardApi
import game.gameplay.messagesapi._

import game.gameplay.GamePlayActor._
import game.core.Player.PlayerId
import akka.actor.Cancellable
import akka.actor.ActorContext

class GamePlayActor(gamePlayId: Long, server: ActorRef)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  def sendGameStateUpdateMsg(target: ActorRef, updateId: Long, table: Table) =
    target ! GameStateUpdateMsg(gamePlayId = gamePlayId, updateId = updateId, gameState = table)

  def receive = checkForInitialGameState

  def checkForInitialGameState: Receive = {
    case state: GameState =>
      context.become(checkForStartingCardRequest(state, 0))
  }

  def checkForStartingCardRequest(state: GameState, updateId: Long) = checkForCardRequest(
      Table(state),
      updateId,
      true,
      Some(state.roundsManager.currentPlayer.id),
      state.playersMap.keys.toSet,
      NoTimer)

  def checkForCardRequest(
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
        val state = table.evaluate
        context.become(checkForStartingCardRequest(state, updateId + 1))

      case msg: GamePlayMsg => if (msg.gamePlayId == gamePlayId) msg match {
        case msg: GamePlayUpdateMsg => if (msg.updateId == updateId) msg match {

          case pcm: PlayedCardRequestMsg =>
            val pca = pcm.played
            val pcaOpt = fromPlayerOpt match {
              case Some(playerId) => if (pca.whoPlayedId == playerId) Some(pca) else None
              case None => Some(pca)
            }
            pcaOpt match {
              case Some(pca) =>
                table.attachCard(pca) match {
                  case (table, attached) => if (attached) {
                    cancelTimer()
                    context.become(checkForCardRequest(table, updateId + 1, true, None, waitingFor, Timer(30.seconds, self, TimeToEvaluate(updateId + 1))))
                  }
                }
              case None => {}
            }
            
          case nam: NoActionRequestMsg =>
            val newWaitingFor = waitingFor - nam.playerId
            if (newWaitingFor != waitingFor)
              if (newWaitingFor.isEmpty) {
                  cancelTimer()
                  self ! TimeToEvaluate(updateId)
              } else
                context.become(checkForCardRequest(table, updateId, false, fromPlayerOpt, newWaitingFor, timer))
        }

        case _: GameStateRequestMsg =>
          sendGameStateUpdateMsg(sender(), updateId, table)
      }
    }
  }
}

object GamePlayActor {
  case class TimeToEvaluate(updateId: Long)
  
  trait Timer
  
  case object NoTimer extends Timer
  
  case class RunningTimer(cancellable: Cancellable) extends Timer
  
  object Timer{
    def apply(timeout: FiniteDuration, actorRef: ActorRef, msg: Any)(implicit context: ActorContext, ec: ExecutionContext) =
      RunningTimer(context.system.scheduler.scheduleOnce(timeout, actorRef, msg))
  }
}
