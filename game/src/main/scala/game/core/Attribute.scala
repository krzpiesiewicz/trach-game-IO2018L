package game.core

import Player.PlayerId

trait Attribute

trait GlobalAttribute extends Attribute

trait PlayerAttribute extends Attribute

trait Health extends PlayerAttribute {
  def value: Int
  def maxValue: Int
}

trait Hand extends PlayerAttribute {
  def maxCards: Int
  def cards: Seq[Card]
}

trait PlayerActiveCards extends PlayerAttribute {
  def cards: Seq[Card] 
}

trait TargetChooser extends PlayerAttribute {
  def playersForTargets(circleOfPlayers: CircleOfPlayers): Map[PlayerId, Player]
}

trait CoveredCardsStack extends GlobalAttribute {
  def cards: Seq[Card]
}

trait DiscardedCardsStack extends GlobalAttribute {
  def cards: Seq[Card]
}

trait GlobalActiveCards extends GlobalAttribute {
  def cards: Seq[Card] 
}

trait Players extends GlobalAttribute {
  def circleOfPlayers: CircleOfPlayers
}

trait RoundsManager extends GlobalAttribute {
  def currentPlayer: Player
  def nextPlayer(circleOfPlayers: CircleOfPlayers): Player
}

trait AttributeBuilder[A <: Attribute] {
  def apply(attribute: A): A
}
