package game.standardtrach.actions

import game.core._
import game.core.actions._

/** An action transforming @initialState to a state with the next round (the round of the next player). */
case class NextRound(initialState: GameState) extends Action {
  
  def state: GameState = initialState transformed {
    case rm: RoundsManager => rm.withNextRound(initialState.attributes.forceGet[Players].circleOfPlayers)
  }
}