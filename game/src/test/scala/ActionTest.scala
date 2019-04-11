import org.scalatest.FunSuite
import com.typesafe.scalalogging.Logger

import game.Logging.logger

import game.core._
import game.core.actions._

import game.example._
import game.example.actions.attacks._
import game.example.DefaultAttributes.DefaultHand
import game.example.DefaultAttributes.DefaultPlayers
import game.example.actions.PriorityIncrementer


class ActionTest extends FunSuite {
  
  test("Action test") {
    implicit val cardFactory = new DefaultCardFactory
    
    val ac = Card[AttackCard]()
    val dc = Card[DefenceCard]()
    val pic = Card[PriorityIncrementerCard]()
    
    val p1 = Player(1, new DefaultAttributesSet(Seq(new DefaultHand(cards = Seq(ac)))))
    val p2 = Player(2, new DefaultAttributesSet(Seq(new DefaultHand(cards = Seq(dc)))))
    
    val circle = new CircleOfPlayers(Array(p1, p2))
    
    implicit val state = new NormalState(new DefaultAttributesSet(Seq(new DefaultPlayers(circle))))
    
    val attack: Action = new Attack(new PlayedCardAtPlayer(ac, p1, p2))
    
    val defence: ActionTransformer = new Defence(new PlayedCardInTree(dc, p2, ac))
    
    val priorityIncrementer: ActionTransformer = new PriorityIncrementer(new PlayedCardInTree(pic, p2, ac))
    
    // ordinary attack and defence
    val defOpt1 = defence.transform(attack)
    assert(defOpt1.isDefined)
    logger.info(s"defence.transform(attack) result: $defOpt1")
    
    // an attack with incremented priority and an ordinary defence
    val strongerAttack = {
      val opt = priorityIncrementer.transform(attack)
      assert(opt.isDefined)
      opt.get
    }
    val defOpt2 = defence.transform(strongerAttack)
    assert(defOpt2.isEmpty)
    logger.info(s"defence.transform(strongerAttack) result: $defOpt2")
    
    // an attack with incremented priority and a defence with incremented priority
    val strongerDefence = {
      val opt = priorityIncrementer.transform(defence)
      assert(opt.isDefined)
      opt.get
    }
    val defOpt3 = strongerDefence.transform(strongerAttack)
    assert(defOpt3.isDefined)
    logger.info(s"strongerDefence.transform(strongerAttack) result: $defOpt3")
  }
}
