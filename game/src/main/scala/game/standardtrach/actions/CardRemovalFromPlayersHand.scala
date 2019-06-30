package game.standardtrach.actions

import game.core._
import game.core.actions._

/** An action of removal the @card from @player's hand. The free place of the removed @card is filled
  * with a card from top of the covered cards stack (if the stack is nonempty).
  * 
  * Throws an exception if an action cannot be constructed; e.g. if player does not own the @card.
  */
class CardRemovalFromPlayersHand(player: Player, card: Card)(implicit initialState: GameState) extends Action {

  val _player = initialState.player(player)
  val _card = initialState.card(card)

  {
    if (!(_player.hand.cards contains _card))
      throw new Exception("Player does not owns the card which is to be removed.")
  }

  /** New game state. */
  def state: GameState = {
    val (poppedCardOpt, newCardsStacks) = initialState.cardsStacks.popCovered match {
      case Some((poppedCard, newCardsStacks)) => (Some(poppedCard), newCardsStacks)
      case None => (None, initialState.cardsStacks)
    }
    initialState transformed {
      case stacks: CardsStacks => newCardsStacks;
      case players: Players => players.updatePlayer(_player.transformed {
        case hand: Hand => hand.replacedCard(_card, poppedCardOpt);
      })
    }
  }
}