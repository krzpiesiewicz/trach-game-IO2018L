package game.abstracts

trait GameState {
  def attributes: AttributesSet[GlobalAttribute]
}

class InitialState(characters: Characters, roundsManager: RoundsManager) extends GameState {
  val attributes = AttributesSet(Seq(characters, roundsManager))
}

class NormalState(override val attributes: AttributesSet[GlobalAttribute]) extends GameState

trait EndState extends GameState