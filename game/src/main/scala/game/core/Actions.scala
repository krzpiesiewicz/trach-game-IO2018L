package game.core

import game.Logging.logger
import scala.reflect.ClassTag

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
  
//  /**
//   * ActionBuilder can build an action that depends from a given gamestate. 
//   */
//  trait ActionBuilder {
//    /**
//     * Builds an action that depends from the given gamestate. 
//     */
//    def apply(state: GameState): Option[Action]
//  }
//  
//  /**
//   * ActionTransformerBuilder can build a transformer that depends from a given gamestate. 
//   */
//  trait ActionTransformerBuilder {
//    /**
//     * Builds a transformer that depends from the given gamestate. 
//     */
//    def apply(state: GameState): ActionTransformer
//  }
  
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

//  /**
//   * ActionBuilder that builds an action initialized by played starting card.
//   */
//  abstract class CardActionBuilder[C <: Card, P <: PlayedStartingCard[C]](pc: PlayedStartingCard[C]) extends ActionBuilder
//
//  /**
//   * ActionTransformerBuilder that builds a transformer based on played card.
//   */
//  abstract class ActionCardTransformerBuilder[C <: Card, P <: PlayedCardInTree[C]](pc: PlayedCardInTree[C]) extends ActionTransformerBuilder
  
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
  class NoneAction(val state: GameState) extends Action
  
  /**
   * EmptyTransformer changes nothing in action or another transformer.
   */
  class EmptyTransformer extends ActionTransformer {
    def transform(action: Action): Option[Action] = Some(action)

    def transform(transformer: ActionTransformer) = Some(transformer)
  }
}

