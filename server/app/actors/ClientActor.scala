package actors

import akka.actor._
import akka.event.Logging

import play.api.libs.json._
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.JsSuccess

import jvmapi.messages._
import messages._
import db._

import actors.PlayerDriver._

import actors.GamesManagerActor.{ GameRequest, RequestedGame }
import actors.MultiplayerGameActor.EnterGame

class ClientActor(out: ActorRef, gamesManager: ActorRef, user: User) extends Actor with ActorLogging {

  val userDriver = UserDriver(user, self)
  
  override def preStart() = {
  }
  
  override def postStop() = {
    // when websocket closes, Play stops ClientActor
    //TODO inform game manager etc.
  }

  def receive = defaultReceive

  // receiver states functions:

  private def defaultReceive: Receive = receiveMsgFromClient({
    case _: QuickMultiplayerGameRequestMsg =>
      context.become(waitingForGame(System.currentTimeMillis()))
  })

  private def waitingForGame(requestId: Long): Receive = {

    def sendGameRequest() = gamesManager ! MsgFromUser(user, GameRequest(requestId))

    sendGameRequest()

    // receive:
    receiveMsgFromClient({
      case _: QuickMultiplayerGameRequestMsg =>
        log.debug("QuickMultiplayerGameRequestMsg")
        sendGameRequest()
    })
      .orElse({
        case RequestedGame(`requestId`, game, gamePlayId) =>
          log.debug("RequestedGame")
          context.become(enteringGame(game, gamePlayId))
      })
  }
  
  private def enteringGame(game: ActorRef, gamePlayId: Long): Receive = {

    def sendEnterGame() = game ! MsgFromUser(user, EnterGame)

    sendEnterGame()

    // receive:
    receiveMsgFromClient({
      case _: QuickMultiplayerGameRequestMsg =>
        log.debug("QuickMultiplayerGameRequestMsg --> sendEnterGame")
        sendEnterGame
    })
      .orElse(receiveMsgToUserAndTransmitToClient({
        case msg: GamePlayInfoUpdateMsg => if (msg.gamePlayId == gamePlayId) {
          log.debug("RequestedGame")
          context.become(playing(game, gamePlayId))
        }
      }))
  }

  private def playing(game: ActorRef, gamePlayId: Long): Receive = receiveMsgFromClient({
    // in case that user has not been informed about game play and he demands info
    case _: QuickMultiplayerGameRequestMsg =>
      game ! MsgFromPlayerDriver(userDriver, GamePlayInfoRequestMsg(gamePlayId = gamePlayId))
    // game actor services GamePlayMsg
    case msg: GamePlayMsg => game ! MsgFromPlayerDriver(userDriver, msg)
  })
    .orElse(transmitMsgToUser)

  // helper methods

  private def receiveMsgFromClient(fromClientReceive: PartialFunction[MsgFromClient, Unit]): Receive = {
    case json: JsValue => msgFromClientReads.reads(json) match {
      case JsSuccess(msg: MsgFromClient, _) =>
        log.debug(msg.toString())
        if (fromClientReceive.isDefinedAt(msg))
          fromClientReceive.apply(msg)
//        fromClientReceive.applyOrElse(msg, _: MsgFromClient => {})
      case _ => { /*wrong json message*/ }
    }
  }

  private def receiveMsgToUserAndTransmitToClient(toClientReceive: PartialFunction[MsgToClient, Unit]): Receive = {
    case MsgToUser(`user`, msg: MsgToClient) =>
      if (toClientReceive.isDefinedAt(msg))
        toClientReceive.apply(msg)
      sendToClient(msg)
  }

  private def transmitMsgToUser: Receive = {
    case MsgToUser(`user`, msg: MsgToClient) => sendToClient(msg)
  }
  
  def sendToClient[T](msg: T)(implicit wr: Writes[T]) = out ! Json.toJson(msg)
}

object ClientActor {
  def props(out: ActorRef, gamesManager: ActorRef, user: User) = Props(new ClientActor(out, gamesManager, user))
}
