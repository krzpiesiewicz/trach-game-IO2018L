package game.gameplay

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }

import game.core.{ GameState, Table }
import game.standardtrach.actions.buildersFactory

import game.gameplay.modelsapi.GameStateApi
import game.gameplay.modelsapi.PlayedCardApi

import game.gameplay.GamePlayActor._

class GamePlayActor(server: ActorRef)(implicit ec: ExecutionContext) extends Actor {

  def receive = checkForInitialGameState

  def checkForInitialGameState: Receive = {
    case state: GameState =>
      context.become(checkForStartingCardRequest(state))
  }

  def checkForStartingCardRequest(state: GameState) = checkForCardRequest(Table(state), None)

  def checkForCardRequest(table: Table, timeoutOpt: Option[FiniteDuration]): Receive = {
    timeoutOpt match {
      case Some(timeout) => context.system.scheduler.scheduleOnce(timeout, self, TimeToEvaluate)
      case None => {}
    }
    {
      case TimeToEvaluate =>
        val state = table.evaluate
        server ! GameStateApi(state)
        context.become(checkForStartingCardRequest(state))
        
      case pca: PlayedCardApi =>
        table.attachCard(pca) match {
          case (table, _) =>
            server ! GameStateApi(table)
            context.become(checkForCardRequest(table, Some(30.seconds)))
        }
        
      case GameStateRequest =>
        sender() ! table.state
    }
  }
}

object GamePlayActor {
  case object TimeToEvaluate
  case object GameStateRequest
}