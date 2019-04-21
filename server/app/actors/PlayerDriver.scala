package actors

import scala.language.implicitConversions
import akka.actor._

import jvmapi.messages._

import messages._
import db._

trait PlayerDriver
case object NoDriver extends PlayerDriver
case class UserDriver(user: User) extends PlayerDriver
case class BotDriver(actorRef: ActorRef) extends PlayerDriver

class PlayersAndDrivers(
    val driversToPlayers: Map[PlayerDriver, Int] = Map.empty,
    val playersToDrivers: Map[Int, PlayerDriver] = Map.empty,
    val driversToRefs: Map[PlayerDriver, ActorRef] = Map.empty) {
  
  def driver(playerId: Int) = playersToDrivers.get(playerId)
  
  def player(driver: PlayerDriver) = driversToPlayers.get(driver)
  
  def actorRef(driver: PlayerDriver) = driversToRefs.get(driver)
  
  def driverAndRef(playerId: Int) = driver(playerId) match {
    case Some(driver) => Some((driver, actorRef(driver).get))
    case None => None
  }
  
  def playersWithNoDriver: Set[Int] = playersToDrivers.filter({case (playerId, driver) => driver == NoDriver}).keySet
  
  def withDriver(driver: PlayerDriver, actorRef: ActorRef, playerId: Int) = {
    if (!playersToDrivers.contains(playerId))
      throw new Exception(s"playerId=$playerId not in playersToDrivers map")
    
    new PlayersAndDrivers(driversToPlayers.updated(driver, playerId), playersToDrivers.updated(playerId, driver))
  }
}

case class MsgFromPlayerDriver(driver: PlayerDriver, msg: Any)

object PlayerDriver {
  implicit def toUserDriver(user: User): PlayerDriver = UserDriver(user)
  implicit def toBotDriver(botRef: ActorRef): PlayerDriver = BotDriver(botRef)
  
  def msgToPlayerDriver(driver: PlayerDriver, msg: Any) = driver match {
    case UserDriver(user) => MsgToUser(user, msg)
    case _: BotDriver => msg
  }
}