package actors

import scala.language.implicitConversions
import akka.actor._

import messages._

import jvmapi._
import jvmapi.messages._

import db._

case class UserDriver(user: User, actorRef: ActorRef) extends DefinedPlayerDriver

class PlayersAndDrivers(
  val driversToPlayers: Map[DefinedPlayerDriver, Int] = Map.empty,
  val playersToDrivers: Map[Int, PlayerDriver] = Map.empty) {

  def driver(playerId: Int): Option[PlayerDriver] = playersToDrivers.get(playerId)

  def player(driver: DefinedPlayerDriver): Option[Int] = driversToPlayers.get(driver)

  def playersWithNoDriver: Set[Int] = playersToDrivers.filter({ case (playerId, driver) => driver == NoDriver }).keySet

  def withUpdatedDriver(oldDriver: DefinedPlayerDriver, newDriver: PlayerDriver): PlayersAndDrivers = driversToPlayers.get(oldDriver) match {
    case Some(playerId) =>
      val newPlayersToDrivers = playersToDrivers.updated(playerId, newDriver)

      newDriver match {
        case newDriver: DefinedPlayerDriver =>
          new PlayersAndDrivers(driversToPlayers - oldDriver + (newDriver -> playerId), newPlayersToDrivers)
        case NoDriver =>
          new PlayersAndDrivers(driversToPlayers - oldDriver, newPlayersToDrivers)
      }

    case None => this
  }

  def withDriver(newDriver: PlayerDriver, playerId: Int): PlayersAndDrivers = playersToDrivers.get(playerId) match {
    case None =>
      throw new Exception(s"playerId=$playerId not in playersToDrivers map")
    case Some(oldDriver) => oldDriver match {
      case oldDriver: DefinedPlayerDriver => withUpdatedDriver(oldDriver, newDriver)
      case NoDriver =>
        newDriver match {
          case driver: DefinedPlayerDriver =>
            new PlayersAndDrivers(driversToPlayers.updated(driver, playerId), playersToDrivers.updated(playerId, driver))
          case NoDriver => this
        }
    }
  }

  /**
   * Applies @f to every pair (playerId, driver) from playersToDrivers map, where the driver is a DefinedDriver.
   */
  def foreachDefined(f: (Int, DefinedPlayerDriver) => Unit): Unit =
    playersToDrivers.foreach({
      case (playerId, driver: DefinedPlayerDriver) => f(playerId, driver)
      case _ => {}
    })
}

object PlayersAndDrivers {
  implicit def toUserDriver(user: User, userRef: ActorRef): DefinedPlayerDriver = UserDriver(user, userRef)
  implicit def toBotDriver(botRef: ActorRef): DefinedPlayerDriver = BotDriver(botRef)

  def sendMsgToPlayerDriver(driver: DefinedPlayerDriver, msg: Any) = driver match {
    case UserDriver(user, userRef) => userRef ! MsgToUser(user, msg)
    case BotDriver(botRef) => botRef ! msg
  }
}