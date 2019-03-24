package game.example.actions

import game.Logging.logger

import game.core._
import game.core.actions._
import game.example.AttackCard
import game.example.DefenceCard
import game.example.MassedAttackCard

package object attacks {

  /**
   * The smallest part of attack logic.
   * @target is a player to be attacked,
   * @from is a player who should be attacked when reversal played,
   * @damage is a number of health points that the target's health should be decreased.
   * @priority describes this small attack unit priority
   */
  class AttackUnit(val target: Player, val from: Player, val damage: Int, val priority: Int) {
    
    def withNewPiority(newPriority: Int): AttackUnit = new AttackUnit(target, from, damage, newPriority)
  }

  /**
   * Simple attack action is composed of one attack unit.
   */
  trait AttackAction extends ActionWithPriority {
    def unit: AttackUnit
  }

  /**
   * Massed attack action is composed of several attack units.
   */
  trait MassedAttackAction extends ActionWithPriority {
    def units: Seq[AttackUnit]

    /**
     * Returns MassedAttackAction that has @unitToTranform transformed by @transform function.
     * 	  - if transformation result is Some(newAttackUnit), @unitToTranform is replaced with newAttackUnit in units,
     * 		- if transformation result is None, @unitToTranform is omitted in units.
     */
    def withTransformedAttackUnit(unitToTranform: AttackUnit, transformation: AttackUnit => Option[AttackUnit]): MassedAttackAction
  }

  class Attack(
      played: PlayedCardAtPlayer[AttackCard],
      priorityOpt: Option[Int] = None,
      unitOpt: Option[AttackUnit] = None)(implicit initialState: GameState)
    extends CardAction[AttackCard, PlayedCardAtPlayer[AttackCard]] with AttackAction {
    {
      if (played.targetPlayer == played.player)
        throw new Exception("Player cannot target himself/herself")
    }

    //TODO
    def state = initialState
    
    val priority = priorityOpt match {
      case Some(priority) => priority
      case None => played.card.priority
    }

    val unit = unitOpt match {
      case Some(unit) => unit
      case None => new AttackUnit(played.targetPlayer, played.player, 1, priority)
    }
    
    def withPriority(newPriority: Int) = new Attack(played, Some(newPriority), Some(unit.withNewPiority(newPriority)))
  }

  class MassedAttack(
      played: PlayedStartingCard[MassedAttackCard],
      priorityOpt: Option[Int] = None,
      unitsOpt: Option[Seq[AttackUnit]] = None)(implicit initialState: GameState)
    extends CardAction[AttackCard, PlayedCardAtPlayer[AttackCard]] with MassedAttackAction {

    //TODO
    def state = initialState
    
    val priority = priorityOpt match {
      case Some(priority) => priority
      case None => played.card.priority
    }

    val units = unitsOpt match {
      case Some(units) => units
      case None => { // default attack units are targeted at all players excluding the massed attack initiator.
        initialState.playersMap.values.filterNot(_ == played.player).toSeq map {
          target => new AttackUnit(target, played.player, 1, priority)
        }
      }
    }

    def withTransformedAttackUnit(unitToTranform: AttackUnit, transformation: AttackUnit => Option[AttackUnit]) = new MassedAttack(
      played,
      Some(priority),
      Some(replaceTransformedAttackUnit(units, unitToTranform, transformation)))
    
    // for now changing priority results in changing priorities of all unit attacks
    def withPriority(newPriority: Int) = new MassedAttack(played, Some(newPriority), Some(units map {_.withNewPiority(newPriority)}))
  }

  class Defence(
      played: PlayedCardInTree[DefenceCard],
      priorityOpt: Option[Int] = None)(implicit initialState: GameState)
    extends ActionCardTransformer[DefenceCard, PlayedCardInTree[DefenceCard]] with ActionTransformerWithPriority {

    val priority = priorityOpt match {
      case Some(priority) => priority
      case None => played.card.priority
    }
    
    def transform(action: Action) = action match {
      case attack: AttackAction => {
        if (attack.unit.priority < priority)
          None
        else
          Some(new NoneAction(initialState) with CardAction[AttackCard, PlayedCardAtPlayer[AttackCard]])
      }
      case massedAttack: MassedAttackAction => {
        //TODO allowing the player to choose who is defended (maybe by server query to client)
        // At the moment player can defend only himself/herself.

        findAttackUnitTargetedAtPlayer(massedAttack.units, played.player) match {
          case None => None
          case Some(unit) => {
            if (unit.priority < priority)
              None
            else
              Some(massedAttack.withTransformedAttackUnit(unit, { u => None }))
          }
        }
      }
      case _ => None
    }
    
    /**
     * Returns None because Defence cannot modify another transformer.
     */
    def transform(transformer: ActionTransformer) = None
    
    def withPriority(newPriority: Int) = new Defence(played, Some(newPriority))
  }

  /**
   * Finds the first attackUnit from @units targeted at the player.
   * Returns Some(attackUnit) if any satisfiable exists.
   * Otherwise, returns None.
   */
  private def findAttackUnitTargetedAtPlayer(units: Seq[AttackUnit], target: Player) = units find { _.target == target }

  /**
   * Transforms @unitToTranform by @transform function:
   * 		- if transformation result is Some(newAttackUnit), returns @units where @unitToTranform is replaced with newAttackUnit,
   * 		- if transformation result is None, returns @units whithout @unitToTranform.
   */
  private def replaceTransformedAttackUnit(units: Seq[AttackUnit], unitToTranform: AttackUnit, transformation: AttackUnit => Option[AttackUnit]): Seq[AttackUnit] = {
    val unitsWithoutUnit = units.filterNot(_ == unitToTranform)
    transformation(unitToTranform) match {
      case Some(newUnit) => unitsWithoutUnit :+ newUnit
      case None => unitsWithoutUnit
    }
  }
}