import org.scalatest.FunSuite

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe }
import scala.concurrent.duration._
import scala.language.postfixOps

import com.typesafe.scalalogging.Logger

import play.api.libs.json._

import game.Logging.logger

import game.gameplay.modelsapi._

import game.core.PlayedCardAtPlayerRequest
import game.core.PlayedCardInTreeRequest
import game.core.TreeWithCards

class GamePlayActorTest extends FunSuite {
  test("simple test") {
    import DafaultData.data1._
  }
}