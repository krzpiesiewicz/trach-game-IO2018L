package game.example

import game.abstracts._

object DefaultAttributes {

  class DefaultHealth(val value: Int = 5, val maxValue: Int = 5) extends Health

  class DefaultHand(val maxCards: Int = 5, val cards: Seq[Card]) extends Hand

  class DefaultActiveCards(val cards: Seq[Card] = Seq.empty) extends CharacterActiveCards

  class DefaultTargetChooser extends TargetChooser {
    def charactersForTargets(circleOfCharacters: CircleOfCharacters) = circleOfCharacters.characters
  }
  
  class DefaultCoveredCardsStack(val cards: Vector[Card]) extends CoveredCardsStack
  
  class DefaultDiscardedCardsStack(val cards: Vector[Card]) extends DiscardedCardsStack
  
  class DefaultGlobalActiveCards(val cards: Vector[Card]) extends GlobalActiveCards

  class DefaultCharacters(val circleOfCharacters: CircleOfCharacters) extends Characters

  class DefaultRoundsManager(val currentCharacter: Character) extends RoundsManager {
    def nextCharacter(circleOfCharacters: CircleOfCharacters) = circleOfCharacters.nextTo(currentCharacter)
  }
}