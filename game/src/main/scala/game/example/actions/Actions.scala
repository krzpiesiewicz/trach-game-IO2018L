package game.example.actions

import game.core._
import game.example.AttackCard
import game.example.DefenceCard

import game.Logging.logger
import game.example.MassedAttackCard

object Actions {
  
  trait AttackAction extends Action {
    def target: Player
  }
  
  trait MassedAttackAction extends Action {
    def targets: Seq[Player]
    def withoutTarget(target: Player): MassedAttackAction
  }
  
  class DefaultMassedAttackAction(val targets: Seq[Player])(implicit initialState: GameState) extends MassedAttackAction { 
    def withoutTarget(target: Player): MassedAttackAction = new DefaultMassedAttackAction(targets.filterNot(_ == target))
    
    def state = initialState
  }
  
  class Attack(played: PlayedCardAtPlayer[AttackCard])(implicit initialState: GameState)
    extends CardAction[AttackCard, PlayedCardAtPlayer[AttackCard]] with AttackAction {
    
    def state = initialState
    
    val target = played.targetPlayer
  }
  
  class MassedAttack(played: PlayedStartingCard[MassedAttackCard])(implicit initialState: GameState)
    extends CardAction[AttackCard, PlayedCardAtPlayer[AttackCard]] with MassedAttackAction {
    
    def state = initialState
    
    val targets = initialState.playersMap.values.toSeq
    
    def withoutTarget(target: Player) = new DefaultMassedAttackAction(targets).withoutTarget(target)
  }

  class Defence(pcit: PlayedCardInTree[DefenceCard])(implicit initialState: GameState)
    extends ActionCardTransformer[DefenceCard, PlayedCardInTree[DefenceCard]] {

    def transform(action: Action) = action match {
      case attack: AttackAction => Some(NoneAction(initialState))
      case massedAttack: MassedAttackAction => Some(massedAttack.withoutTarget(pcit.player))
      case _ => None 
    }
  }

}