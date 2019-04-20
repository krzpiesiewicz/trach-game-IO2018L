import org.scalatest.FunSuite
import com.typesafe.scalalogging.Logger

import game.Logging.logger

import game.core._
import game.core.actions._

import game.standardtrach._
import game.standardtrach.actions.attacks._
import game.standardtrach.actions.PriorityIncrementer
import game.standardtrach.actions.buildersFactory
import game.standardtrach.DefaultAttributes._

class ActionTest extends FunSuite {
  
  object data {
    implicit val cardFactory = new DefaultCardFactory
    
    val ac = Card[AttackCard]()
    val dc = Card[DefenceCard]()
    val pic = Card[PriorityIncrementerCard]()
    
    val p1 = Player(1, new DefaultAttributesSet(Seq(new DefaultHealth())))
    val p2 = Player(2, new DefaultAttributesSet(Seq(new DefaultHealth())))
    
    val circle = new CircleOfPlayers(Array(p1, p2))
    
    implicit val state = new NormalState(new DefaultAttributesSet(Seq(new DefaultPlayers(circle))))
  }
  
  test("Attack test") {
    import data._
    
    val attack: Action = new Attack(new PlayedCardAtPlayer(ac, p1, p2))
    
    val defence: ActionTransformer = new Defence(new PlayedCardInTree(dc, p2, ac))
    
    val priorityIncrementer: ActionTransformer = new PriorityIncrementer(new PlayedCardInTree(pic, p2, ac))
    
    // ordinary attack and defence
    val defOpt1 = defence.transform(attack)
    assert(defOpt1.isDefined)
    
    // an attack with incremented priority and an ordinary defence
    val strongerAttack = {
      val opt = priorityIncrementer.transform(attack)
      assert(opt.isDefined)
      opt.get
    }
    
    // asserting if the attack has decreased HP
    assert(strongerAttack.state.player(p2).health.value == p2.health.value - 1)
    
    val defOpt2 = defence.transform(strongerAttack)
    assert(defOpt2.isEmpty)
    
    // an attack with incremented priority and a defence with incremented priority
    val strongerDefence = {
      val opt = priorityIncrementer.transform(defence)
      assert(opt.isDefined)
      opt.get
    }
    
    val defendedAttack = {
      val defOpt3 = strongerDefence.transform(strongerAttack)
      assert(defOpt3.isDefined)
      defOpt3.get
    }
    // asserting if the attack has not decreased HP
    assert(defendedAttack.state.player(p2).health.value == p2.health.value)
  }
  
  test("BuildersFactory test") {
    import data._
    
    val attackOpt = buildersFactory.createActionBuilder(new PlayedCardAtPlayer(ac, p1, p2))(state)
    val defenceOpt = buildersFactory.createTransformerBuilder(new PlayedCardInTree(dc, p2, ac))(state)
    
    assert(attackOpt.isDefined)
    assert(defenceOpt.isDefined)
    
    val attack = attackOpt.get
    val defence = defenceOpt.get
    
    // ordinary attack and defence
    val defOpt1 = defence.transform(attack)
    assert(defOpt1.isDefined)
  }
}
