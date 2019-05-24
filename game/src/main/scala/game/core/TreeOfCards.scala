package game.core

import game.Logging.logger

import game.core.actions._

/**
 * Trait representing tree of played cards. Every node of the tree has a played card.
 * If the tree is not empty, its root has a starting card played.
 */
trait TreeOfCards {
  /**
   * Returns tree of played cards with @subtree attached if the @subtree can be attached.
   * Otherwise it throws an exception.
   */
  def attachSubtree(subtree: CardNode): TreeOfCards
}

/**
 * Trait describing a root of rooted subtree of cards. It is extended by root tree (TreeWithCards) and inner node (CardInnerNode).
 */
trait CardNode {
  type N <: CardNode

  val children: Seq[CardInnerNode]
  val playedCard: PlayedCard[_ <: Card]

  def withChildren(newChildren: Seq[CardInnerNode]): N
  
  /**
   * Returns a tree of @this with @subtree attached to the corresponding place in the tree of @this if there is such a place.
   * Otherwise it throws an exception.
   */
  def attach(subtree: CardInnerNode): N = {
    /**
     * Returns (true, @newNode) if in the tree of @node there is a PlayedCard of the parent card of the root node of the @subtree.
     * (It means that @node or one of its offsprings has a PlayedCard of the @subtree.playedCard.parentCard).
     * @newNode is @node with @subtree attached to the corresponding place.
     * Otherwise it returns (false, @node).
     */
    def mapTree[CN <: CardNode](node: CN): (Boolean, node.N) = {
      if (node.playedCard.card == subtree.playedCard.parentCard) {
        (true, node.withChildren(node.children :+ subtree))
      }
      else {
        val (attached, newChildren) = node.children.foldLeft[(Boolean, Seq[CardInnerNode])](false, Seq.empty) {
          case ((prevAttached, newChildrenBuf), child) =>
            val (childAttached, newChild) = mapTree(child)
            (prevAttached || childAttached, newChildrenBuf :+ newChild)
        }
        val newNode = node.withChildren(newChildren)
        (attached, newNode)
      }
    }

    val (attached, newTree) = mapTree(this)
    if (!attached)
      throw new Exception(s"Subtree $subtree not attached in three of CardNode $this. Subtree root parent card is missing in the tree of $this.")
    newTree
  }

  /**
   * Returns a tree of @this with @pcit attached to the corresponding place in the tree of @this if there is such a place.
   * Otherwise it throws an exception.
   */
  def attach(pcit: PlayedCardInTree[_ <: Card])(implicit buildersFactory: BuildersFactory): N =
    attach(CardInnerNode(pcit, buildersFactory.createTransformerBuilder(pcit)))
    
  /**
   * Returns all played cards which the tree contains.
   */
  def playedCards: Seq[PlayedCard[_ <: Card]] = children.foldLeft(Seq[PlayedCard[_ <: Card]](playedCard)){case (seq, child) => seq ++ child.playedCards}
}

/**
 * Inner node of tree of played cards that is not a root of the tree.
 */
case class CardInnerNode(
  val playedCard: PlayedCardInTree[_ <: Card],
  val transformerBuilder: ActionTransformerBuilder,
  val children: Seq[CardInnerNode] = Seq.empty) extends CardNode {

  type N = CardInnerNode

  def withChildren(newChildren: Seq[CardInnerNode]) = CardInnerNode(playedCard, transformerBuilder, newChildren)
}

object CardInnerNode {
  def apply(pcit: PlayedCardInTree[_ <: Card])(implicit buildersFactory: BuildersFactory): CardInnerNode =
    CardInnerNode(pcit, buildersFactory.createTransformerBuilder(pcit))
}

/**
 * Tree of cards with some played cards, where there is a starting card played in the root.
 */
case class TreeWithCards(
  val playedCard: PlayedStartingCard[_ <: Card],
  val actionBuilder: ActionBuilder,
  val children: Seq[CardInnerNode] = Seq.empty) extends TreeOfCards with CardNode {

  type N = TreeWithCards

  def withChildren(newChildren: Seq[CardInnerNode]) = TreeWithCards(playedCard, actionBuilder, newChildren)
  
  def attachSubtree(subtree: CardNode): TreeWithCards = subtree match {
    case subtree: CardInnerNode => attach(subtree)
    case _ => throw new Exception(s"Cannot attached a subtree where its root is based on a starting card played.")
  }
}

object TreeWithCards {
  def apply(psc: PlayedStartingCard[_ <: Card])(implicit buildersFactory: BuildersFactory): TreeWithCards =
    TreeWithCards(psc, buildersFactory.createActionBuilder(psc))
}

/**
 * Tree of cards with no played cards.
 */
case object EmptyTree extends TreeOfCards {
  
  def attachSubtree(subtree: CardNode): TreeOfCards = subtree match {
    case rootTree: TreeWithCards => rootTree
    case _ => throw new Exception(s"Cannot create a TreeOfCards only from subtree where subtree's root is not based on a starting card played.")
  }
}
