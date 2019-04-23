import org.scalatest._

import com.typesafe.scalalogging.Logger

import game.Logging.logger

import game.core._
import game.core.actions._

import game.standardtrach._
import game.standardtrach.actions._
import game.standardtrach.actions.attacks._
import game.standardtrach.actions.PriorityIncrementer
import game.standardtrach.DefaultAttributes._
import game.gameplay._

class TableTest extends FunSuite {
  
  test("Table test - attack") {
    import DafaultData.data1._
       
    // assert that player cannot play a notstarting card at the beginnig
    val (table2, attached2) = table.attach(CardInnerNode(PlayedCardInTree(dc, p2, ac)))
    assert(!attached2)
    
    // p1 plays his ac at p2
    val (table3, attached3) = table2.attach(TreeWithCards(PlayedCardAtPlayer(ac, p1, p2)))
    assert(attached3)
    assert(!table3.state.player(p1).hand.cards.contains(ac))

    // assert that now p2 cannot play starting card
    val (table4, attached4) = table3.attach(TreeWithCards(PlayedCardAtPlayer(ac2, p2, p1)))
    assert(!attached4)
    
    // p1 increases priority of attack
    val (table5, attached5) = table4.attach(CardInnerNode(PlayedCardInTree(pic, p1, ac)))
    assert(attached5)
    
    // p2's health should have got damage
    val state5e = table5.evaluate
    assert(state5e.player(p2).health.value == p2.health.value - 1)
    
    // p2 cannot play a simple defence against attack with increased priority
    val (table6, attached6) = table5.attach(CardInnerNode(PlayedCardInTree(dc, p2, ac)))
    assert(!attached6)
    
    // but now in another galaxy, p2 played dc with pic2 to defend himself
    val (table7, attached7) = table6.attach(CardInnerNode(PlayedCardInTree(dc, p2, ac)).attach(PlayedCardInTree(pic2, p2, dc)))
    assert(attached7)
    
    // p2's health should not have got damage 
    val state7e = table7.evaluate
    assert(state7e.player(p2).health.value == p2.health.value)
  }
}
