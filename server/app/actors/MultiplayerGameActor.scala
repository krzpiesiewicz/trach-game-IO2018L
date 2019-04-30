package actors

import scala.language.implicitConversions
import scala.concurrent.ExecutionContext

import akka.actor._
import akka.event.{ Logging, DiagnosticLoggingAdapter }

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

  override val log: DiagnosticLoggingAdapter = Logging(this)
  
  private var gamePlay: ActorRef = _
  private var playersAndDrivers: PlayersAndDrivers = _
  private var usersToDrivers: Map[User, UserDriver] = _
  private var gamePlayState: GamePlayState = _

  override def preStart() {
    
    log.mdc(Map("actorSufix" -> s"[gamePlayId=$gamePlayId]"))

    val gameState = gamesettings.DefaultGame.multiplayerTemporaryGameState

    gamePlay = context.actorOf(Props(new GamePlayActor(gamePlayId, self)), s"GamePlayActor-$gamePlayId")
    gamePlay ! gameState

    gamePlayState = GamePlayState.RUNNING

    playersAndDrivers = new PlayersAndDrivers(playersToDrivers = gameState.playersMap.keys.map(playerId => (playerId, NoDriver)).toMap)
    usersToDrivers = Map.empty
    
    setupBotActors(1)

    context.become(ready)

    log.debug(s"I am ready")
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
      log.debug(s"MsgFromPlayerDriver($driver, $msg)")
      msg match {
        case _: GamePlayInfoRequestMsg =>
          playersAndDrivers.player(driver) match {
            case Some(playerId) => sendGamePlayInfoUpdate(driver, playerId)
            case None => {}
          }
        case _ =>
          gamePlay ! msg
      }
    }

    case MsgFromUser(user, msg) => msg match {
      case EnterGame =>
        val driver = usersToDrivers.get(user) match {
          case Some(driver) =>
            if (driver.actorRef != sender) {
              val newDriver = addNewDriverForUser(user, sender)
              playersAndDrivers = playersAndDrivers.withUpdatedDriver(driver, newDriver)
              newDriver
            } else
              driver
          case None =>
            addNewDriverForUser(user, sender)
        }
        val playerIdOpt = playersAndDrivers.player(driver) match {
          case Some(playerId) => Some(playerId)
          case None => playersAndDrivers.playersWithNoDriver.headOption match {
            case None => None
            case Some(playerId) =>
              playersAndDrivers = playersAndDrivers.withDriver(driver, playerId)
              log.debug(s"user $user entered the game as player of id=$playerId")
              Some(playerId)
          }
        }
        playerIdOpt match {
          case Some(playerId) =>
            sendGamePlayInfoUpdate(driver, playerId)
          case None => {}
        }
    }
    
    case msg: GameStateUpdateMsg =>
      log.debug(s"I got msg from GamePlayActor of type ${msg.msgType}:\n$msg\n")
      
      val playersNames = playersAndDrivers.playersToDrivers.foldLeft[Map[Int, String]](Map.empty) { case (map, (playerId, driver)) =>
        val nameOpt = driver match {
          case UserDriver(user, _) => Some(user.username)
          case _: BotDriver => Some("Server's bot")
          case _ => None
        }
        nameOpt match {
          case Some(name) => map + (playerId -> name)
          case None => map
        }
      }
      
      val msgWithNames = msg.withPlayersNames(playersNames)
      
      playersAndDrivers.playersToDrivers.foreach({
        case (playerId, driver) => sendMsgToPlayerDriver(driver, msgWithNames.presentedToPlayers(Set(playerId)))
      })

    case msg: GamePlayMsg =>
      log.debug(s"I got msg from GamePlayActor of type ${msg.msgType}")

      playersAndDrivers.playersToDrivers.foreach({
        case (playerId, driver) => sendMsgToPlayerDriver(driver, msg)
      })
  }

  private def addNewDriverForUser(user: User, userRef: ActorRef): UserDriver = {
    val newDriver = UserDriver(user, userRef)
    usersToDrivers = usersToDrivers.updated(user, newDriver)
    newDriver
  }

  private def sendGamePlayInfoUpdate(driver: PlayerDriver, playerId: Int) {
    sendMsgToPlayerDriver(driver, GamePlayInfoUpdateMsg(
      gamePlayId = gamePlayId,
      playerId = playerId,
      gamePlayState = gamePlayState))
  }
  
  private def setupBotActors(botsCount: Int) {
    for (i <- 1 to botsCount) {
      playersAndDrivers.playersWithNoDriver.headOption match {
        case None => {}
        case Some(playerId) =>
          val bot = context.actorOf(BotActor.props(gamePlayId, playerId), s"BotActor-g$gamePlayId-p$playerId")
          val driver = BotDriver(bot)
          playersAndDrivers = playersAndDrivers.withDriver(driver, playerId)
          log.debug(s"bot $bot entered the game as player of id=$playerId")
      }
    }
  }
}

object MultiplayerGameActor {
  def props(gamesManager: ActorRef, gamePlayId: Long)(implicit ec: ExecutionContext) =
    Props(new MultiplayerGameActor(gamesManager, gamePlayId))

  case object EnterGame
}