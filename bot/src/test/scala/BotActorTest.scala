import org.scalatest.{ FunSuite, BeforeAndAfterAll }

import akka.actor.{ ActorSystem, Actor, Props }
import akka.testkit.{ TestKit, TestProbe }
import akka.event.Logging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

import com.typesafe.config.ConfigFactory

import play.api.libs.json._

import jvmapi._
import jvmapi.models._
import jvmapi.messages._

import bot.BotActor

class BotActorTest extends FunSuite with BeforeAndAfterAll {

  implicit val executionContext = ExecutionContext.Implicits.global

  val config = ConfigFactory.load("test.conf")
  implicit val system = ActorSystem("BotActorSystem", config.getConfig("test"))

  override def afterAll() {
    system.terminate()
  }

  test("example test") {

    val gamePlayId = 42
    val probe = TestProbe()
    val bot = system.actorOf(BotActor.props(probe.ref, gamePlayId, 1, 0.milliseconds))

    val p1 = Player(1, "player A", 5, Seq(Card(1, "attack"), Card(2, "priority_inc")), Seq.empty)
		val p2 = Player(2, "player A", 5, Seq(Card(3, "defence")), Seq.empty)

		val state = GameState(Seq(p1, p2), Seq.empty, Seq.empty, Seq.empty, Vector.empty, 1, 1)

		val msg = GameStateUpdateMsg(gamePlayId = gamePlayId, updateId = 1, gameState = state)
    
		bot.tell(msg, probe.ref)
		
		val reply = probe.expectMsgPF(1.second) {
      case MsgFromPlayerDriver(botref, msg: PlayedCardsRequestMsg)  => msg
      case _ => throw new Exception("Wrong message")
    }
		
    system.stop(bot)
    system.stop(probe.ref)
  }
}