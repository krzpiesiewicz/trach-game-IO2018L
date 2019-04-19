package game.gameplay

import scala.language.implicitConversions

import game.standardtrach.Cards
import game.core.actions.BuildersFactory

import jvmapi.models._


package object modelsconverters {

  // converters game objects to jvmapi models:
  
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
    parentCardId = pc.parentCard.id)

  implicit def cardSeq(cards: Seq[game.core.Card]): Seq[Card] = cards.map(toCardModel(_))

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
  
  // convertes jvmapi models to game objects:
  
  /**
   * It checks if a request is correct according to a game state. It means checking if all ids can be mapped for existing objects
   * (throws an exception if something does not exist in game state).
   * It does not check if the player owns the card, nor other similar constraints!
   */
  implicit def toPlayedCard(pc: PlayedCard)(implicit state: game.core.GameState): game.core.PlayedCard[_ <: game.core.Card] = pc match {
    case pc: PlayedStartingCardAtPlayer => toPlayedStartingCardAtPlayer(pc)
    case pc: PlayedStartingCardAtCard => toPlayedStartingCardAtCard(pc)
    case pc: PlayedCardInTree => toPlayedCardInTree(pc)
  }
  
  implicit def toPlayedStartingCard(pc: PlayedStartingCard)(implicit state: game.core.GameState): game.core.PlayedStartingCard[_ <: game.core.Card] =
    toPlayedCard(pc).asInstanceOf[game.core.PlayedStartingCard[_ <: game.core.Card]]

  implicit def toPlayedStartingCardAtPlayer(pc: PlayedStartingCardAtPlayer)(implicit state: game.core.GameState) = new game.core.PlayedCardAtPlayer(
    state.card(pc.card.id),
    state.player(pc.whoPlayedId),
    state.player(pc.targetPlayerId))

  implicit def toPlayedStartingCardAtCard(pc: PlayedStartingCardAtCard)(implicit state: game.core.GameState) = new game.core.PlayedCardAtActiveCard(
    state.card(pc.card.id),
    state.player(pc.whoPlayedId),
    state.card(pc.targetCardId))

  implicit def toPlayedCardInTree(pc: PlayedCardInTree)(implicit state: game.core.GameState) = new game.core.PlayedCardInTree(
    state.card(pc.card.id),
    state.player(pc.whoPlayedId),
    state.card(pc.parentCardId))
  
  implicit def toTreeWithCards(tree: CardTree)(implicit state: game.core.GameState, buildersFactory: BuildersFactory): game.core.TreeWithCards =
    tree.children.map(toCardInnerNode(_)).foldLeft(game.core.TreeWithCards(tree.playedCard)) { case (root, node) => root.attach(node)}
    
  implicit def toCardInnerNode(cardNode: CardNode)(implicit state: game.core.GameState, buildersFactory: BuildersFactory): game.core.CardInnerNode =
    cardNode.children.map(toCardInnerNode(_)).foldLeft(game.core.CardInnerNode(cardNode.playedCard)) { case (root, node) => root.attach(node)}
    
  implicit def toCardNode(treeOrNode: CardTreeOrNode)(implicit state: game.core.GameState, buildersFactory: BuildersFactory): game.core.CardNode = treeOrNode match {
    case tree: CardTree => toTreeWithCards(tree)
    case node: CardNode => toCardInnerNode(node)
  }
}
