import org.scalatest.{ FunSuite, BeforeAndAfterAll }

import akka.actor.{ ActorSystem, Actor, Props }
import akka.testkit.{ TestKit, TestProbe }
import akka.event.Logging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

import com.typesafe.scalalogging.Logger
import com.typesafe.config.ConfigFactory

import play.api.libs.json._

import game.Logging.logger

import jvmapi.models._
import jvmapi.messages._

import game.gameplay.GamePlayActor
import game.gameplay.modelsconverters._

import game.core.PlayedCardAtPlayerRequest
import game.core.PlayedCardInTreeRequest
import game.core.TreeWithCards

class GamePlayActorTest extends FunSuite with BeforeAndAfterAll {

  implicit val executionContext = ExecutionContext.Implicits.global

  val config = ConfigFactory.load("test.conf")
  implicit val system = ActorSystem("GamePlayTestSystem", config.getConfig("test"))

  override def afterAll() {
    system.terminate()
  }

  test("simple test") {
    import DafaultData.data1._

    val gamePlayId = 42

    val probe = TestProbe()

    val gamePlay = system.actorOf(Props(new GamePlayActor(gamePlayId, probe.ref)))

    gamePlay.tell(state, probe.ref)

    val updateId0 = probe.expectMsgPF(1.second) {
      case msg: GameStateUpdateMsg => {
        msg.updateId
      }
      case msg => {
        logger.error(msg.toString)
        throw new Exception("I want GameStateUpdateMsg first")
      }
    }

    // p2 cannot play his card because it is not his turn
    gamePlay.tell(
      PlayedCardsRequestMsg(
        gamePlayId = gamePlayId,
        updateId = updateId0,
        played = PlayedStartingCardAtPlayer(
          card = ac2,
          whoPlayedId = p1.id,
          targetPlayerId = p2.id)),
      probe.ref)

    // p1 plays his ac at p2
    gamePlay.tell(
      PlayedCardsRequestMsg(
        gamePlayId = gamePlayId,
        updateId = updateId0,
        played = PlayedStartingCardAtPlayer(
          card = ac,
          whoPlayedId = p1.id,
          targetPlayerId = p2.id)),
      probe.ref)

    val updateId1 = probe.expectMsgPF(5.second) {
      case msg: GameStateUpdateMsg => {
        msg.updateId
      }
      case msg => {
        logger.error(msg.toString)
        throw new Exception("I want GameStateUpdateMsg first")
      }
    }
    assert(updateId1 == updateId0 + 1)
    
    // p2 plays his dc to defend before p1's ac
    gamePlay.tell(
      PlayedCardsRequestMsg(
        gamePlayId = gamePlayId,
        updateId = updateId1,
        played = PlayedCardInTree(
          card = dc,
          whoPlayedId = p2.id,
          targetCardId = ac.id)),
      probe.ref)
      
    val updateId2 = probe.expectMsgPF(1.second) {
      case msg: GameStateUpdateMsg => {
        msg.updateId
      }
      case msg => {
        logger.error(msg.toString)
        throw new Exception("I want GameStateUpdateMsg first")
      }
    }
    assert(updateId2 == updateId1 + 1)
    
    // p1 do not want to do anything in updateId2
    gamePlay.tell(
      NoActionRequestMsg(
        gamePlayId = gamePlayId,
        updateId = updateId2,
        playerId = p1.id),
      probe.ref)
      
    // p2 do not want to do anything
    gamePlay.tell(
      NoActionRequestMsg(
        gamePlayId = gamePlayId,
        updateId = updateId2,
        playerId = p2.id),
      probe.ref)
      
    // Now GamePlayActor should finish the round
      
    val updateId3 = probe.expectMsgPF(1.second) {
      case msg: GameStateUpdateMsg => {
        // At the beginning of a new round, table's tree of cards should be empty
        assert(msg.gameState.cardTree.isEmpty)
        msg.updateId
      }
      case msg => {
        logger.error(msg.toString)
        throw new Exception("I want GameStateUpdateMsg first")
      }
    }
    assert(updateId3 == updateId2 + 1)
    
    system.stop(gamePlay)
    system.stop(probe.ref)
  }
}