package game.gameplay

import scala.language.implicitConversions

import game.standardtrach.Cards

import jvmapi.models._

package object modelsconverters {

  implicit def toCardModel(card: game.core.Card): Card = Card(card.id, Cards.nameOf(card))

  implicit def toPlayerModel(player: game.core.Player): Player = Player(
    player.id,
    "",
    player.health.value,
    player.hand.cards,
    player.activeCards.cards)

  implicit def toPlayedStartingCardModel(pc: game.core.PlayedStartingCard[_]): PlayedStartingCard = pc match {
    case pc: game.core.PlayedCardAtPlayer[_] => toPlayedStartingCardAtPlayerModel(pc)
    case pc: game.core.PlayedCardAtActiveCard[_] => toPlayedStartingCardAtCardModel(pc)
  }

  implicit def toPlayedStartingCardAtPlayerModel(pc: game.core.PlayedCardAtPlayer[_]): PlayedStartingCardAtPlayer = PlayedStartingCardAtPlayer(
    card = pc.card.asInstanceOf[game.core.Card],
    whoPlayedId = pc.player.id,
    targetPlayerId = pc.targetPlayer.id)

  implicit def toPlayedStartingCardAtCardModel(pc: game.core.PlayedCardAtActiveCard[_]): PlayedStartingCardAtCard = PlayedStartingCardAtCard(
    card = pc.card.asInstanceOf[game.core.Card],
    whoPlayedId = pc.player.id,
    targetCardId = pc.activeCard.id)

  implicit def toPlayedCardInTreeModel(pc: game.core.PlayedCardInTree[_]): PlayedCardInTree = PlayedCardInTree(
    card = pc.card.asInstanceOf[game.core.Card],
    whoPlayedId = pc.player.id,
    targetCardId = pc.parentCard.id)

  implicit def cardSeq(cards: Seq[game.core.Card]): Seq[Card] = cards.map(toCardModel(_))

  implicit def toPlayedCardRequest(pc: PlayedCard): game.core.PlayedCardRequest = pc match {
    case pc: PlayedStartingCardAtPlayer => game.core.PlayedCardAtPlayerRequest(pc.card.id, pc.whoPlayedId, pc.targetPlayerId)
    case pc: PlayedStartingCardAtCard => game.core.PlayedCardAtActiveCardRequest(pc.card.id, pc.whoPlayedId, pc.targetCardId)
    case pc: PlayedCardInTree => game.core.PlayedCardInTreeRequest(pc.card.id, pc.whoPlayedId, pc.targetCardId)
  }

  implicit def toCardTreeModel(tree: game.core.TreeWithCards): CardTree = CardTree(
    tree.playedCard,
    tree.children.map(toCardNodeModel(_)))

  implicit def toCardNodeModel(node: game.core.CardInnerNode): CardNode = CardNode(
    node.playedCard,
    node.children.map(toCardNodeModel(_)))
    
    def toGameStateModel(state: game.core.GameState, treeOpt: Option[game.core.TreeOfCards]): GameState = GameState(
    state.playersMap.values.map(toPlayerModel(_)).toSeq,
    state.coveredCardsStack.cards,
    state.discardedCardsStack.cards,
    state.globalActiveCards.cards,
    treeOpt match {
      case Some(tree: game.core.TreeOfCards) => tree match {
        case tree: game.core.TreeWithCards => Some(toCardTreeModel(tree))
        case _ => None
      }
      case _ => None
    })

  implicit def toGameStateModel(state: game.core.GameState): GameState = toGameStateModel(state, None)

  implicit def toGameStateModel(table: game.core.Table): GameState = toGameStateModel(table.state, Some(table.tree))
}
