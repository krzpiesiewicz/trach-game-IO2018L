import org.scalatest.FunSuite
import com.typesafe.scalalogging.Logger

import play.api.libs.json._

import game.Logging.logger

import game.gameplay.modelsapi._

import game.core.PlayedCardAtPlayerRequest
import game.core.PlayedCardInTreeRequest
import game.core.TreeWithCards

class ApiTest extends FunSuite {

  test("GameStateApi") {
    import DafaultData.data1._

    val gameStateApi = GameStateApi(state)
    val gameStateApiJson = Json.toJson(gameStateApi)
    //    logger.info(gameStateApiJson.toString() + "\n")

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

    assert(json1 == gameStateApiJson)
    
    val fromJson1 = Json.fromJson[GameStateApi](json1)
    assert(fromJson1.isSuccess)
    assert(fromJson1.get == gameStateApi)

    // p1 plays his ac at p2
    val (table1, _) = table.attachCard(PlayedCardAtPlayerRequest(ac.id, p1.id, p2.id))

    // p1 increases priority of attack
    val (table2, _) = table1.attachCard(PlayedCardInTreeRequest(pic.id, p1.id, ac.id))

    val gameStateApi2 = GameStateApi(table2)
    val gameStateApiJson2 = Json.toJson(gameStateApi2)
    //    logger.info(gameStateApiJson2.toString() + "\n")

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
|    "playedCard": {"type":"PlayedStartingCardAtPlayer","cardId":1,"whoPlayedId":1,"targetPlayerId":2},
|    "childrenNodes": [
|      {"playedCard":{"type":"PlayedStartingCardInTree","cardId":4,"whoPlayedId":1,"targetCardId":1},"childrenNodes":[]}
|    ]
|  }
|}""".stripMargin)

    assert(json2 == gameStateApiJson2)

    val fromJson2 = Json.fromJson[GameStateApi](json2)

    assert(fromJson2.isSuccess)
    assert(fromJson2.get == gameStateApi2)
  }
}