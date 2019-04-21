package jvmapi.messages

import play.api.libs.json._
import play.api.libs.json.Format._

import jvmapi.models._

/**
 * Message incoming outside the server via websocket.
 */
trait MsgFromClient

/**
 * Outgoing message to client from server sent via websocket.
 */
trait MsgToClient

/**
 * Message related to the game play of the given id.
 */
trait GamePlayMsg {
  val gamePlayId: Long
}

/**
 * Message related to the update of the given id.
 */
trait GamePlayUpdateMsg extends GamePlayMsg {
  val updateId: Long
}

// messages from client:

case class GameStateRequestMsg(
  msgType: String = "GameStateRequest",
  gamePlayId: Long) extends MsgFromClient with GamePlayMsg

case class PlayedCardsRequestMsg(
  msgType: String = "PlayedCardsRequest",
  gamePlayId: Long,
  updateId: Long,
  playerId: Int,
  played: CardTreeOrNode) extends MsgFromClient with GamePlayUpdateMsg

case class NoActionRequestMsg(
  msgType: String = "NoActionRequest",
  gamePlayId: Long,
  updateId: Long,
  playerId: Int) extends MsgFromClient with GamePlayUpdateMsg

case class GamePlayResultRequestMsg(
  msgType: String = "GamePlayResultRequest",
  gamePlayId: Long) extends MsgFromClient with GamePlayMsg

// messages to client:

case class GameStateUpdateMsg(
  msgType: String = "GameStateUpdate",
  gamePlayId: Long,
  updateId: Long,
  gameState: GameState) extends MsgToClient with GamePlayUpdateMsg

case class GamePlayResultMsg(
  msgType: String = "GamePlayResult",
  gamePlayId: Long,
  winnerId: Int) extends MsgToClient with GamePlayMsg
  
object GameStateRequestMsg {
  implicit val gameStateRequestMsgFormat = Json.format[GameStateRequestMsg]
}
object GameStateUpdateMsg {
  implicit val gameStateUpdateMsgFormat = Json.format[GameStateUpdateMsg]
}

object PlayedCardsRequestMsg {
  implicit val playedCardsRequestMsgFormat = Json.format[PlayedCardsRequestMsg]
}

object NoActionRequestMsg {
  implicit val noActionRequestMsgFormat = Json.format[NoActionRequestMsg]
}
object GamePlayResultRequestMsg {
  implicit val gamePlayResultRequestMsgFormat = Json.format[GamePlayResultRequestMsg]
}

object GamePlayResultMsg {
  implicit val gamePlayResultMsgFormat = Json.format[GamePlayResultMsg]
}


