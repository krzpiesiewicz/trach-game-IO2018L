import play.api.libs.json._
import play.api.libs.json.Format._

import jvmapi.messages._
import jvmapi.jsonutils._

import db._
import jvmapi.messages.GamePlayMsg

package object messages {
  
  case class MsgFromUser(user: User, msg: Any)
  case class MsgToUser(user: User, msg: Any)
  
  //============================================================================================>

  object GamePlayState extends Enumeration {
    type GamePlayState = Value
    val RUNNING = Value("running")
    val STOPPED = Value("stopped")
    val FINISHED = Value("finished")
  }

  import GamePlayState.GamePlayState
  
  // messages from client:
  
  case class GamePlayInfoRequestMsg(
    msgType: String = "GamePlayInfoRequest",
    gamePlayId: Long)
    extends MsgFromClient with GamePlayMsg
    
  case class QuickMultiplayerGameRequestMsg(
    msgType: String = "QuickMultiplayerGameRequest")
    extends MsgFromClient

  // messages to client:
    
  case class GamePlayInfoUpdateMsg(
    msgType: String = "GamePlayInfoUpdate",
    gamePlayId: Long,
    playerId: Int,
    gamePlayState: GamePlayState)
    extends MsgToClient with GamePlayMsg

  // json utils:

  implicit val gamePlayInfoRequestMsgFormat = Json.format[GamePlayInfoRequestMsg]

  implicit val gamePlayStateReads: Reads[GamePlayState.Value] = EnumUtils.enumReads(GamePlayState)

  implicit val gamePlayInfoUpdateMsgFormat = Json.format[GamePlayInfoUpdateMsg]
  implicit val quickMultiplayerGameRequestMsgFormat = Json.format[QuickMultiplayerGameRequestMsg]
  
  implicit object msgFromClientReads extends Reads[MsgFromClient] {
    
    import jvmapi.messages.GameStateRequestMsg._
    import jvmapi.messages.PlayedCardsRequestMsg._
    import jvmapi.messages.NoActionRequestMsg._
    import jvmapi.messages.GamePlayResultRequestMsg._
    
   def reads(json: JsValue): JsResult[MsgFromClient] = (json \ "msgType").get match {
      case JsString(typeName) => typeName match {
        case "GamePlayInfoRequest" => gamePlayInfoRequestMsgFormat.reads(json)
        case "QuickMultiplayerGameRequest" => quickMultiplayerGameRequestMsgFormat.reads(json)
        case "GameStateRequest" => gameStateRequestMsgFormat.reads(json)
        case "PlayedCardsRequest" => playedCardsRequestMsgFormat.reads(json)
        case "NoActionRequest" => noActionRequestMsgFormat.reads(json)
        case "GamePlayResultRequest" => gamePlayResultRequestMsgFormat.reads(json)
        case _ => JsError(s"""unknown type "$typeName"""")
      }
      case _ => JsError("""no "type" field""")
    }
  }
  
  implicit object msgToClientWrites extends Writes[MsgToClient] {
    
    import jvmapi.messages.GameStateUpdateMsg._
    import jvmapi.messages.GamePlayResultMsg._

    override def writes(msg: MsgToClient): JsValue = msg match {
      case msg: GamePlayInfoUpdateMsg => gamePlayInfoUpdateMsgFormat.writes(msg)
      case msg: GameStateUpdateMsg => gameStateUpdateMsgFormat.writes(msg)
      case msg: GamePlayResultMsg => gamePlayResultMsgFormat.writes(msg)
    }
  }
}