package game.core

import game.Logging.logger

package object actions {

  /**
   * Action is an abstraction giving next gamestate and a seq of changes.
   * Action can be transformed by ActionTransformer into another action.
   */
  trait Action {
    def state: GameState

    def changes: Seq[Change] = Seq.empty
  }

  /**
   * NoneAction changes nothing in game state.
   */
  class NoneAction(val state: GameState) extends Action

  /**
   * User-oriented piece of information describing a single change in game.
   */
  trait Change

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

  /**
   * EmptyTransformer changes nothing in action or another transformer.
   */
  class EmptyTransformer extends ActionTransformer {
    def transform(action: Action): Option[Action] = Some(action)

    def transform(transformer: ActionTransformer) = Some(transformer)
  }

  /**
   * Action initialized by played starting card.
   */
  trait CardAction[C <: Card, P <: PlayedStartingCard[C]] extends Action

  /**
   * ActionTransformer based on played card.
   */
  trait ActionCardTransformer[C <: Card, P <: PlayedCardInTree[C]] extends ActionTransformer

  /**
   * Factory for creating actions and transformers based on played cards.
   */
  trait ActionFactory {
    def createAction[C <: Card, P <: PlayedStartingCard[C]](psc: P): CardAction[C, P]

    def createTransformer[C <: Card, P <: PlayedCardInTree[C]](psc: P): ActionCardTransformer[C, P]
  }
}

