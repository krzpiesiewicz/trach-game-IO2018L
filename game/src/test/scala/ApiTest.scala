import org.scalatest.FunSuite

import play.api.libs.json._

import jvmapi.models._
import game.gameplay.modelsconverters._

import game.core.PlayedCardAtPlayer
import game.core.PlayedCardInTree
import game.core.TreeWithCards
import game.core.CardInnerNode
import game.core.Table

import game.standardtrach.actions.{ buildersFactory, PlayingCards }

class ApiTest extends FunSuite {

  test("ModelsConvertersTest") {
    import DafaultData.data1._

    val gameState = toGameStateModel(state)
    val gameStateJson = Json.toJson(gameState)

    val json = Json.parse("""
|{
|  "players": [
|    {"id":1,"name":"","health":5,"hand":[{"id":1,"type":"attack"},{"id":4,"type":"priority_inc"}],"activeCards":[]},
|    {"id":2,"name":"","health":5,"hand":[{"id":3,"type":"defence"},{"id":2,"type":"attack"},{"id":5,"type":"priority_inc"}],"activeCards":[]}
|  ],
|  "coveredCardsStack":[{"id":6,"type":"shelter"}],
|  "usedCardsStack":[],
|  "tableActiveCards":[],
|  "cardTrees": [],
|  "roundId":1,
|  "playerIdOnMove":1
|}""".stripMargin)

    assert(json == gameStateJson)
    
    val fromJson = Json.fromJson[GameState](json)
    assert(fromJson.isSuccess)
    assert(fromJson.get == gameState)

    // p1 plays his ac at p2
    val state1 = new PlayingCards(TreeWithCards(PlayedCardAtPlayer(ac, p1, p2)), p1)(state).state
    val gameState1 = toGameStateModel(state1)
    
    val json1 = Json.parse("""
|{
|  "players": [
|    {"id":1,"name":"","health":5,"hand":[{"id":6,"type":"shelter"}, {"id":4,"type":"priority_inc"}],"activeCards":[]},
|    {"id":2,"name":"","health":5,"hand":[{"id":3,"type":"defence"},{"id":2,"type":"attack"},{"id":5,"type":"priority_inc"}],"activeCards":[]}
|  ],
|  "coveredCardsStack":[],
|  "usedCardsStack":[],
|  "tableActiveCards":[],
|  "cardTrees": [{
|    "id": 1,
|    "playedCard": {"type":"PlayedStartingCardAtPlayer","card":{"id":1,"type":"attack"},"whoPlayedId":1,"targetPlayerId":2},
|    "childrenNodes": []
|  }],
|  "roundId":1,
|  "playerIdOnMove":1
|}""".stripMargin)

    val fromJson1 = Json.fromJson[GameState](json1)
    assert(fromJson1.isSuccess)
    assert(fromJson1.get == gameState1)

    // p1 increases priority of attack
    val state2 = new PlayingCards(CardInnerNode(PlayedCardInTree(pic, p1, ac)), p1)(state1).state

    val gameState2 = toGameStateModel(state2)
    val gameStateJson2 = Json.toJson(gameState2)

    val json2 = Json.parse("""
|{
|  "players": [
|    {"id":1,"name":"","health":5,"hand":[{"id":6,"type":"shelter"}],"activeCards":[]},
|    {"id":2,"name":"","health":5,"hand":[{"id":3,"type":"defence"},{"id":2,"type":"attack"},{"id":5,"type":"priority_inc"}],"activeCards":[]}
|  ],
|  "coveredCardsStack":[],
|  "usedCardsStack":[],
|  "tableActiveCards":[],
|  "cardTrees": [{
|    "id": 1,
|    "playedCard": {"type":"PlayedStartingCardAtPlayer","card":{"id":1,"type":"attack"},"whoPlayedId":1,"targetPlayerId":2},
|    "childrenNodes": [
|      {"playedCard":{"type":"PlayedCardInTree","card":{"id":4,"type":"priority_inc"},"whoPlayedId":1,"parentCardId":1},"childrenNodes":[]}
|    ]
|  }],
|  "roundId":1,
|  "playerIdOnMove":1
|}""".stripMargin)

    assert(json2 == gameStateJson2)

    val fromJson2 = Json.fromJson[GameState](json2)

    assert(fromJson2.isSuccess)
    assert(fromJson2.get == gameState2)
  }
}