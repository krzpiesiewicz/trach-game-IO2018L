package game.core

import scala.language.existentials

trait TreeOfCards

case object EmptyTree extends TreeOfCards

case class TreeWithCards(val rootPlayingCard: PlayedStartingCard[_ <: Card], val children: Seq[CardNode] = Seq.empty) extends TreeOfCards {
  /**
   * TODO implementation of simple attaching a new leaf (without any checks. It means transforming only the tree structure).
   * At the moment the method does nothing.
   */
  def attachPlayedCard(pcit: PlayedCardInTree[_ <: Card]) = this
}

class CardNode(val playingCard: PlayedCardInTree[_ <: Card], val children: Seq[CardNode])

case class Table(val state: GameState, val tree: TreeOfCards) {

  def attachCard(pcr: PlayedCardRequest): (Table, Boolean) = {
    val notAttached = (this, false)
    try {
      val pc = PlayedCard(pcr)(state)
      if (isPlayersCard(pc.player, pc.card))
        tree match {
          case EmptyTree => pc match {
            case psc: PlayedStartingCard[_] => {
              val newState = removeCardFromPlayersHand(pc.player, pc.card, state)
              (Table(newState, TreeWithCards(psc)), true)
            }
            case _ => notAttached
          }
          case treeWithCards: TreeWithCards => pc match {
            case pcit: PlayedCardInTree[_] => {
              //TODO implementation of checking if card can be attached in certaing place
              // Tip: use ActionBuilders and Refiners
              val newState = removeCardFromPlayersHand(pc.player, pc.card, state)
              (Table(newState, treeWithCards.attachPlayedCard(pcit)), true)
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
