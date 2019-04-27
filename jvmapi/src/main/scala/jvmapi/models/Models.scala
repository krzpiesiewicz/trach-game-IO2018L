package jvmapi.models

import play.api.libs.json._
import play.api.libs.json.Format._
import play.api.libs.json.Writes._
import play.api.libs.json.Reads._

case class Card(id: Int, `type`: String) {
  
  def covered = Card(-1, "covered_card")
}

case class Player(id: Int, name: String, health: Int, hand: Seq[Card], activeCards: Seq[Card]) {
  
  def withName(name: String) = Player(id, name, health, hand, activeCards)
  
  def withCoveredHand = Player(id, name, health, hand.map(_.covered), activeCards)
}

case class GameState(
  players: Seq[Player],
  coveredCardsStack: Seq[Card] = Seq.empty,
  usedCardsStack: Seq[Card] = Seq.empty,
  tableActiveCards: Seq[Card] = Seq.empty,
  cardTree: Option[CardTree] = None,
  roundId: Int,
  playerIdOnMove: Int) {
  
  def withPlayersNames(names: Map[Int, String]) = GameState(
      players.map(p => names.get(p.id) match {
        case Some(name) => p.withName(name)
        case None => p
      }),
      coveredCardsStack,
      usedCardsStack,
      tableActiveCards,
      cardTree,
      roundId,
      playerIdOnMove)
  
  /**
   * Returns the game state with covered hands of all palyers except those with ids from @playersIds.
   */
  def presentedToPlayers(playersIds: Set[Int]) = GameState(
      players.map(p => if (playersIds.contains(p.id)) p else p.withCoveredHand),
      coveredCardsStack.map(_.covered),
      usedCardsStack,
      tableActiveCards,
      cardTree,
      roundId,
      playerIdOnMove)
}

trait CardTreeOrNode {
  val playedCard: PlayedCard
  val childrenNodes: Seq[CardNode]
}

case class CardTree(playedCard: PlayedStartingCard, childrenNodes: Seq[CardNode] = Seq.empty) extends CardTreeOrNode

case class CardNode(playedCard: PlayedCardInTree, childrenNodes: Seq[CardNode] = Seq.empty) extends CardTreeOrNode

trait PlayedCard {
  val `type`: String
  val card: Card
  val whoPlayedId: Int
}

trait PlayedStartingCard extends PlayedCard

case class PlayedStartingCardAtPlayer(
  `type`: String = "PlayedStartingCardAtPlayer",
  card: Card,
  whoPlayedId: Int,
  targetPlayerId: Int) extends PlayedStartingCard

case class PlayedStartingCardAtCard(
  `type`: String = "PlayedStartingCardAtCard",
  card: Card,
  whoPlayedId: Int,
  targetCardId: Int) extends PlayedStartingCard

case class PlayedCardInTree(
  `type`: String = "PlayedCardInTree",
  card: Card,
  whoPlayedId: Int,
  parentCardId: Int) extends PlayedCard

object Card {
  implicit val cardFormat = Json.format[Card]
}

object Player {
  implicit val playerFormat = Json.format[Player]
}

object PlayedStartingCardAtPlayer {
  implicit val playedStartingCardAtPlayerFormat = Json.format[PlayedStartingCardAtPlayer]
}
 
object PlayedStartingCardAtCard {
  implicit val playedStartingCardAtCardFormat = Json.format[PlayedStartingCardAtCard]
}

object PlayedCardInTree {
  implicit val playedCardInTreeFormat = Json.format[PlayedCardInTree]
}

object PlayedCard {
  import PlayedStartingCardAtPlayer._
  import PlayedStartingCardAtCard._
  import PlayedCardInTree._
  
  implicit object playedCardFormat extends Format[PlayedCard] {
    def writes(psc: PlayedCard) = psc match {
      case pca: PlayedStartingCardAtPlayer => playedStartingCardAtPlayerFormat.writes(pca)
      case pca: PlayedStartingCardAtCard => playedStartingCardAtCardFormat.writes(pca)
      case pca: PlayedCardInTree => playedCardInTreeFormat.writes(pca)
    }

    def reads(json: JsValue): JsResult[PlayedCard] = (json \ "type").get match {
      case JsString(typeName) => typeName match {
        case "PlayedStartingCardAtPlayer" => playedStartingCardAtPlayerFormat.reads(json)
        case "PlayedStartingCardAtCard" => playedStartingCardAtCardFormat.reads(json)
        case "PlayedStartingCardInTree" => playedCardInTreeFormat.reads(json)
        case _ => JsError(s"""unknown type "$typeName"""")
      }
      case _ => JsError("""no "type" field""")
    }
  }
}

object PlayedStartingCard {
  import PlayedCard._
  
  implicit object playedStartingCardFormat extends Format[PlayedStartingCard] {
    def writes(psc: PlayedStartingCard) = playedCardFormat.writes(psc)

    def reads(json: JsValue): JsResult[PlayedStartingCard] = playedCardFormat.reads(json) match {
      case jsSuc: JsSuccess[PlayedCard] => jsSuc.value match {
        case psc: PlayedStartingCard => JsSuccess(psc)
        case _ => JsError("""it is not starting card""")
      }
      case jsErr: JsError => jsErr
    }
  }
}

object CardNode {
  implicit val cardNodeFormat = Json.format[CardNode]
}

object CardTree {
  implicit val cardTreeFormat = Json.format[CardTree]
}

object GameState {
  implicit val gameStateFormat = Json.format[GameState]
}

object CardTreeOrNode {
  def apply(playedCard: PlayedCard, childrenNodes: Seq[CardNode]): CardTreeOrNode = playedCard match {
    case psc: PlayedStartingCard => CardTree(psc, childrenNodes)
    case pcit: PlayedCardInTree => CardNode(pcit, childrenNodes)
  }

  def unapply(node: CardTreeOrNode): Option[(PlayedCard, Seq[CardNode])] = Some((node.playedCard, node.childrenNodes))
  
  implicit val cardTreeOrNodeFormat = Json.format[CardTreeOrNode]
}
