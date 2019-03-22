package game.core

object Actions {

  trait Action {
    def apply: GameState
  
    def changes: Seq[Change] = Seq.empty
  }
  
  case class NoneAction(state: GameState) extends Action {
    val apply = state
  }
  
  trait Change
  
  trait ActionBuilder
  
  trait ActionRefiner
}