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

/** Provides implementations of actions relating to cards of the standard version of Trach Game */
package object actions {

  implicit object buildersFactory extends BuildersFactory {

    def createActionBuilder[C <: Card](pc: PlayedStartingCard[C]): ActionBuilder = pc match {
      case pc: PlayedCardAtPlayer[AttackCard @unchecked] if pc.card.tag == classTag[AttackCard] =>
        { implicit state: GameState => tryOpt(new Attack(pc)) }

      case pc: PlayedCardAtPlayer[MassedAttackCard @unchecked] if pc.card.tag == classTag[MassedAttackCard] =>
        { implicit state: GameState => tryOpt(new MassedAttack(pc)) }

      case _ =>
        { implicit state: GameState => None }
    }

    def createTransformerBuilder[C <: Card](pc: PlayedCardInTree[C]): ActionTransformerBuilder = pc match {
      case pc: PlayedCardInTree[DefenceCard @unchecked] if pc.card.tag == classTag[DefenceCard] =>
        { implicit state: GameState => tryOpt(new Defence(pc)) }

      case pc: PlayedCardInTree[PriorityIncrementerCard @unchecked] if pc.card.tag == classTag[PriorityIncrementerCard] =>
        { implicit state: GameState => tryOpt(new PriorityIncrementer(pc)) }

      case _ =>
        { implicit state: GameState => None }
    }

    /**
     * Returns Some(t) if no exception thrown during evaluating t otherwise None.
     */
    private def tryOpt[T](t: => T) = try {
      Some(t)
    } catch {
      case _: Exception => None
    }
  }
}