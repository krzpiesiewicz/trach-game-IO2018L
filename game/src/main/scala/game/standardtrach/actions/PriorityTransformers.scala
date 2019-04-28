package game.standardtrach.actions

import game.core.actions._
import game.core.PlayedCardInTree
import game.core.GameState
import game.standardtrach.PriorityIncrementerCard

trait ActionWithPriority extends Action {
  def priority: Int

  /**
   * Returns action with new priority
   */
  def withPriority(newPriority: Int): ActionWithPriority
}

trait ActionTransformerWithPriority extends ActionTransformer {
  def priority: Int

  /**
   * Returns action transformer with new priority
   */
  def withPriority(newPriority: Int): ActionTransformerWithPriority
}

class PriorityIncrementer(pcit: PlayedCardInTree[PriorityIncrementerCard])(implicit initialState: GameState)
  extends ActionCardTransformer[PriorityIncrementerCard, PlayedCardInTree[PriorityIncrementerCard]] {

  def transform(action: Action) = action match {
    case action: ActionWithPriority => Some(action.withPriority(action.priority - 2))
    case _ => None
  }

  def transform(transformer: ActionTransformer) = transformer match {
    case transformer: ActionTransformerWithPriority => Some(transformer.withPriority(transformer.priority - 2))
    case _ => None
  }
}