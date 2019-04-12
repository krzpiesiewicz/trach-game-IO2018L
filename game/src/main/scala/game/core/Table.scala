package game.core

import scala.language.existentials

import game.core.actions._

trait TreeOfCards

trait CardNode {
  type N <: CardNode

  val children: Seq[CardInnerNode]
  val playedCard: PlayedCard[_ <: Card]

  def withChildren(newChildren: Seq[CardInnerNode]): N
}

case object EmptyTree extends TreeOfCards

case class TreeWithCards(
  val playedCard: PlayedStartingCard[_ <: Card],
  val actionBuilder: ActionBuilder,
  val children: Seq[CardInnerNode] = Seq.empty) extends TreeOfCards with CardNode {

  type N = TreeWithCards

  def withChildren(newChildren: Seq[CardInnerNode]) = TreeWithCards(playedCard, actionBuilder, newChildren)

  def attachPlayedCard(pcit: PlayedCardInTree[_ <: Card], transformerBuilder: ActionTransformerBuilder): TreeWithCards = {

    def mapTree[CN <: CardNode](node: CN): node.N = {
      if (node.playedCard.card == pcit.parentCard)
        node.withChildren(children :+ new CardInnerNode(pcit, transformerBuilder))
      else
        node.withChildren(node.children map { case child => mapTree(child) })
    }

    mapTree(this)
  }
}

case class CardInnerNode(
  val playedCard: PlayedCardInTree[_ <: Card],
  val transformerBuilder: ActionTransformerBuilder,
  val children: Seq[CardInnerNode] = Seq.empty) extends CardNode {

  type N = CardInnerNode

  def withChildren(newChildren: Seq[CardInnerNode]) = CardInnerNode(playedCard, transformerBuilder, newChildren)
}

case class Table(val state: GameState, val tree: TreeOfCards = EmptyTree)(implicit buildersFactory: BuildersFactory) {

  def attachCard(pcr: PlayedCardRequest): (Table, Boolean) = {
    val notAttached: (Table, Boolean) = (this, false)
    try {
      val pc = PlayedCard(pcr)(state)
      if (pc.player.owns(pc.card))
        tree match {
          case EmptyTree => pc match {
            case psc: PlayedStartingCard[_] => {
              val actionBuilder = buildersFactory.createActionBuilder(psc)
              actionBuilder(state) match {
                case Some(action) => {
                  val newState = GameState.removeCardFromPlayersHand(psc.player, psc.card, state)
                  (Table(newState, TreeWithCards(psc, actionBuilder)), true)
                }
                case None => notAttached
              }
            }
            case _ => notAttached
          }
          case treeWithCards: TreeWithCards => pc match {
            case pcit: PlayedCardInTree[_] => {
              val transformerBuilder = buildersFactory.createTransformerBuilder(pcit)
              transformerBuilder(state) match {
                case Some(transformer) => {
                  val newState = GameState.removeCardFromPlayersHand(pc.player, pc.card, state)
                  (Table(newState, treeWithCards.attachPlayedCard(pcit, transformerBuilder)), true)
                }
                case None => notAttached
              }
            }
            case _ => notAttached
          }
        }
      else
        notAttached
    } catch {
      case e: Exception => {
        notAttached
      }
    }
  }

  lazy val evaluate: GameState = {

    def evalTree(node: CardInnerNode): Option[ActionTransformer] = {
      node.transformerBuilder(state) match {
        case Some(transformer) => {
          val newTransformer = node.children.map(evalTree _).foldLeft(transformer)((parentTransformer: ActionTransformer, childTransformerOpt: Option[ActionTransformer]) =>
            childTransformerOpt match {
              case Some(childTransformer) =>
                childTransformer.transform(parentTransformer) match {
                  case Some(transformer) => transformer
                  case None => parentTransformer
                }
              case None => parentTransformer
            })
          Some(newTransformer)
        }
        case None => None
      }
    }

    tree match {
      case EmptyTree => state
      case tree: TreeWithCards => {
        tree.actionBuilder(state) match {
          case Some(action) => {
            val newAction = tree.children.map(evalTree _).foldLeft(action)((action: Action, transformerOpt: Option[ActionTransformer]) =>
              transformerOpt match {
                case Some(transformer) =>
                  transformer.transform(action) match {
                    case Some(action) => action
                    case None => action
                  }
                case None => action
              })
            newAction.state
          }
          case None => state
        }
      }
    }
  }
}
