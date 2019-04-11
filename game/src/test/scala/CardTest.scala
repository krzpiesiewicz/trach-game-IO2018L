import org.scalatest.FunSuite
import com.typesafe.scalalogging.Logger

import scala.reflect.classTag

import game.Logging.logger

import game.core._
import game.core.Card.CardId

import game.standardtrach._

final class ExampleCardWithArgs protected (val arg1: Int, val arg2: String) extends TypedCard[ExampleCardWithArgs] {
  override def toString = s"Card(id=$id, $tag, arg1=$arg1, arg2=$arg2)"
}

class CardTest extends FunSuite {
  
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
  
  test("CardMap") {
    implicit val cardFactory = new DefaultCardFactory
    
    assert(Cards.tagForName("shelter") == Some(classTag[ShelterCard]))
    
    assert(Cards.nameOf(Card[MassedAttackCard]()) == "mass_attack")
  }
}

