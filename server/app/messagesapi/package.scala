import play.api.libs.json._
import play.api.libs.json.Format._

import game.gameplay.messagesapi._

import messagesapi.JsonUtils._

package object messagesapi {

  case class GamePlayInfoRequestMsg(
    msgType: String = "GamePlayInfoRequest",
    gamePlayId: Long)
    extends MsgFromClient

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

  case class QuickMultiplayerGameRequestMsg(
    msgType: String = "QuickMultiplayerGameRequest",
    requestId: Long) extends MsgFromClient

  implicit val gamePlayInfoRequestMsgFormat = Json.format[GamePlayInfoRequestMsg]

  implicit val gamePlayStateReads: Reads[GamePlayState.Value] = EnumUtils.enumReads(GamePlayState)

  implicit val gamePlayInfoUpdateMsgFormat = Json.format[GamePlayInfoUpdateMsg]
  implicit val quickMultiplayerGameRequestMsgFormat = Json.format[QuickMultiplayerGameRequestMsg]
  
  implicit object msgFromClientReads extends Reads[MsgFromClient] {
    
   def reads(json: JsValue): JsResult[MsgFromClient] = (json \ "msgType").get match {
      case JsString(typeName) => typeName match {
        case "GamePlayInfoRequest" => gamePlayInfoRequestMsgFormat.reads(json)
        case "QuickMultiplayerGameRequest" => quickMultiplayerGameRequestMsgFormat.reads(json)
        case "GameStateRequest" => gameStateRequestMsgFormat.reads(json)
        case "PlayedCardRequest" => playedCardRequestMsgFormat.reads(json)
        case "NoActionRequest" => noActionRequestMsgFormat.reads(json)
        case "GamePlayResultRequest" => gamePlayResultRequestMsgFormat.reads(json)
        case _ => JsError(s"""unknown type "$typeName"""")
      }
      case _ => JsError("""no "type" field""")
    }
  }
  
  case class AuthenticatedUserMsg[T <: MsgFromClient](userId: Long, msg: T)
}