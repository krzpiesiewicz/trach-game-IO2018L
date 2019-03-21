package game.core

trait Action {
  def apply: GameState

  def changes: Seq[Change] = Seq.empty
}

case class NoneAction(state: GameState) extends Action {
  val apply = state
}

trait Change

trait ActionBuilder {
  def apply: Action
}

trait StartingCardActionBuilderFactory[C <: StartingCard] {
  def apply(card: C, character: Player, state: GameState): ActionBuilder
}

object StartingCardActionBuilder {
  def apply[C <: StartingCard](card: C, character: Player, state: GameState)(implicit builder: StartingCardActionBuilderFactory[C]) = builder(card, character, state)
}