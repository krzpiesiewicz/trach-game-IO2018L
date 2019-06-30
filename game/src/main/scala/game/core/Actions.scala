package game.core

import game.Logging.logger
import scala.reflect.ClassTag

/** Provides traits relating to actions and transformers */
package object actions {

  /**
   * Action is an abstraction giving next gamestate and a seq of changes.
   * Action can be transformed by ActionTransformer into another action.
   */
  trait Action {
    def state: GameState
  }

  /**
   * ActionTransformer is a abstraction modifying actions and other transformers.
   */
  trait ActionTransformer {
    /**
     * Tries to transform an action into another one.
     * Returns Some(newAction) if transformation can be made or None otherwise.
     */
    def transform(action: Action): Option[Action]

    /**
     * Tries to transform a transformer into another one.
     * Returns Some(newTransformer) if transformation can be made or None otherwise.
     */
    def transform(transformer: ActionTransformer): Option[ActionTransformer]
  }
  
  type ActionBuilder = (GameState) => Option[Action]
  type ActionTransformerBuilder = (GameState) => Option[ActionTransformer]

  /**
   * Action initialized by played starting card.
   */
  trait CardAction[C <: Card, P <: PlayedStartingCard[C]] extends Action

  /**
   * ActionTransformer based on played card.
   */
  trait ActionCardTransformer[C <: Card, P <: PlayedCardInTree[C]] extends ActionTransformer
  
  /**
   * Factory for creating builders of actions and transformers based on played cards.
   */
  trait BuildersFactory {
    /**
     * Creates an action builder based on played starting card.
     */
    def createActionBuilder[C <: Card](pc: PlayedStartingCard[C]): ActionBuilder

    /**
     * Creates a transformer builder based on played card.
     */
    def createTransformerBuilder[C <: Card](pc: PlayedCardInTree[C]): ActionTransformerBuilder
  }
  
  /**
   * NoneAction changes nothing in game state.
   */
  case class NoneAction(state: GameState) extends Action
  
  /**
   * EmptyTransformer changes nothing in action or another transformer.
   */
  class EmptyTransformer extends ActionTransformer {
    /**
     * Returns Some(@action).
     */
    def transform(action: Action): Option[Action] = Some(action)

    /**
     * Returns Some(@transformer).
     */
    def transform(transformer: ActionTransformer) = Some(transformer)
  }
}

