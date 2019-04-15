package controllers

import com.google.inject._

import play.api._
import play.api.mvc._
import play.api.libs.streams.ActorFlow
import play.api.libs.json.JsValue

import akka.actor.ActorSystem
import akka.stream.Materializer

import actors.ClientActor


@Singleton
class MainController @Inject() (cc: ControllerComponents) (implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    {
      Ok("Welcome to the main page. Here, have the url to the websocket server: " + routes.MainController.socket().webSocketURL(secure = true))
    }
  }
  
  def socket = WebSocket.accept[JsValue, JsValue] { request =>
    ActorFlow.actorRef { out =>
      ClientActor.props(out)
    }
  }
}
