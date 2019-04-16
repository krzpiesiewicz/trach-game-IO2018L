package actors

import akka.actor._

import messagesapi._

class GamesManagerActor extends Actor {
  
  var gamesMap: Map[Long, ActorRef] = Map.empty
  
  override def preStart() {
    //TODO load saved games into gamesMap
    context.become(ready)
  }
  
  def receive = uninitialized
  
  def uninitialized: Receive = {
    case _ => sender() ! "unitialized"
  }
  
  def ready: Receive = {
    case msg: QuickMultiplayerGameMsg => {
      
    }
  }
}