package actors

import akka.actor._

import play.api.libs.json.JsValue
import play.api.libs.json.Json


class ClientActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: JsValue =>
      out ! (Json.parse(s"""{"odp": "fajnie", "msg": ${Json.stringify(msg)}}"""))
  }
  
  override def postStop() = {
    // when websocket closes, Play stops ClientActor
    //TODO inform game manager etc.
  }
}

object ClientActor {
  def props(out: ActorRef) = Props(new ClientActor(out))
}