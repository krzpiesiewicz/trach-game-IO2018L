package actors

import akka.actor._

import play.api.libs.json.JsValue
import play.api.libs.json.Json

import game.gameplay.messagesapi.MsgFromClient
import messagesapi._
import play.api.libs.json.JsSuccess

class ClientActor(out: ActorRef, gamesManager: ActorRef) extends Actor {

  def receive = {
    case json: JsValue => msgFromClientReads.reads(json) match {
      case jsSuc: JsSuccess[MsgFromClient] =>
        val msg = jsSuc.value
        out ! Json.parse(s"""{"to do klienta": "tak"}""")
        //TODO check msg and send user authenticated msg
      case _ => {}
    }
  }

  override def postStop() = {
    // when websocket closes, Play stops ClientActor
    //TODO inform game manager etc.
  }
}

object ClientActor {
  def props(out: ActorRef, gamesManager: ActorRef) = Props(new ClientActor(out, gamesManager))
}