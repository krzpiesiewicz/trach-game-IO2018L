package game.core

import game.core.actions._

/**
 * Table is responsible for building tree of cards and evaluating it to game state.
 */
case class Table(val state: GameState, val tree: TreeOfCards = EmptyTree) {

  /**
   * Tries to attach subtree @cn to the @tree.
   * In case of success it returns (newTable, true) where newTable is @this table with subtree @cn added to the @tree.
   * Otherwise it returns (@this, false).
   */
  def attach(cn: CardNode): (Table, Boolean) = {
    val notAttached = (this, false)
    try {
      val newTree = tree.attachSubtree(cn)
      val newState = cn.playedCards.foldLeft(state) {
        (state, pc) => GameState.removeCardFromPlayersHand(pc.player, pc.card, state)
      }
      evaluate(Some(cn), newTree, newState) // throws an exception if one of nodes of @cn subtree cannot be evaluated to an action/transformer or it is not applicable
      val newTable = Table(newState, newTree)
      (newTable, true)
    } catch {
      case e: Exception => notAttached
    }
  }
  
  /**
   * New game state created from @state by application of action evaluated of @tree.
   */
  lazy val evaluate: GameState = evaluate(None, tree, state)

  /**
   * Evaluates the tree in the given game @state. If @addedSubtreeOpt is Some(@addedSubtree) then:
   * 1. It verifies that all nodes of subtree @addedSubtree was successfully evaluated to action (in root case) or transformer (in inner node case).
   * 2. It verifies that for every node @n of subtree @addedSubtree, action/transformer related to @n was applicated to the whole @tree evaluation.
   * Throws an exception if one of the points 1., 2. is not satisfied.
   */
  def evaluate(addedSubtreeOpt: Option[CardNode], tree: TreeOfCards, state: GameState): GameState = {
    
    def isRootOfAddedSubtree(cn: CardNode) = addedSubtreeOpt match {
      case None => false
      case Some(addedSubtree) => cn.playedCard.card == addedSubtree.playedCard.card
    }

    def evalTree(node: CardInnerNode, isNodeOfAddedSubtree: Boolean): Option[ActionTransformer] = {
      node.transformerBuilder(state) match {
        case Some(transformer) =>
          val childrenAndTransformersOpts = node.children.map { child =>
            val childTransformerOpt = evalTree(child, isNodeOfAddedSubtree || isRootOfAddedSubtree(child))
            (child, childTransformerOpt)
            }

          // transform the parent by all the children transformers.
          val newTransformer = childrenAndTransformersOpts.foldLeft(transformer) {
            case (parentTransformer: ActionTransformer, (child: CardInnerNode, childTransformerOpt: Option[ActionTransformer])) =>
              childTransformerOpt match {
                case Some(childTransformer) =>
                  // check if childTransformer is applicable to the parent.
                  childTransformer.transform(parentTransformer) match {
                    case Some(transformer) => transformer
                    case None =>
                      if (isNodeOfAddedSubtree || isRootOfAddedSubtree(child))
                        throw new Exception("Child's transformer not applicable to the parent's transformer")
                      parentTransformer
                  }
                case None => parentTransformer // if it isNodeOfAddedSubtree, an exception was thrown in recursive call of evalTree.
              }
          }
          Some(newTransformer)
        case None =>
          if (isNodeOfAddedSubtree)
            throw new Exception("Inner node transformer not build successfully")
          None
      }
    }

    tree match {
      case EmptyTree => state
      case tree: TreeWithCards =>
        val wholeTreeIsAdded = isRootOfAddedSubtree(tree)
        tree.actionBuilder(state) match {
          case Some(action) =>
            val childrenTransformers = tree.children.map { child =>
              val isNodeOfAddedSubtree = wholeTreeIsAdded || isRootOfAddedSubtree(child)
              (evalTree(child, isNodeOfAddedSubtree), isNodeOfAddedSubtree)
            }
            val newAction = childrenTransformers.foldLeft(action) {
              case (action: Action, (transformerOpt: Option[ActionTransformer], isNodeOfAddedSubtree)) =>
                transformerOpt match {
                  case Some(transformer) =>
                    transformer.transform(action) match {
                      case Some(action) => action
                      case None =>
                        if (isNodeOfAddedSubtree)
                          throw new Exception("Child's transformer not applicable to the parent's action")
                        action
                    }
                  case None => action // if it isNodeOfAddedSubtree, an exception was thrown in recursive call of evalTree.
                }
            }
            newAction.state
          case None =>
            if (wholeTreeIsAdded)
              throw new Exception("Root node action not build successfully")
            state
        }
    }
  }
}
