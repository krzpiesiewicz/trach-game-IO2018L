import org.scalatest.FunSuite
import com.typesafe.scalalogging.Logger

import play.api.libs.json._

import game.Logging.logger

import jvmapi.models._
import game.gameplay.modelsconverters._

import game.core.PlayedCardAtPlayerRequest
import game.core.PlayedCardInTreeRequest
import game.core.TreeWithCards

class ApiTest extends FunSuite {

  test("ModelsConvertersTest") {
    import DafaultData.data1._

    val gameState = toGameStateModel(state)
    val gameStateJson = Json.toJson(gameState)
    //    logger.info(gameStateJson.toString() + "\n")

    val json1 = Json.parse("""
|{
|  "players": [
|    {"id":1,"name":"","health":5,"hand":[{"id":1,"type":"attack"},{"id":4,"type":"priority_inc"}],"activeCards":[]},
|    {"id":2,"name":"","health":5,"hand":[{"id":3,"type":"defence"},{"id":2,"type":"attack"},{"id":5,"type":"priority_inc"}],"activeCards":[]}
|  ],
|  "coveredCardsStack":[],
|  "usedCardsStack":[],
|  "tableActiveCards":[]
|}""".stripMargin)

    assert(json1 == gameStateJson)
    
    val fromJson1 = Json.fromJson[GameState](json1)
    assert(fromJson1.isSuccess)
    assert(fromJson1.get == gameState)

    // p1 plays his ac at p2
    val (table1, _) = table.attachCard(PlayedCardAtPlayerRequest(ac.id, p1.id, p2.id))

    // p1 increases priority of attack
    val (table2, _) = table1.attachCard(PlayedCardInTreeRequest(pic.id, p1.id, ac.id))

    val gameState2 = toGameStateModel(table2)
    val gameStateJson2 = Json.toJson(gameState2)
    //    logger.info(gameStateJson2.toString() + "\n")

    val json2 = Json.parse("""
|{
|  "players": [
|    {"id":1,"name":"","health":5,"hand":[],"activeCards":[]},
|    {"id":2,"name":"","health":5,"hand":[{"id":3,"type":"defence"},{"id":2,"type":"attack"},{"id":5,"type":"priority_inc"}],"activeCards":[]}
|  ],
|  "coveredCardsStack":[],
|  "usedCardsStack":[],
|  "tableActiveCards":[],
|  "cardTree": {
|    "playedCard": {"type":"PlayedStartingCardAtPlayer","card":{"id":1,"type":"attack"},"whoPlayedId":1,"targetPlayerId":2},
|    "childrenNodes": [
|      {"playedCard":{"type":"PlayedStartingCardInTree","card":{"id":4,"type":"priority_inc"},"whoPlayedId":1,"targetCardId":1},"childrenNodes":[]}
|    ]
|  }
|}""".stripMargin)

    assert(json2 == gameStateJson2)

    val fromJson2 = Json.fromJson[GameState](json2)

    assert(fromJson2.isSuccess)
    assert(fromJson2.get == gameState2)
  }
}