package game.standardtrach

import game.core._
import scala.collection.Seq
import game.core.Card
import game.core.CircleOfPlayers
import game.core.CoveredCardsStack
import game.core.DiscardedCardsStack
import game.core.GlobalActiveCards
import game.core.Hand
import game.core.Health
import game.core.Player
import game.core.PlayerActiveCards
import game.core.Players
import game.core.RoundsManager
import game.core.TargetChooser

object DefaultAttributes {

  class DefaultHealth(val value: Int = 5, val maxValue: Int = 5) extends Health {
    
    def getDamage(damageValue: Int) = new DefaultHealth(value - damageValue, maxValue)
  }

  class DefaultHand(val maxCards: Int = 5, val cards: Seq[Card]) extends Hand {
    
    def replacedCard(oldCard: Card, newCard: Card) = new DefaultHand(maxCards, cards.map {card => if (card == oldCard) newCard else card})
  }

  class DefaultActiveCards(val cards: Seq[Card] = Seq.empty) extends PlayerActiveCards

  class DefaultTargetChooser extends TargetChooser {
    
    def playersForTargets(circleOfPlayers: CircleOfPlayers) = circleOfPlayers.playersMap
  }
  
  class DefaultCoveredCardsStack(val cards: Vector[Card]) extends CoveredCardsStack {
    
    def pop = (cards.head, new DefaultCoveredCardsStack(cards.drop(1)))
  }
  
  class DefaultDiscardedCardsStack(val cards: Vector[Card]) extends DiscardedCardsStack {
    
    def push(card: Card) = new DefaultDiscardedCardsStack(cards.+:(card))
  }
  
  class DefaultGlobalActiveCards(val cards: Vector[Card]) extends GlobalActiveCards

  class DefaultPlayers(val circleOfPlayers: CircleOfPlayers) extends Players

  class DefaultRoundsManager(val currentPlayer: Player) extends RoundsManager {
    def nextPlayer(circleOfPlayers: CircleOfPlayers) = circleOfPlayers.nextTo(currentPlayer)
  }
}