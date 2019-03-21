package game.core

trait GameState {
  def attributes: AttributesSet[GlobalAttribute]
}

class InitialState(players: Players, roundsManager: RoundsManager) extends GameState {
  val attributes = AttributesSet(Seq(players, roundsManager))
}

class NormalState(override val attributes: AttributesSet[GlobalAttribute]) extends GameState

trait EndState extends GameState