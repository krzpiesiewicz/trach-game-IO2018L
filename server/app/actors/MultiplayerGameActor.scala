package actors

import scala.concurrent.ExecutionContext
import akka.actor._

import game.gameplay.messagesapi._
import game.gameplay.GamePlayActor

import messagesapi._


class MultiplayerGameActor(gamesManager: ActorRef, gamePlayId: Long)(implicit ec: ExecutionContext) extends Actor {
  
  var gamePlay: ActorRef = _
  
  override def preStart() {
    gamePlay = context.actorOf(Props(new GamePlayActor(gamePlayId, self)))
    context.become(ready)
  }
  
  override def postStop() {
    context.stop(gamePlay)
  }
  
  def receive = uninitialized
  
  def uninitialized: Receive = {
    case _ => sender() ! "unitialized"
  }
  
  def ready: Receive = {
    case msg: GamePlayMsg => if (msg.gamePlayId == gamePlayId) {
      
    }
  }
}