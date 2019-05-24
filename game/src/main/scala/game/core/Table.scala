package game.core

import game.core.actions._

/**
 * Table gives functions for building trees of cards and evaluating them to game state.
 */
object Table {
  /**
   * Tries to attach subtree @cn to a proper tree or add @cn as new tree if @cn is rooted (it is of type TreeWithCards).
   * In case of success it returns (newState, true) where newState is the @state with the subtree @cn attached and
   * all @cn's non-virtual cards are removed from player's hand. Otherwise it returns (@state, false).
   */
  def attach(cn: CardNode, state: GameState): (GameState, Boolean) = {
    val notAttached = (state, false)
    
    try {
      val (treeId, newTrees) = cn match {
        case newTree: TreeWithCards =>
          // if the card node @cn is a root node (tree with cards) then it should be added as a new tree
          val treeId = if (state.cardTrees.trees.keySet.isEmpty) 1 else state.cardTrees.trees.keySet.max + 1
          (treeId, state.cardTrees.trees + (treeId -> newTree))
        case _: CardInnerNode =>
          /**
           * Finds a tree where card node @cn should be attached and attaches @cn to the tree.
           * Otherwise it throws an exception
           */
          def findTreeAndAttach(s: Stream[(Int, TreeWithCards)]): (Int, TreeWithCards) = s match {
            case Stream.Empty => throw new Exception("A tree where card node should be attached not found")
            case (treeId, tree) #:: tail => try {
              (treeId, tree.attachSubtree(cn))
            } catch {
              case e: Exception => findTreeAndAttach(tail)
            }
          }
          
          val (treeId, newTree) = findTreeAndAttach(state.cardTrees.trees.toStream)
          // Replace the found tree with @newTree
          (treeId, state.cardTrees.trees + (treeId -> newTree))
        }
      val stateWithPlayerCardsRemoved = cn.playedCards.foldLeft(state) {
        (state, pc) => GameState.removeCardFromPlayersHand(pc.player, pc.card, state)
      }
      val stateWithCardNodeAdded = stateWithPlayerCardsRemoved transformed {
        case cardTrees: CardTrees => CardTrees(newTrees)
      }
      
      evaluate(Some((treeId, cn)), stateWithCardNodeAdded) /* throws an exception if one of nodes of @cn subtree
        cannot be evaluated to an action/transformer or it is not applicable. */
      (stateWithCardNodeAdded, true)
    } catch {
      case e: Exception => notAttached
    }
  }
  
  /**
   * New game state created from @state by application of actions evaluated from card trees.
   * All non-virtual cards from card trees are put on discarded stack.
   */
  def evaluate(state: GameState): GameState = evaluate(None, state)
  
  /**
   * Evaluates all card trees from game @state and applies their actions to the @state.
   * Puts all cards non-virtual cards from trees on discarded card stack.
   */
  private def evaluate(treeIdAndAddedSubtreeOpt: Option[(Int, CardNode)], state: GameState): GameState = {
    /**
     * Recursively removes and evaluates the first card tree until cardTrees map empty.
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
        val stateWithoutFirstTree = state transformed {
          case cardTrees: CardTrees => CardTrees(state.cardTrees.trees.tail)
        }
        val evaluatedState = evaluateTree(addedSubtreeOpt, tree, stateWithoutFirstTree)
        // put all cards from the @tree on discarded cards stack
        val newState = tree.playedCards.foldLeft(evaluatedState) { case (state, pc) =>
          GameState.putCardOnDiscardedStack(pc.card, state)
        }
        recursivelyEvaluateFirstTree(newState)
      }
    }
    
    recursivelyEvaluateFirstTree(state)
  }

  /**
   * Evaluates the @tree in the given game @state. If @addedSubtreeOpt is Some(@addedSubtree) then:
   * 1. It verifies that all nodes of subtree @addedSubtree was successfully evaluated to action (in root case)
   * or transformer (in inner node case).
   * 2. It verifies that for every node @n of subtree @addedSubtree, action/transformer related to @n was
   * applicated to the whole @tree evaluation.
   * Throws an exception if one of the points 1., 2. is not satisfied.
   */
  private def evaluateTree(addedSubtreeOpt: Option[CardNode], tree: TreeOfCards, state: GameState): GameState = {
    
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
            case (parentTransformer: ActionTransformer,
                (child: CardInnerNode, childTransformerOpt: Option[ActionTransformer])) =>
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
        val evaluatedState = tree.actionBuilder(state) match {
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
        evaluatedState
    }
  }
}
