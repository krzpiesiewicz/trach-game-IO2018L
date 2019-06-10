package game.standardtrach.actions

import game.core.actions.Action
import game.core.CardNode
import game.core.GameState
import game.core.Table
import game.core.CardTrees
import game.core.CardsStacks

/** CardTreeEvaluation is an action that finds a @tree for @treeId in card trees map of game state @initialState.
  * If there is no tree for @treeId then it gives @initialState.
  * Otherwise it gives new game state obtained in the following way:
  *   1. The found @tree is removed from card trees map.
  *   2. The @tree is evaluated to an action which is applied to the gamestate.
  *   3. All non-virtual cards from the @tree are put on discarded card stack.
  *
  * If @addedSubtreeOpt is Some(@addedSubtree) then:
  *   1. It verifies that all nodes of subtree @addedSubtree was successfully evaluated to action (in root case)
  *      or transformer (in inner node case).
  *   2. It verifies that for every node @n of subtree @addedSubtree, action/transformer related to @n was
  *      applicated to the whole @tree evaluation.
  *
  *   It throws an exception if one of the points 1., 2. is not satisfied.
  *
  * So if @addedSubtreeOpt is None, then an exception cannot be thrown.
  */
case class CardTreeEvaluation(initialState: GameState, treeId: Int, addedSubtreeOpt: Option[CardNode] = None)
  extends Action {

  /** New game state after application of an action evaluated from card @tree of @treeId.
    */
  val state: GameState = {
    initialState.cardTrees.trees.get(treeId) match {
      case None => initialState
      case Some(tree) =>
        val stateWithoutTheTree = initialState transformed {
          case cardTrees: CardTrees => CardTrees(initialState.cardTrees.trees - treeId)
        }
        val evaluatedState = Table.evaluateTree(addedSubtreeOpt, tree, stateWithoutTheTree).state
        val newState = tree.playedCards.foldLeft(evaluatedState) {
          case (state, pc) => state transformed {
            case stacks: CardsStacks => stacks.pushDiscarded(pc.card)
          }
        }
        newState
    }
  }
}

/** TreeEvaluation is an action that gives a new game state obtained by recursive applying CardTreeEvaluation action
  * for the first card tree. The recursion ends when the game state has no card tree.
  *
  * If @treeIdAndAddedSubtreeOpt is Some((@treeId, @addedSubtree)) then when applying CardTreeEvaluation action
  * for the tree of @treeId, CardTreeEvaluation action gets Some(@addedSubtree) as parameter @addedSubtreeOpt (Look at
  * @CardTreeEvaluation docs). It means that an exception might be thrown.
  *
  * Of course, if @treeIdAndAddedSubtreeOpt is None, then an exception cannot be thrown.
  */
case class AllCardTreesEvaluation(initialState: GameState, treeIdAndAddedSubtreeOpt: Option[(Int, CardNode)] = None)
  extends Action {

  /** New game state after application of all actions evaluated from card trees.
    */
  val state: GameState = {
    /** Recursively applies CardTreeEvaluation for the first card tree until cardTrees map is empty.
      */
    def recursivelyEvaluateFirstTree(state: GameState): GameState = {
      if (state.cardTrees.trees.isEmpty)
        state
      else {
        val (treeId, tree) = state.cardTrees.trees.head
        val addedSubtreeOpt = treeIdAndAddedSubtreeOpt match {
          case Some((`treeId`, subtree)) => Some(subtree)
          case _ => None
        }
        val newState = CardTreeEvaluation(state, treeId, addedSubtreeOpt).state
        recursivelyEvaluateFirstTree(newState)
      }
    }

    recursivelyEvaluateFirstTree(initialState)
  }
}