package game.standardtrach

import scala.reflect.classTag

import game.core.actions._
import game.core.PlayedCardInTree
import game.core.GameState
import game.core.PlayedCardAtPlayer
import game.core.Card
import game.core.PlayedStartingCard
import game.core.PlayedCard

import game.standardtrach.actions.attacks._

package object actions {
  
  object actionFactory extends ActionFactory { 

    def createAction[C <: Card](pc: PlayedStartingCard[C])(implicit state: GameState): Option[CardAction[pc.T, _ <: PlayedStartingCard[pc.T]]] = pc match {
      case pc: PlayedCardAtPlayer[AttackCard @unchecked] if pc.card.tag == classTag[AttackCard] => Some(new Attack(pc))
      case pc: PlayedCardAtPlayer[MassedAttackCard @unchecked] if pc.card.tag == classTag[MassedAttackCard] => Some(new MassedAttack(pc))
      case _ => None
    }

    def createTransformer[C <: Card](pc: PlayedCardInTree[C])(implicit state: GameState): Option[ActionCardTransformer[pc.T, _ <: PlayedCardInTree[pc.T]]] = pc match {
      case pc: PlayedCardInTree[DefenceCard @unchecked] if pc.card.tag == classTag[DefenceCard] => Some(new Defence(pc))
      case pc: PlayedCardInTree[PriorityIncrementerCard @unchecked] if pc.card.tag == classTag[PriorityIncrementerCard] => Some(new PriorityIncrementer(pc))
      case _ => None
    }
  }
}