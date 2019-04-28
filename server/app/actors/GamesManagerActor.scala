package actors

import scala.concurrent.ExecutionContext

import akka.actor._
import akka.event.{Logging, DiagnosticLoggingAdapter}

import messages._
import db._

import actors.GamesManagerActor._

class GamesManagerActor()(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  override val log: DiagnosticLoggingAdapter = Logging(this)
  
  var gamesMap: Map[Long, ActorRef] = Map.empty // gamePlayId -> game
  var waitingRequests: Map[(User, Long), ActorRef] = Map.empty // (user, requestId) -> userActor
  var handledRequests: Map[(User, Long), Long] = Map.empty // (user, requestId) -> gamePlayId

  override def preStart() {    
    //TODO load saved games into gamesMap
    context.become(ready)

    log.debug("I am ready")
  }

  def receive = uninitialized

  def uninitialized: Receive = {
    case _ => sender() ! "unitialized"
  }

  def ready: Receive = {

    case MsgFromUser(user, msg) =>
      log.debug(s"received $msg from user $user")
      msg match {
        case GameRequest(requestId) =>
          val key = (user, requestId)
          handledRequests.get(key) match {
            case Some(gamePlayId) =>
              gamesMap.get(gamePlayId) match {
                case Some(game) => sender ! RequestedGame(requestId, game, gamePlayId)
                case None => {}
              }
            case None =>
              if (!waitingRequests.contains(key)) {
                waitingRequests = waitingRequests + (key -> sender)
                log.debug(s"new request added - key: $key")
                if (waitingRequests.size >= 2) {
                  val (chosen, rest) = waitingRequests.splitAt(3)
                  waitingRequests = rest
                  val gamePlayId = Database.getFreeGamePlayId()
                  val game = context.actorOf(Props(new MultiplayerGameActor(self, gamePlayId)), s"MultiplayerGameActor-gamePlayId-$gamePlayId")
                  log.debug(s"game created")
                  chosen.foreach({
                    case ((user, requestId), userActor) =>
                      handledRequests = handledRequests + (key -> gamePlayId)
                      userActor ! RequestedGame(requestId, game, gamePlayId)
                      log.debug(s"requested game (gamePlayId=$gamePlayId) sent to user $user")
                  })
                }
              }
          }
      }
  }
}

object GamesManagerActor {
  def props(implicit ec: ExecutionContext) = Props(new GamesManagerActor)

  case class GameRequest(requestId: Long)

  case class RequestedGame(requestId: Long, game: ActorRef, gamePlayId: Long)
}
