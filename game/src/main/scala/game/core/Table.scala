package game.core

import scala.language.existentials

import game.Logging.logger
import game.core.actions._


case class Table(val state: GameState, val tree: TreeOfCards = EmptyTree)(implicit buildersFactory: BuildersFactory) {

  def attachCard(pcr: PlayedCardRequest): (Table, Boolean) = {
    val notAttached: (Table, Boolean) = (this, false)
    try {
      val pc = PlayedCardRequest.toPlayedCard(pcr)(state)
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
                  (Table(newState, treeWithCards.attach(pcit, transformerBuilder)), true)
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
