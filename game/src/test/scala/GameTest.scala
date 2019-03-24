import org.scalatest.FunSuite
import com.typesafe.scalalogging.Logger

import scala.reflect.classTag

import game.Logging.logger

import game.core._
import game.core.actions._
import game.core.Card.CardId

import game.example._
import game.example.actions.attacks._
import game.example.DefaultAttributes.DefaultHand
import game.example.DefaultAttributes.DefaultPlayers
import game.example.actions.PriorityIncrementer

final class ExampleCardWithArgs protected (val arg1: Int, val arg2: String) extends TypedCard[ExampleCardWithArgs] {
  override def toString = s"Card(id=$id, $tag, arg1=$arg1, arg2=$arg2)"
}

class GameTest extends FunSuite {
  
  test("CardFactory test") { 
    implicit val cardFactory = new DefaultCardFactory
    
    val c1 = Card[AttackCard]()
    logger.info(s"$c1")
    
    val c5 = Card[CatCard](id = 5)()
    logger.info(s"$c5")
    
    val c2 = Card[CatCard]()
    logger.info(s"$c2")
    
    val c3 = Card(classTag[MassedAttackCard])()
    logger.info(s"$c3")
    
    val c10 = Card(classTag[MassedAttackCard], id = 10)()
    logger.info(s"$c10")
    
    val c4 = Card[ExampleCardWithArgs](100, "Hej c4")
    logger.info(s"$c4")
    
    val c9 = Card[ExampleCardWithArgs](id = 9)(100, "Hej c9")
    logger.info(s"$c9")
    
    val c6 = Card(classTag[ExampleCardWithArgs])(100, "Hej c6")
    logger.info(s"$c6")
    
    val c20 = Card(classTag[ExampleCardWithArgs], id = 20)(100, "Hej c20")
    logger.info(s"$c20")
  }
  
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
