package actors

import scala.language.implicitConversions
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import akka.actor._
import akka.event.{ Logging, DiagnosticLoggingAdapter }

import messages._
import messages.GamePlayState.GamePlayState

import jvmapi._
import jvmapi.messages._

import db._

import game.core.GameState

import game.gameplay.GamePlayActor
import bot.BotActor

import MultiplayerGameActor._

import actors.PlayersAndDrivers.sendMsgToPlayerDriver

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
    // handle a message from player driver (user or bot)
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

    // handle a message (from user) that is not related to controlling the player
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

    // handle a message from GamePlayActor with a game state update
    case msg: GameStateUpdateMsg =>
      val playersNames = playersAndDrivers.playersToDrivers.foldLeft[Map[Int, String]](Map.empty) {
        case (map, (playerId, driver)) =>
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

      playersAndDrivers.foreachDefined({
        case (playerId, driver) => sendMsgToPlayerDriver(driver, msgWithNames.presentedToPlayers(Set(playerId)))
      })

    // handle other gameplay messages from GamePlayActor 
    case msg: GamePlayMsg =>
      log.debug(s"I got msg from GamePlayActor of type ${msg.msgType}")

      playersAndDrivers.foreachDefined({
        case (playerId, driver) => sendMsgToPlayerDriver(driver, msg)
      })
  }

  private def addNewDriverForUser(user: User, userRef: ActorRef): UserDriver = {
    val newDriver = UserDriver(user, userRef)
    usersToDrivers = usersToDrivers.updated(user, newDriver)
    newDriver
  }

  private def sendGamePlayInfoUpdate(driver: DefinedPlayerDriver, playerId: Int) {
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
          val bot = context.actorOf(BotActor.props(self, gamePlayId, playerId, 1500.millisecond), s"BotActor-g$gamePlayId-p$playerId")
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
