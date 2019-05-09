package game.core

/**
 * Player on move can exchange cards from hand at the beginning of a round.
 */
case class HandExchange(player: Player, cards: Seq[Card])

object HandExchange {
  /**
   * If number of cards to exchange is not greater than 3, then there are exchange to
   */
  def exchange(he: HandExchange)(implicit state: GameState): (GameState, Boolean) = {
    val notExchanged = (state, false)
    if (he.cards.length > 3)
      notExchanged
    else {
      val stateWithPlayersHPdecreased = state transformed {
        case players: Players => players.updatePlayer(he.player.transformed {
          case health: Health => health.changedHP(-1)
        })
      }
      val newState = he.cards.foldLeft(stateWithPlayersHPdecreased) {
        (state, card) =>
          GameState.putCardOnDiscardedStack(
            card = card,
            state = GameState.removeCardFromPlayersHand(he.player, card, state))
      }
      (newState, true)
    }
  }
}