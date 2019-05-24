import org.scalatest._

import game.core._
import game.standardtrach.actions._

class TableTest extends FunSuite {
  
  test("Table test - attack") {
    import DafaultData.data1._
       
    // assert that player cannot play a notstarting card at the beginnig
    val (state2, attached2) = Table.attach(CardInnerNode(PlayedCardInTree(dc, p2, ac)), state)
    assert(!attached2)
    
    // p1 plays his ac at p2
    val (state3, attached3) = Table.attach(TreeWithCards(PlayedCardAtPlayer(ac, p1, p2)), state2)
    assert(attached3)
    assert(!state3.player(p1).hand.cards.contains(ac))
    
    // p1 increases priority of attack
    val (state4, attached4) = Table.attach(CardInnerNode(PlayedCardInTree(pic, p1, ac)), state3)
    assert(attached4)

    // p2's health should have got damage after the tree evaluation
    val state5a = Table.evaluate(state4)
    assert(state5a.player(p2).health.value == p2.health.value - 1)
    
    // p2 cannot play a simple defence against attack with increased priority
    val (state5, attached5) = Table.attach(CardInnerNode(PlayedCardInTree(dc, p2, ac)), state4)
    assert(!attached5)
    
    // but now in another galaxy, p2 played dc with pic2 to defend himself
    val (state6, attached6) = Table.attach(
        CardInnerNode(PlayedCardInTree(dc, p2, ac)).attach(PlayedCardInTree(pic2, p2, dc)), state5)
    assert(attached6)
    
    // p2's health should not have got damage 
    assert(state6.player(p2).health.value == p2.health.value)
  }
  
  test("Table test - attack, defence and priority_inc to defence") {
    import DafaultData.multiplayerTemporaryData._
    
    // p1 plays his ac at p2
    val (state2, attached2) = Table.attach(TreeWithCards(PlayedCardAtPlayer(ac, p1, p2)), state)
    assert(attached2)
    
    // p2 plays his dc at ac in response
    val (state3, attached3) = Table.attach(CardInnerNode(PlayedCardInTree(dc, p2, ac)), state2)
    assert(attached3)
    
    // p2 plays his pic at dc to increase priority of defence
    val (state4, attached4) = Table.attach(CardInnerNode(PlayedCardInTree(pic, p2, dc)), state3)
    assert(attached4)
  }
  
  test("Table test - attack, priority_inc to attack and cannot defence to priority_inc") {
    import DafaultData.multiplayerTemporaryData._
    
    // p1 plays his ac at p2
    val (state2, attached2) = Table.attach(TreeWithCards(PlayedCardAtPlayer(ac, p1, p2)), state)
    assert(attached2)
    
    // p2 plays pic at ac to increase priority of attack
    val (state3, attached3) = Table.attach(CardInnerNode(PlayedCardInTree(pic, p2, ac)), state2)
    assert(attached3)
    
    // p2 cannot play dc at pic
    val (state4, attached4) = Table.attach(CardInnerNode(PlayedCardInTree(dc, p2, pic)), state3)
    assert(!attached4)
  }
}
