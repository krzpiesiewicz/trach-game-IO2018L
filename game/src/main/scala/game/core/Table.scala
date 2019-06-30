package game.core

import game.core.actions._

/** Has functions for building trees of cards and evaluating them to game state.
  */
object Table {

  /** Finds a pair {@code (treeId, tree)} in the given {@code stream} where card node {@card cardNode} should be attached
    * and attaches {@code cardNode} to the found {@code tree}.
    * If such a {@code tree} is not found, it throws an exception.
    *
    * @param cardNode card node to attach
    * @param stream   stream of pairs {@code (treeId, treeWithCards)}.
    * @return {@code (treeId, newTree)} where {@code newTree} is the found {@code tree} with {@code cardNode} attached.
    */
  def findTreeAndAttach(cardNode: CardNode, stream: Stream[(Int, TreeWithCards)]): (Int, TreeWithCards) = stream match {
    case Stream.Empty => throw new Exception("A tree where card node should be attached not found")
    case (treeId, tree) #:: tail => try {
      (treeId, tree.attachSubtree(cardNode))
    } catch {
      case e: Exception => findTreeAndAttach(cardNode, tail)
    }
  }

  /** Evaluates the {@code tree} to an action in the given game {@code state}.
    *
    * If {@code addedSubtreeOpt} is {@code Some(@addedSubtree)} then:
    *   1. It verifies that all nodes of subtree {@code addedSubtree} was successfully evaluated to an action (in root case)
    * or a transformer (in inner node case).
    *
    *   2. It verifies that for every node {@code n} of subtree {@code addedSubtree}, action/transformer related to {@code n} was
    * applied to the whole {@code tree} evaluation.
    *
    * It throws an exception if one of the points 1., 2. is not satisfied.
    *
    * So if {@code addedSubtreeOpt} is {@code None}, then an exception cannot be thrown.
    */
  def evaluateTree(addedSubtreeOpt: Option[CardNode], tree: TreeOfCards, state: GameState): Action = {

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
      case EmptyTree => NoneAction(state)
      case tree: TreeWithCards =>
        val wholeTreeIsAdded = isRootOfAddedSubtree(tree)
        val action = tree.actionBuilder(state) match {
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
            newAction
          case None =>
            if (wholeTreeIsAdded)
              throw new Exception("Root node action not build successfully")
            NoneAction(state)
        }
        action
    }
  }
}
