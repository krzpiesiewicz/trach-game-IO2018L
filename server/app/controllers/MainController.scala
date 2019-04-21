package controllers

import com.google.inject._

import scala.concurrent.ExecutionContext

import play.api._
import play.api.mvc._
import play.api.libs.streams.ActorFlow
import play.api.libs.json.JsValue

import akka.actor._
import akka.stream.Materializer

import actors.ClientActor
import actors.GamesManagerActor

import db._

@Singleton
class MainController @Inject() (cc: ControllerComponents) (implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext) extends AbstractController(cc) {

  val gamesManager = system.actorOf(GamesManagerActor.props)
  
  def index() = Action { implicit request: Request[AnyContent] =>
    {
      Ok("Welcome to the main page. Here, have the url to the websocket server: " + routes.MainController.socket().webSocketURL(secure = true))
    }
  }
  
  /**
   * TODO Authentication of users
   */
  def socket = WebSocket.accept[JsValue, JsValue] { request =>
    ActorFlow.actorRef { out =>
      val userId = Database.getFreeUserId()
      val user = User(userId, s"Anonymous user $userId")
      ClientActor.props(out, gamesManager, user)
    }
  }
}
