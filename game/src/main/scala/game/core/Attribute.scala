package game.core

import Player.PlayerId
import game.core.Card.CardId

/**
 * Attributes are abstractions which describes the state of game. Almost every element in game should be an attribute
 * or be included in some attribute.
 * There are two main kinds of attributes:
 *  @PlayerAttribute - describing a player. Every @Player has a set of attributes (@AttributesSet[PlayerAttribute]).
 *  @GlobalAttribute - describing the whole game (not connected with the certain player).
 *  @GameState has a set of attributes (@AttributesSet[GlobalAttribute]).
 * 
 */
trait Attribute

/**
 * Attribute describing the whole game (not connected with the certain player).
 * @GameState has a set of attributes (@AttributesSet[GlobalAttribute]).
 */
trait GlobalAttribute extends Attribute

/**
 * Attribute describing a player. Every @Player has a set of attributes (@AttributesSet[PlayerAttribute]).
 */
trait PlayerAttribute extends Attribute

trait Health extends PlayerAttribute {
  def value: Int
  def maxValue: Int
  
  /**
   * Returns a health of player that got a damage of value @damageValue.
   * Health should implement policy of getting damages.
   */
  def getDamage(damageValue: Int): Health
  
  /**
   * Returns a health of player with newValue equal old @value + @change. Of course, @newValue should be in [0, maxValue].
   * The change is absolute. It should be modified by any health policy.
   */
  def changedHP(change: Int): Health
}

trait Hand extends PlayerAttribute {
  def maxCards: Int
  def cards: Seq[Card]
  
  /**
   * Returns the hand where @oldCard is replaced by @newCard when @newCardOpt is Some(@newCard) otherwise it returns the hand without @oldCard.
   */
  def replacedCard(oldCard: Card, newCardOpt: Option[Card]): Hand
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
   * If stack is not empty it returns Some(poppedCard, restOfStack) otherwise it returns None.
   */
  def pop: Option[(Card, CoveredCardsStack)]
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
  
  def updatePlayer(player: Player): Players
}

trait RoundsManager extends GlobalAttribute {
  def roundId: Int
  
  def currentPlayer: Player
  def nextPlayer(circleOfPlayers: CircleOfPlayers): Player
  
  /**
   * Returns RoundsManager with next player round.
   */
  def withNextRound(circleOfPlayers: CircleOfPlayers): RoundsManager
}

/**
 * CardTrees is a global attribute storing all card trees built by players during the current round.
 */
case class CardTrees(trees: Map[Int, TreeWithCards] = Map.empty) extends GlobalAttribute

object Attribute {
  type AttributeTransformer[A <: Attribute] = PartialFunction[A, A]
}
