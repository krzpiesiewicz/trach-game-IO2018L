import play.api.libs.json._
import play.api.libs.json.Format._

import messagesapi.JsonUtils._

package object messagesapi {
   
  case class GamePlayInfoRequestMsg(
      msgType: String = "GamePlayInfoRequest",
      gamePlayId: Long)

  object GamePlayState extends Enumeration {
    type GamePlayState = Value
    val RUNNING = Value("running")
    val STOPPED = Value("stopped")
    val FINISHED = Value("finished")
  }

  import GamePlayState.GamePlayState

  case class GamePlayInfoUpdateMsg(
    msgType: String = "GamePlayStateUpdate",
    gamePlayId: Long,
    gamePlayState: GamePlayState)

  case class QuickMultiplayerGameMsg(
    msgType: String = "QuickMultiplayerGame",
    requestId: Long)
    
  implicit val gamePlayInfoRequestMsgFormat = Json.format[GamePlayInfoRequestMsg]
  
  implicit val gamePlayStateRead: Reads[GamePlayState.Value] = EnumUtils.enumReads(GamePlayState)
      
  implicit val gamePlayInfoUpdateMsgFormat = Json.format[GamePlayInfoUpdateMsg]
  implicit val quickMultiplayerGameMsgFormat = Json.format[QuickMultiplayerGameMsg]
}