package game.standardtrach

import game.core._
import scala.collection.Seq
import game.core.Card
import game.core.CircleOfPlayers
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

  case class DefaultHealth(value: Int = 5, maxValue: Int = 5) extends Health {

    def getDamage(damageValue: Int) = new DefaultHealth(value - damageValue, maxValue)
    
    def changedHP(change: Int) = new DefaultHealth(math.max(0, math.min(maxValue, value + change)), maxValue)
  }

  case class DefaultHand(maxCards: Int = 5, cards: Seq[Card]) extends Hand {

    def replacedCard(oldCard: Card, newCardOpt: Option[Card]) = new DefaultHand(
      maxCards,
      newCardOpt match {
        case Some(newCard) => cards.map { card => if (card == oldCard) newCard else card }
        case None => cards.filterNot(_ == oldCard)
      })
  }

  case class DefaultActiveCards(cards: Seq[Card] = Seq.empty) extends PlayerActiveCards

  case class DefaultTargetChooser() extends TargetChooser {

    def playersForTargets(circleOfPlayers: CircleOfPlayers) = circleOfPlayers.playersMap
  }

  /** Attribute describing two cards stacks:
   *  - discarded cards stack (used cards),
   *  - covered cards stack (cards ready to be used in the game).
   */
  case class DefaultCardsStacks(discardedCards: Vector[Card], coveredCards: Vector[Card]) extends CardsStacks {
    /**
     * Pops a card from the covered cards stack.
     * If stack is not empty it returns Some(poppedCard, newCardsStacks) otherwise it returns None.
     */
    def popCovered: Option[(Card, CardsStacks)] =
      if (coveredCards.isEmpty)
        None
      else
        Some(coveredCards.head, DefaultCardsStacks(discardedCards, coveredCards.drop(1)))
    
    /**
     * Pushes a @card onto the discarded stack.
     * Returns CardsStacks with @card pushed on the discarded cards stack.
     */
    def pushDiscarded(card: Card): CardsStacks =
      DefaultCardsStacks(discardedCards.+:(card), coveredCards)
  }
  
  case class DefaultGlobalActiveCards(cards: Vector[Card]) extends GlobalActiveCards

  case class DefaultAllCards(cardsMap: Map[CardId, Card]) extends AllCards
  
  object DefaultAllCards {
    def apply(cards: Seq[Card]) = new DefaultAllCards(cards.map(c => (c.id, c)).toMap)
  }
  
  case class DefaultPlayers(val circleOfPlayers: CircleOfPlayers) extends Players {

    def updatePlayer(player: Player) = new DefaultPlayers(circleOfPlayers.updatePlayer(player))
  }

  case class DefaultRoundsManager(
      currentPlayer: Player,
      roundId: Int = 1,
      isBeginingOfTheRound: Boolean = true) extends RoundsManager {
    
    def nextPlayer(circleOfPlayers: CircleOfPlayers): Player = {
      /**
       * Returns the first alive player next to given @player
       * or @currentPlayer if no one alive between @player and @currentPlayer.
       */
      def nextAlive(player: Player): Player = {
        val next = circleOfPlayers.nextTo(player)
        if (!next.isDead)
          next
        else if (next != currentPlayer)
          nextAlive(next)
        else
          currentPlayer
      }
      
      nextAlive(currentPlayer)
    }
    
    def withNextRound(circleOfPlayers: CircleOfPlayers) =
      DefaultRoundsManager(nextPlayer(circleOfPlayers), roundId + 1)
    
    def roundStarted: RoundsManager = DefaultRoundsManager(currentPlayer, roundId, false)
  }
}