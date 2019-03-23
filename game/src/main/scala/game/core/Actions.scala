package game.core

import game.Logging.logger

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
case class NoneAction(val state: GameState) extends Action

/**
 * User-oriented piece of information describing a single change in game.
 */
trait Change

/**
 * ActionTransformer refines
 */
trait ActionTransformer {
  def transform(action: Action): Option[Action]
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

