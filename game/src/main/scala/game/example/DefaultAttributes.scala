package game.example

import game.core._

object DefaultAttributes {

  class DefaultHealth(val value: Int = 5, val maxValue: Int = 5) extends Health

  class DefaultHand(val maxCards: Int = 5, val cards: Seq[Card]) extends Hand

  class DefaultActiveCards(val cards: Seq[Card] = Seq.empty) extends PlayerActiveCards

  class DefaultTargetChooser extends TargetChooser {
    def playersForTargets(circleOfPlayers: CircleOfPlayers) = circleOfPlayers.playersMap
  }
  
  class DefaultCoveredCardsStack(val cards: Vector[Card]) extends CoveredCardsStack
  
  class DefaultDiscardedCardsStack(val cards: Vector[Card]) extends DiscardedCardsStack
  
  class DefaultGlobalActiveCards(val cards: Vector[Card]) extends GlobalActiveCards

  class DefaultPlayers(val circleOfPlayers: CircleOfPlayers) extends Players

  class DefaultRoundsManager(val currentPlayer: Player) extends RoundsManager {
    def nextPlayer(circleOfPlayers: CircleOfPlayers) = circleOfPlayers.nextTo(currentPlayer)
  }
}