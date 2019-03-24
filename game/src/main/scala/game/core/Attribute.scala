package game.core

import Player.PlayerId
import game.core.Card.CardId

trait Attribute

trait GlobalAttribute extends Attribute

trait PlayerAttribute extends Attribute

trait Health extends PlayerAttribute {
  def value: Int
  def maxValue: Int
  
  /**
   * Returns a health of player that got a damage of value @damageValue.
   * Health should implement policy of getting damages.
   */
  def getDamage(damageValue: Int): Health
}

trait Hand extends PlayerAttribute {
  def maxCards: Int
  def cards: Seq[Card]
  
  /**
   * Returns the hand where @oldCard is replaced by @newCard.
   */
  def replacedCard(oldCard: Card, newCard: Card): Hand
}

trait PlayerActiveCards extends PlayerAttribute {
  def cards: Seq[Card] 
}

trait TargetChooser extends PlayerAttribute {
  def playersForTargets(circleOfPlayers: CircleOfPlayers): Map[PlayerId, Player]
}

trait CoveredCardsStack extends GlobalAttribute {
  def cards: Seq[Card]
  
  /**
   * Pops a covered card from the stack.
   * Returns the popped card and the stack without the card.
   */
  def pop: (Card, CoveredCardsStack)
}

trait DiscardedCardsStack extends GlobalAttribute {
  def cards: Seq[Card]
  
  /**
   * Pushes a @card onto the stack.
   * Returns the stack with the pushed @card.
   */
  def push(card: Card): DiscardedCardsStack
}

trait GlobalActiveCards extends GlobalAttribute {
  def cards: Seq[Card] 
}

trait AllCards extends GlobalAttribute {
  def cardsMap: Map[CardId, Card]
}

trait Players extends GlobalAttribute {
  def circleOfPlayers: CircleOfPlayers
}

trait RoundsManager extends GlobalAttribute {
  def currentPlayer: Player
  def nextPlayer(circleOfPlayers: CircleOfPlayers): Player
}

trait AttributeTransformer[A <: Attribute] {
  def transform(attribute: A): A
}
