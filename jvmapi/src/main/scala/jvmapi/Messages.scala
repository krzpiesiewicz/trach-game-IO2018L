package jvmapi

import play.api.libs.json._
import play.api.libs.json.Format._

package messages {

  import jvmapi.models._

  /**
   * Message incoming outside the server via websocket.
   */
  trait MsgFromClient
  
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
  
  case class GameStateRequestMsg(
    msgType: String = "GameStateRequest",
    gamePlayId: Long) extends MsgFromClient with GamePlayMsg
  
  case class GameStateUpdateMsg(
    msgType: String = "GameStateUpdate",
    gamePlayId: Long,
    updateId: Long,
    gameState: GameState) extends GamePlayUpdateMsg

  case class PlayedCardsRequestMsg(
    msgType: String = "PlayedCardsRequest",
    gamePlayId: Long,
    updateId: Long,
    played: CardTreeOrNode) extends MsgFromClient with GamePlayUpdateMsg

  case class NoActionRequestMsg(
    msgType: String = "NoActionRequest",
    gamePlayId: Long,
    updateId: Long,
    playerId: Int) extends MsgFromClient with GamePlayUpdateMsg

  case class GamePlayResultRequestMsg(
      msgType: String = "GamePlayResultRequest",
      gamePlayId: Long) extends MsgFromClient with GamePlayMsg
    
  case class GamePlayResultMsg(
    msgType: String = "GamePlayResult",
    gamePlayId: Long,
    winnerId: Int) extends GamePlayMsg
}

package object messages {

  implicit val gameStateRequestMsgFormat = Json.format[GameStateRequestMsg]
  implicit val gameStateUpdateMsgFormat = Json.format[GameStateUpdateMsg]
  implicit val playedCardRequestMsgFormat = Json.format[PlayedCardsRequestMsg]
  implicit val noActionRequestMsgFormat = Json.format[NoActionRequestMsg]
  implicit val gamePlayResultRequestMsgFormat = Json.format[GamePlayResultRequestMsg]
  implicit val gamePlayResultMsgFormat = Json.format[GamePlayResultMsg]
}
