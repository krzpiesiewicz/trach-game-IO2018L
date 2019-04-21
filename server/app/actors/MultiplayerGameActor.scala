package actors

import scala.language.implicitConversions
import scala.concurrent.ExecutionContext

import akka.actor._
import akka.event.Logging

import jvmapi.messages._

import messages._
import messages.GamePlayState.GamePlayState
import db._

import game.core.GameState

import game.gameplay.GamePlayActor
import bot.BotActor

import MultiplayerGameActor._

import actors.PlayerDriver._
import play.api.libs.json.Json

class MultiplayerGameActor(gamesManager: ActorRef, gamePlayId: Long)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  private var gamePlay: ActorRef = _
  private var playersAndDrivers: PlayersAndDrivers = _
  private var gamePlayState: GamePlayState = _

  override def preStart() {

    val gameState = gamesettings.DefaultGame.multiplayerTemporaryGameState

    gamePlay = context.actorOf(Props(new GamePlayActor(gamePlayId, self)))
    gamePlay ! gameState

    gamePlayState = GamePlayState.RUNNING
    
    log.debug(Json.stringify(Json.toJson(gamePlayState)))

    playersAndDrivers = new PlayersAndDrivers(playersToDrivers = gameState.playersMap.keys.map(playerId => (playerId, NoDriver)).toMap)

    context.become(ready)
    
    log.debug(s"MultiplayerGameActor(gamePlayId=$gamePlayId): I am ready")
  }

  override def postStop() {
    context.stop(gamePlay)
  }

  def receive = uninitialized

  def uninitialized: Receive = {
    case _ => sender ! "unitialized"
  }

  def ready: Receive = {
    case MsgFromPlayerDriver(driver, msg: GamePlayMsg with MsgFromClient) => if (msg.gamePlayId == gamePlayId) {
      log.debug(s"MultiplayerGameActor(gamePlayId=$gamePlayId): MsgFromPlayerDriver($driver, $msg)")
      msg match {
        case _: GamePlayInfoRequestMsg =>
          playersAndDrivers.player(driver) match {
            case Some(playerId) =>
              sender ! msgToPlayerDriver(driver, GamePlayInfoUpdateMsg(
                gamePlayId = gamePlayId,
                playerId = playerId,
                gamePlayState = gamePlayState))
            case None => {}
          }
        case _ =>
          gamePlay ! msg
      }
    }

    case MsgFromUser(user, msg) => msg match {
      case EnterGame =>
        val playerIdOpt = playersAndDrivers.player(user) match {
          case Some(playerId) => Some(playerId)
          case None => playersAndDrivers.playersWithNoDriver.headOption match {
            case None => None
            case Some(playerId) =>
              playersAndDrivers = playersAndDrivers.withDriver(user, sender, playerId)
              log.debug(s"MultiplayerGameActor(gamePlayId=$gamePlayId): user $user entered the game as player of id=$playerId")
              Some(playerId)
          }
        }
        playerIdOpt match {
          case Some(playerId) => self.tell(MsgFromPlayerDriver(user, GamePlayInfoRequestMsg(gamePlayId=gamePlayId)), sender)
          case None => {}
        }
    }
    
    case msg: GamePlayMsg =>
      playersAndDrivers.playersToDrivers.foreach({
        case (playerId, driver) =>
          playersAndDrivers.driversToRefs.get(driver) match {
            case Some(ref) => ref ! msgToPlayerDriver(driver, msg)
            case None => {}
          }
      })
  }
}

object MultiplayerGameActor {
  def props(gamesManager: ActorRef, gamePlayId: Long)(implicit ec: ExecutionContext) =
    Props(new MultiplayerGameActor(gamesManager, gamePlayId))

  case object EnterGame
}