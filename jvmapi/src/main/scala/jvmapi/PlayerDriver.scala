package jvmapi

import scala.language.implicitConversions
import akka.actor._

trait PlayerDriver
case object NoDriver extends PlayerDriver
case class BotDriver(actorRef: ActorRef) extends PlayerDriver

case class MsgFromPlayerDriver(driver: PlayerDriver, msg: Any)

object PlayerDriver {
  implicit def toBotDriver(botRef: ActorRef): PlayerDriver = BotDriver(botRef)
}