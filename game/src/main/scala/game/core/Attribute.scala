package game.core

import Player.PlayerId
import game.core.Card.CardId

/** Attributes are abstractions which describes the state of game. Almost every element in game should be an attribute
  * or be included in some attribute.
  * There are two main kinds of attributes:
  *
  * [[PlayerAttribute]] - describing a player. Every @Player has a set of attributes (@AttributesSet[PlayerAttribute]).
  * [[GlobalAttribute - describing the whole game (not connected with the certain player).
  * [[GameState]] has a set of attributes [[AttributesSet[GlobalAttribute] ]].
  * [[Player]] has a set of attributes [[AttributesSet[PlayerAttribute] ]].
  */
trait Attribute

/** Attribute describing the whole game (not connected with the certain player).
  * [[GameState]] has a set of attributes (@AttributesSet[GlobalAttribute]).
  */
trait GlobalAttribute extends Attribute

/** Attribute describing a player. [[Player]] has a set of attributes [[AttributesSet[PlayerAttribute] ]]. */
trait PlayerAttribute extends Attribute

/** Attribute describing player's health. */
trait Health extends PlayerAttribute {
  /** Current value of player's health.
    * @return current value of player's health.
    */
  def value: Int

  /** Maximal value of player's health.
    * @return maximal value of player's health.
    */
  def maxValue: Int

  /** Returns a [[Health]] of the player that got a damage of value {@code damageValue}.
    * Health should implement policy of getting damages.
    * @param damageValue value of a damage
    * @return a health of player that got a damage of value {@code damageValue}.
    */
  def getDamage(damageValue: Int): Health

  /** Returns a [[Health]] of the player with {@code newValue} equal old {@code value + change}. Of course, {@code newValue}
    * should be in {@code [0, maxValue]}.
    * The change is absolute. It should be modified by any health policy.
    * @param change
    * @return
    */
  def changedHP(change: Int): Health
}

/** Attribute describing player's hand with cards. */
trait Hand extends PlayerAttribute {
  /** Maximal number of cards that player can hold in hand */
  def maxCards: Int

  /** Sequence of cards in player's hand */
  def cards: Seq[Card]

  /** Returns the hand where {@code oldCard] is replaced by {@code newCard} when {@code newCardOpt} is {@code Some(newCard)}
    * Otherwise it returns the hand without {@code oldCard}.
    * @param oldCard
    * @param newCardOpt
    * @return
    */
  def replacedCard(oldCard: Card, newCardOpt: Option[Card]): Hand
}

trait PlayerActiveCards extends PlayerAttribute {
  def cards: Seq[Card]
}

trait TargetChooser extends PlayerAttribute {
  def playersForTargets(circleOfPlayers: CircleOfPlayers): Map[PlayerId, Player]
}

/** Attribute describing two cards stacks:
  *  - discarded cards stack (used cards),
  *  - covered cards stack (cards ready to be used in the game).
  */
trait CardsStacks extends GlobalAttribute {
  /** Cards on the discarded cards stack */
  def discardedCards: Seq[Card]

  /** Cards on the covered cards stack */
  def coveredCards: Seq[Card]

  /** Pops a card from the covered cards stack.
    * If stack is not empty it returns Some(poppedCard, newCardsStacks) otherwise it returns None.
    * @return
    */
  def popCovered: Option[(Card, CardsStacks)]

  /** Pushes a @card onto the discarded stack.
    * Returns CardsStacks with @card pushed on the discarded cards stack.
    */
  def pushDiscarded(card: Card): CardsStacks
}

/** Attribute describing a set of global active cards */
trait GlobalActiveCards extends GlobalAttribute {
  def cards: Seq[Card]
}

/** Attribute describing a set of all cards in gameplay */
trait AllCards extends GlobalAttribute {
  def cardsMap: Map[CardId, Card]
}

/** Attribute describing a set of Players */
trait Players extends GlobalAttribute {
  /** Collection of players set in some order */
  def circleOfPlayers: CircleOfPlayers

  /** Updates certain {@code player}
    * @param player
    * @return [[Players]] with the {@code player}
    */
  def updatePlayer(player: Player): Players
}

/** Attribute describing a state of round management */
trait RoundsManager extends GlobalAttribute {
  def roundId: Int

  def currentPlayer: Player

  /** Get the player who is next on move.
    * @param circleOfPlayers
    * @return
    */
  def nextPlayer(circleOfPlayers: CircleOfPlayers): Player

  /** Checks if it is beginning of round.
    * @return
    */
  def isBeginingOfTheRound: Boolean

  /** Returns RoundsManager with next player round.
    * @param circleOfPlayers
    * @return
    */
  def withNextRound(circleOfPlayers: CircleOfPlayers): RoundsManager

  /** Returns RoundManager with isBeginingOfTheRound = false.
    * @return
    */
  def roundStarted: RoundsManager
}

/** CardTrees is a global attribute storing all card trees built by players during the current round.
  * @param trees
  */
case class CardTrees(trees: Map[Int, TreeWithCards] = Map.empty) extends GlobalAttribute

/** Companion object for Attribute class */
object Attribute {
  type AttributeTransformer[A <: Attribute] = PartialFunction[A, A]
}
