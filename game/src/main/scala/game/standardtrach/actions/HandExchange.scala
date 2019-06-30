package game.standardtrach.actions

import game.core._
import game.core.actions._

/** Player on move can exchange cards from hand at the beginning of a round.
  *  Throws an exception if an action cannot be construct; e.g. if number of @cards to exchange is greater than 3
  *  or @player does not own the @cards.
  */
class HandExchange(player: Player, cards: Seq[Card])(implicit initialState: GameState) extends Action {
  
  val _player = initialState.player(player)
  
  {
    if (initialState.roundsManager.isBeginingOfTheRound && initialState.roundsManager.currentPlayer != _player)
      throw new Exception(s"An action from the player of id=${_player.id} is not expected.")
    if (!cards.forall(_player.owns(_)))
      throw new Exception("Player does not own the cards that he wants to exchange.")
    if (cards.length > 3)
      throw new Exception("Player can exchange not more than 3 cards.")
  }

  /** New game state with @player's @cards from hand exchanged.
    */
  def state: GameState = {
    val stateWithPlayersHPdecreased = initialState transformed {
      case players: Players => players.updatePlayer(_player.transformed {
        case health: Health => health.changedHP(-1)
      })
    }
    val newState = cards.foldLeft(stateWithPlayersHPdecreased) {
      (state, card) =>
        new CardRemovalFromPlayersHand(_player, card)(state).state transformed {
          case stacks: CardsStacks => stacks.pushDiscarded(card)
        }
    }
    new NextRound(newState).state
  }
}