package game.example

import game.abstracts._

object ActionsBuilders {
  
  implicit object attackBuilderFactory extends StartingCardActionBuilderFactory[AttackCard] {
    
    def apply(card: AttackCard, character: Character, state: GameState) = new ActionBuilder {
      def apply = NoneAction(state)
    }
  }
}