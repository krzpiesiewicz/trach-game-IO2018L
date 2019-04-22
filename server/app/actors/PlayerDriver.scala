package actors

import scala.language.implicitConversions
import akka.actor._

import jvmapi.messages._

import messages._
import db._

trait PlayerDriver
trait DefinedPlayerDriver extends PlayerDriver

case object NoDriver extends PlayerDriver
case class UserDriver(user: User, actorRef: ActorRef) extends DefinedPlayerDriver
case class BotDriver(actorRef: ActorRef) extends DefinedPlayerDriver

class PlayersAndDrivers(
    val driversToPlayers: Map[PlayerDriver, Int] = Map.empty,
    val playersToDrivers: Map[Int, PlayerDriver] = Map.empty) {
  
  def driver(playerId: Int) = playersToDrivers.get(playerId)
  
  def player(driver: PlayerDriver) = driversToPlayers.get(driver)
  
  def playersWithNoDriver: Set[Int] = playersToDrivers.filter({case (playerId, driver) => driver == NoDriver}).keySet
  
  def withUpdatedDriver(oldDriver: PlayerDriver, newDriver: PlayerDriver) = driversToPlayers.get(oldDriver) match {
    case Some(playerId) =>
      new PlayersAndDrivers(driversToPlayers - oldDriver + (newDriver -> playerId), playersToDrivers.updated(playerId, newDriver))
    case None => this
  }
  
  def withDriver(driver: PlayerDriver, playerId: Int) = playersToDrivers.get(playerId) match {
    case None =>
      throw new Exception(s"playerId=$playerId not in playersToDrivers map")
    case Some(oldDriver) =>
      if (oldDriver == NoDriver)
        new PlayersAndDrivers(driversToPlayers.updated(driver, playerId), playersToDrivers.updated(playerId, driver))
      else
        withUpdatedDriver(oldDriver, driver)
  }
}

case class MsgFromPlayerDriver(driver: PlayerDriver, msg: Any)

object PlayerDriver {
  implicit def toUserDriver(user: User, userRef: ActorRef): PlayerDriver = UserDriver(user, userRef)
  implicit def toBotDriver(botRef: ActorRef): PlayerDriver = BotDriver(botRef)
  
  def sendMsgToPlayerDriver(driver: PlayerDriver, msg: Any) = driver match {
    case UserDriver(user, userRef) => userRef ! MsgToUser(user, msg)
    case BotDriver(botRef) => botRef ! msg
    case NoDriver => {}
  }
}