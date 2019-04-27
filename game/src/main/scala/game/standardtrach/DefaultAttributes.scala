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
import game.core.Card.CardId

object DefaultAttributes {

  case class DefaultHealth(val value: Int = 5, val maxValue: Int = 5) extends Health {

    def getDamage(damageValue: Int) = new DefaultHealth(value - damageValue, maxValue)
  }

  case class DefaultHand(val maxCards: Int = 5, val cards: Seq[Card]) extends Hand {

    def replacedCard(oldCard: Card, newCardOpt: Option[Card]) = new DefaultHand(
      maxCards,
      newCardOpt match {
        case Some(newCard) => cards.map { card => if (card == oldCard) newCard else card }
        case None => cards.filterNot(_ == oldCard)
      })
  }

  case class DefaultActiveCards(val cards: Seq[Card] = Seq.empty) extends PlayerActiveCards

  case class DefaultTargetChooser() extends TargetChooser {

    def playersForTargets(circleOfPlayers: CircleOfPlayers) = circleOfPlayers.playersMap
  }

  case class DefaultCoveredCardsStack(val cards: Vector[Card]) extends CoveredCardsStack {

    def pop = if (cards.isEmpty) None else Some(cards.head, new DefaultCoveredCardsStack(cards.drop(1)))
  }

  case class DefaultDiscardedCardsStack(val cards: Vector[Card]) extends DiscardedCardsStack {

    def push(card: Card) = new DefaultDiscardedCardsStack(cards.+:(card))
  }

  case class DefaultGlobalActiveCards(val cards: Vector[Card]) extends GlobalActiveCards

  case class DefaultAllCards(cardsMap: Map[CardId, Card]) extends AllCards
  
  object DefaultAllCards {
    def apply(cards: Seq[Card]) = new DefaultAllCards(cards.map(c => (c.id, c)).toMap)
  }
  
  case class DefaultPlayers(val circleOfPlayers: CircleOfPlayers) extends Players {

    def updatePlayer(player: Player) = new DefaultPlayers(circleOfPlayers.updatePlayer(player))
  }

  case class DefaultRoundsManager(val currentPlayer: Player, val roundId: Int = 1) extends RoundsManager {
    def nextPlayer(circleOfPlayers: CircleOfPlayers) = circleOfPlayers.nextTo(currentPlayer)
    
    def withNextRound(circleOfPlayers: CircleOfPlayers) = DefaultRoundsManager(nextPlayer(circleOfPlayers), roundId + 1)
  }
}