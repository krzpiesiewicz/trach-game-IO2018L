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
import game.gameplay.messagesapi._
import game.gameplay.GamePlayActor

import game.core.PlayedCardAtPlayerRequest
import game.core.PlayedCardInTreeRequest
import game.core.TreeWithCards
import com.typesafe.config.ConfigFactory

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
      PlayedCardRequestMsg(
        gamePlayId = gamePlayId,
        updateId = updateId0,
        played = PlayedStartingCardAtPlayerApi(
          cardId = ac2.id,
          whoPlayedId = p1.id,
          targetPlayerId = p2.id)),
      probe.ref)

    // p1 plays his ac at p2
    gamePlay.tell(
      PlayedCardRequestMsg(
        gamePlayId = gamePlayId,
        updateId = updateId0,
        played = PlayedStartingCardAtPlayerApi(
          cardId = ac.id,
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
      PlayedCardRequestMsg(
        gamePlayId = gamePlayId,
        updateId = updateId1,
        played = PlayedCardInTreeApi(
          cardId = dc.id,
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