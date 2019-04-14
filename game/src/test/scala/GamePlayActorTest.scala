import org.scalatest.{ FunSuite, BeforeAndAfterAll }

import akka.actor.{ ActorSystem, Actor, Props }
import akka.testkit.{ TestKit, TestProbe }
import akka.event.Logging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

import com.typesafe.scalalogging.Logger

import play.api.libs.json._

import game.Logging.logger

import game.gameplay.modelsapi._
import game.gameplay.GamePlayActor
import game.gameplay.GamePlayActor.GameStateRequest

import game.core.PlayedCardAtPlayerRequest
import game.core.PlayedCardInTreeRequest
import game.core.TreeWithCards

class GamePlayActorTest extends FunSuite with BeforeAndAfterAll {

  implicit val executionContext = ExecutionContext.Implicits.global

  val system = ActorSystem("GamePlayTestSystem")

  override def afterAll() {
    system.terminate()
  }

  test("simple test") {
    import DafaultData.data1._

    val server = system.actorOf(Props(new ServerActor()))

    val gamePlay = system.actorOf(Props(new GamePlayActor(server)))

    gamePlay.tell(state, server)

    gamePlay.tell(GameStateRequest, server)

    gamePlay.tell(
      PlayedStartingCardAtPlayerApi(
        cardId = ac2.id,
        whoPlayedId = p1.id,
        targetPlayerId = p2.id),
      server)

    // p1 plays his ac at p2
    gamePlay.tell(
      PlayedStartingCardAtPlayerApi(
        cardId = ac.id,
        whoPlayedId = p1.id,
        targetPlayerId = p2.id),
      server)

    // p2 plays his dc to defend before p1's ac
    gamePlay.tell(
      PlayedCardInTreeApi(
        cardId = dc.id,
        whoPlayedId = p2.id,
        targetCardId = ac.id),
      server)

    gamePlay.tell(GameStateRequest, server)
    
    Thread.sleep(2000)
    system.stop(gamePlay)
    system.stop(server)
  }

  class ServerActor extends Actor {
    val log = Logging(context.system, this)

    def receive = {
      case x => log.info(x.toString + "\n")
    }
  }
}