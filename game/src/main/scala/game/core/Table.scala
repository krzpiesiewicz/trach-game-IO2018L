package game.core

import scala.language.existentials
import game.core.actions._

trait TreeOfCards

trait CardNode {
  type N <: CardNode

  val children: Seq[CardInnerNode]
  val playedCard: PlayedCard[_ <: Card]

  def withChildren(newChildren: Seq[CardInnerNode]): N
}

case object EmptyTree extends TreeOfCards

case class TreeWithCards(val playedCard: PlayedStartingCard[_ <: Card], val action: Action, val children: Seq[CardInnerNode] = Seq.empty) extends TreeOfCards with CardNode {
  type N = TreeWithCards
  def withChildren(newChildren: Seq[CardInnerNode]) = TreeWithCards(playedCard, action, newChildren)

  def attachPlayedCard(pcit: PlayedCardInTree[_ <: Card], transformer: ActionTransformer): TreeWithCards = {

    def mapTree[CN <: CardNode](node: CN): node.N = {
      if (node.playedCard.card == pcit.parentCard)
        node.withChildren(children :+ new CardInnerNode(pcit, transformer))
      else
        node.withChildren(node.children map { case child => mapTree(child) })
    }

    mapTree(this)
  }
}

case class CardInnerNode(val playedCard: PlayedCardInTree[_ <: Card], val transformer: ActionTransformer, val children: Seq[CardInnerNode] = Seq.empty) extends CardNode {
  type N = CardInnerNode

  def withChildren(newChildren: Seq[CardInnerNode]) = CardInnerNode(playedCard, transformer, newChildren)
}

case class Table(val state: GameState, val tree: TreeOfCards)(implicit actionFactory: ActionFactory) {

  def attachCard(pcr: PlayedCardRequest): (Table, Boolean) = {
    val notAttached = (this, false)
    try {
      val pc = PlayedCard(pcr)(state)
      if (isPlayersCard(pc.player, pc.card))
        tree match {
          case EmptyTree => pc match {
            case psc: PlayedStartingCard[_] => {
              actionFactory.createAction(psc)(state) match {
                case Some(action) => {
                  val newState = removeCardFromPlayersHand(psc.player, psc.card, state)
                  (Table(newState, TreeWithCards(psc, action)), true)
                }
                case None => notAttached
              }
            }
            case _ => notAttached
          }
          case treeWithCards: TreeWithCards => pc match {
            case pcit: PlayedCardInTree[_] => {
              actionFactory.createTransformer(pcit)(state) match {
                case Some(transformer) => {
                  val newState = removeCardFromPlayersHand(pc.player, pc.card, state)
                  (Table(newState, treeWithCards.attachPlayedCard(pcit, transformer)), true)
                }
                case None => notAttached
              }
            }
            case _ => notAttached
          }
        }
      else
        notAttached
    } catch {
      case e: Exception => notAttached
    }
  }

  /**
   * TODO
   */
  def evaluate: GameState = {

    def evalTree(node: CardInnerNode): ActionTransformer = {
      node.children.map(evalTree _).foldLeft(node.transformer)((parentTransformer: ActionTransformer, childTransformer: ActionTransformer) =>
        childTransformer.transform(parentTransformer) match {
          case Some(transformer) => transformer
          case None => parentTransformer
        })
    }

    tree match {
      case EmptyTree => state
      case tree: TreeWithCards => {
        val action = tree.children.map(evalTree _).foldLeft(tree.action)((action: Action, transformer: ActionTransformer) =>
          transformer.transform(action) match {
            case Some(action) => action
            case None => action
          })
          action.state
      }
    }
  }

  private def isPlayersCard(player: Player, card: Card): Boolean = player.hand.cards contains card

  /**
   * TODO implementation of state transformation by removing player's card.
   * It should remove the card from player's hand and give the player a new one (next one from the CoveredCardsStack).
   * Tip: implement Action that has AttributeBuilder which modifies global attribute CoveredCardsStack
   * and inner builder to modify player's attribute Hand.
   * At the moment the method does nothing.
   */
  private def removeCardFromPlayersHand(player: Player, card: Card, state: GameState): GameState = state
}
