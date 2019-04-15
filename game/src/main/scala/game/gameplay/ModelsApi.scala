package game.gameplay

import scala.language.implicitConversions

import play.api.libs.json._
import play.api.libs.json.Format._
import play.api.libs.json.Writes._
import play.api.libs.json.Reads._

import game.core._
import game.standardtrach.Cards

package object modelsapi {

  case class CardApi(id: Int, `type`: String)

  case class PlayerApi(id: Int, name: String, health: Int, hand: Seq[CardApi], activeCards: Seq[CardApi])

  case class GameStateApi(
    players: Seq[PlayerApi],
    coveredCardsStack: Seq[CardApi],
    usedCardsStack: Seq[CardApi],
    tableActiveCards: Seq[CardApi],
    cardTree: Option[CardTreeApi])

  case class CardTreeApi(playedCard: PlayedStartingCardApi, childrenNodes: Seq[CardNodeApi])

  case class CardNodeApi(playedCard: PlayedCardInTreeApi, childrenNodes: Seq[CardNodeApi])

  trait PlayedCardApi {
    val `type`: String
    val cardId: Int
    val whoPlayedId: Int
  }

  trait PlayedStartingCardApi extends PlayedCardApi

  case class PlayedStartingCardAtPlayerApi(
    `type`: String = "PlayedStartingCardAtPlayer",
    cardId: Int,
    whoPlayedId: Int,
    targetPlayerId: Int) extends PlayedStartingCardApi

  case class PlayedStartingCardAtCardApi(
    `type`: String = "PlayedStartingCardAtCard",
    cardId: Int,
    whoPlayedId: Int,
    targetCardId: Int) extends PlayedStartingCardApi

  case class PlayedCardInTreeApi(
    `type`: String = "PlayedStartingCardInTree",
    cardId: Int,
    whoPlayedId: Int,
    targetCardId: Int) extends PlayedCardApi

  object CardApi {
    implicit def apply(card: Card): CardApi = CardApi(card.id, Cards.nameOf(card))
  }

  object PlayerApi {
    implicit def apply(player: Player): PlayerApi = PlayerApi(
      player.id,
      "",
      player.health.value,
      player.hand.cards,
      player.activeCards.cards)
  }

  object GameStateApi {
    def apply(state: GameState, treeOpt: Option[TreeOfCards]): GameStateApi = GameStateApi(
      state.playersMap.values.map(PlayerApi(_)).toSeq,
      state.coveredCardsStack.cards,
      state.discardedCardsStack.cards,
      state.globalActiveCards.cards,
      treeOpt match {
        case Some(tree: TreeOfCards) => tree match {
          case tree: TreeWithCards => Some(CardTreeApi(tree))
          case _ => None
        }
        case _ => None
      })

    implicit def apply(state: GameState): GameStateApi = GameStateApi(state, None)

    implicit def apply(table: Table): GameStateApi = GameStateApi(table.state, Some(table.tree))
  }

  object CardTreeApi {
    implicit def apply(tree: TreeWithCards): CardTreeApi = CardTreeApi(
      tree.playedCard,
      tree.children.map(CardNodeApi(_)))
  }

  object CardNodeApi {
    implicit def apply(node: CardInnerNode): CardNodeApi = CardNodeApi(
      node.playedCard,
      node.children.map(CardNodeApi(_)))
  }

  object PlayedStartingCardApi {
    implicit def apply[C <: Card](pc: PlayedStartingCard[C]): PlayedStartingCardApi = pc match {
      case pc: PlayedCardAtPlayer[C] => PlayedStartingCardAtPlayerApi(pc)
      case pc: PlayedCardAtActiveCard[C] => PlayedStartingCardAtCardApi(pc)
    }
  }

  object PlayedStartingCardAtPlayerApi {
    implicit def apply[C <: Card](pc: PlayedCardAtPlayer[C]): PlayedStartingCardAtPlayerApi = PlayedStartingCardAtPlayerApi(
      cardId = pc.card.id,
      whoPlayedId = pc.player.id,
      targetPlayerId = pc.targetPlayer.id)
  }

  object PlayedStartingCardAtCardApi {
    implicit def apply[C <: Card](pc: PlayedCardAtActiveCard[C]): PlayedStartingCardAtCardApi = PlayedStartingCardAtCardApi(
      cardId = pc.card.id,
      whoPlayedId = pc.player.id,
      targetCardId = pc.activeCard.id)
  }

  object PlayedCardInTreeApi {
    implicit def apply[C <: Card](pc: PlayedCardInTree[C]): PlayedCardInTreeApi = PlayedCardInTreeApi(
      cardId = pc.card.id,
      whoPlayedId = pc.player.id,
      targetCardId = pc.parentCard.id)
  }

  implicit def cardApiSeq(cards: Seq[Card]): Seq[CardApi] = cards.map(CardApi(_))
  
  implicit def toPlayedCardRequest(pca: PlayedCardApi): PlayedCardRequest = pca match {
    case pca: PlayedStartingCardAtPlayerApi => PlayedCardAtPlayerRequest(pca.cardId, pca.whoPlayedId, pca.targetPlayerId)
    case pca: PlayedStartingCardAtCardApi => PlayedCardAtActiveCardRequest(pca.cardId, pca.whoPlayedId, pca.targetCardId)
    case pca: PlayedCardInTreeApi => PlayedCardInTreeRequest(pca.cardId, pca.whoPlayedId, pca.targetCardId)
  }
  
  implicit val cardApiFormat = Json.format[CardApi]
  implicit val playerApiFormat = Json.format[PlayerApi]
  implicit val playedStartingCardAtPlayerApiFormat = Json.format[PlayedStartingCardAtPlayerApi]
  implicit val playedStartingCardAtCardApiFormat = Json.format[PlayedStartingCardAtCardApi]
  implicit val playedCardInTreeApiFormat = Json.format[PlayedCardInTreeApi]
   
  implicit object playedCardApiFormat extends Format[PlayedCardApi] {
    def writes(psc: PlayedCardApi) = psc match {
      case pca: PlayedStartingCardAtPlayerApi => playedStartingCardAtPlayerApiFormat.writes(pca)
      case pca: PlayedStartingCardAtCardApi => playedStartingCardAtCardApiFormat.writes(pca)
      case pca: PlayedCardInTreeApi => playedCardInTreeApiFormat.writes(pca)
    }
    
    def reads(json: JsValue): JsResult[PlayedCardApi] = (json \ "type").get match {
      case JsString(typeName) => typeName match {
        case "PlayedStartingCardAtPlayer" => playedStartingCardAtPlayerApiFormat.reads(json)
        case "PlayedStartingCardAtCard" => playedStartingCardAtCardApiFormat.reads(json)
        case "PlayedStartingCardInTree" => playedCardInTreeApiFormat.reads(json)
        case _ => JsError(s"""unknown type "$typeName"""")
      }
      case _ => JsError("""no "type" field""")
    }
  }
  
  implicit object playedStartingCardApiFormat extends Format[PlayedStartingCardApi] {
    def writes(psc: PlayedStartingCardApi) = playedCardApiFormat.writes(psc)
    
    def reads(json: JsValue): JsResult[PlayedStartingCardApi] = playedCardApiFormat.reads(json) match {
      case jsSuc: JsSuccess[PlayedCardApi] => jsSuc.value match {
        case psc: PlayedStartingCardApi => JsSuccess(psc)
        case _ => JsError("""it is not starting card""")
      }
      case jsErr: JsError => jsErr
    }
  }
  
  implicit val cardNodeApiFormat = Json.format[CardNodeApi] 
  implicit val cardTreeApiFormat = Json.format[CardTreeApi]
  implicit val gameStateApiFormat = Json.format[GameStateApi]
}