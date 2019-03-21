package game.example

import game.core._

object ActionsBuilders {
  
  implicit object attackBuilderFactory extends StartingCardActionBuilderFactory[AttackCard] {
    
    def apply(card: AttackCard, player: Player, state: GameState) = new ActionBuilder {
      def apply = NoneAction(state)
    }
  }
}