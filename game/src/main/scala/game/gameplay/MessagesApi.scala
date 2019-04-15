package game.gameplay

import play.api.libs.json._
import play.api.libs.json.Format._

package object messagesapi {

  import game.gameplay.modelsapi._

  trait GamePlayMsg {
    val gamePlayId: Long
  }
  
  trait GamePlayUpdateMsg extends GamePlayMsg {
    val updateId: Long
  }
  
  case class GameStateRequestMsg(
    msgType: String = "GameStateRequest",
    gamePlayId: Long) extends GamePlayMsg
  
  case class GameStateUpdateMsg(
    msgType: String = "GameStateUpdate",
    gamePlayId: Long,
    updateId: Long,
    gameState: GameStateApi) extends GamePlayUpdateMsg

  case class PlayedCardMsg(
    msgType: String = "PlayedCard",
    gamePlayId: Long,
    updateId: Long,
    played: PlayedCardApi) extends GamePlayUpdateMsg

  case class NoActionMsg(
    msgType: String = "NoAction",
    gamePlayId: Long,
    updateId: Long,
    playerId: Int) extends GamePlayUpdateMsg

  case class GamePlayResultMsg(
    msgType: String = "GamePlayResult",
    gamePlayId: Long,
    winnerId: Int) extends GamePlayMsg

  implicit val gameStateRequestMsgFormat = Json.format[GameStateRequestMsg]
  implicit val gameStateUpdateMsgFormat = Json.format[GameStateUpdateMsg]
  implicit val playedCardMsgFormat = Json.format[PlayedCardMsg]
  implicit val noActionMsgFormat = Json.format[NoActionMsg]
  implicit val gamePlayResultMsgFormat = Json.format[GamePlayResultMsg]
}