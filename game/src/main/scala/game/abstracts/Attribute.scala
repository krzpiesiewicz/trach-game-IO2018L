package game.abstracts

import Character.CharacterId

trait Attribute

trait GlobalAttribute extends Attribute

trait CharacterAttribute extends Attribute

trait Health extends CharacterAttribute {
  def value: Int
  def maxValue: Int
}

trait Hand extends CharacterAttribute {
  def maxCards: Int
  def cards: Seq[Card]
}

trait CharacterActiveCards extends CharacterAttribute {
  def cards: Seq[Card] 
}

trait TargetChooser extends CharacterAttribute {
  def charactersForTargets(circleOfCharacters: CircleOfCharacters): Map[CharacterId, Character]
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

trait Characters extends GlobalAttribute {
  def circleOfCharacters: CircleOfCharacters
}

trait RoundsManager extends GlobalAttribute {
  def currentCharacter: Character
  def nextCharacter(circleOfCharacters: CircleOfCharacters): Character
}

trait AttributeBuilder[A <: Attribute] {
  def apply(attribute: A): A
}
