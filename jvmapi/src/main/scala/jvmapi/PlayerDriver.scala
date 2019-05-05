package jvmapi

import scala.language.implicitConversions
import akka.actor._

trait PlayerDriver
trait DefinedPlayerDriver extends PlayerDriver

case object NoDriver extends PlayerDriver

case class BotDriver(actorRef: ActorRef) extends DefinedPlayerDriver

case class MsgFromPlayerDriver(driver: DefinedPlayerDriver, msg: Any)