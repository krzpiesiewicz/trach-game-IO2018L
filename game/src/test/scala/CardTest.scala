import org.scalatest.FunSuite

import scala.reflect.classTag

import game.core._
import game.standardtrach._

final class ExampleCardWithArgs protected (val arg1: Int, val arg2: String) extends TypedCard[ExampleCardWithArgs] {
  override def toString = s"Card(id=$id, $tag, arg1=$arg1, arg2=$arg2)"
}

class CardTest extends FunSuite {
  
  test("CardFactory test") { 
    implicit val cardFactory = new DefaultCardFactory
    
    val c1 = Card[AttackCard]()
    assert(c1.id == 1 && c1.tag == classTag[AttackCard])
    
    val c5 = Card[CatCard](id = 5)()
    assert(c5.id == 5 && c5.tag == classTag[CatCard])
    
    val c2 = Card[CatCard]()
    assert(c2.id == 2)
    
    val c3 = Card(classTag[MassedAttackCard])()
    assert(c3.id == 3 && c3.tag == classTag[MassedAttackCard])
    
    val c10 = Card(classTag[MassedAttackCard], id = 10)()
    assert(c10.id == 10 && c10.tag == classTag[MassedAttackCard])
    
    val c4 = Card[ExampleCardWithArgs](100, "Hej c4")
    assert(c4.id == 4 && c4.tag == classTag[ExampleCardWithArgs] && c4.arg1 == 100 && c4.arg2 == "Hej c4")
    
    val c9 = Card[ExampleCardWithArgs](id = 9)(101, "Hej c9")
    assert(c9.id == 9 && c9.tag == classTag[ExampleCardWithArgs] && c9.arg1 == 101 && c9.arg2 == "Hej c9")
    
    val c6 = Card(classTag[ExampleCardWithArgs])(102, "Hej c6")
    assert(c6.id == 5 && c6.tag == classTag[ExampleCardWithArgs] && c6.arg1 == 102 && c6.arg2 == "Hej c6")
    
    val c20 = Card(classTag[ExampleCardWithArgs], id = 20)(120, "Hej c20")
    assert(c20.id == 20 && c20.tag == classTag[ExampleCardWithArgs] && c20.arg1 == 120 && c20.arg2 == "Hej c20")
  }
  
  test("CardMap") {
    implicit val cardFactory = new DefaultCardFactory
    
    assert(Cards.tagForName("shelter") == Some(classTag[ShelterCard]))
    
    assert(Cards.nameOf(Card[MassedAttackCard]()) == "mass_attack")
  }
}

