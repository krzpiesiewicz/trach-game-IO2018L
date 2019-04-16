package game.gameplay

import play.api.libs.json._
import play.api.libs.json.Format._

package object messagesapi {

  import game.gameplay.modelsapi._

  /**
   * Message incoming outside the server via socket.
   */
  trait MsgFromClient
  
  /**
   * Message corresponding to the game play of the given id.
   */
  trait GamePlayMsg {
    val gamePlayId: Long
  }
  
  /**
   * Message corresponding to the update of the given id.
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
    gameState: GameStateApi) extends GamePlayUpdateMsg

  case class PlayedCardRequestMsg(
    msgType: String = "PlayedCardRequest",
    gamePlayId: Long,
    updateId: Long,
    played: PlayedCardApi) extends MsgFromClient with GamePlayUpdateMsg

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

  implicit val gameStateRequestMsgFormat = Json.format[GameStateRequestMsg]
  implicit val gameStateUpdateMsgFormat = Json.format[GameStateUpdateMsg]
  implicit val playedCardRequestMsgFormat = Json.format[PlayedCardRequestMsg]
  implicit val noActionRequestMsgFormat = Json.format[NoActionRequestMsg]
  implicit val gamePlayResultRequestMsgFormat = Json.format[GamePlayResultRequestMsg]
  implicit val gamePlayResultMsgFormat = Json.format[GamePlayResultMsg]
}