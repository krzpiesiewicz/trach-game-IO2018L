package actors

import akka.actor._

import messagesapi._

class GamesManagerActor extends Actor {
  
  var gamesMap: Map[Long, ActorRef] = Map.empty
  var waitingUsers: Set[Long] = Set.empty
  
  override def preStart() {
    //TODO load saved games into gamesMap
    context.become(ready)
  }
  
  def receive = uninitialized
  
  def uninitialized: Receive = {
    case _ => sender() ! "unitialized"
  }
  
  def ready: Receive = {
    
    case AuthenticatedUserMsg(userId, msg) => msg match {
      case msg: QuickMultiplayerGameRequestMsg =>
        waitingUsers = waitingUsers + userId
        //TODO check if a game play should start
      case _ => {}
    }
  }
}

object GamesManagerActor {
  def props = Props(new GamesManagerActor)
}