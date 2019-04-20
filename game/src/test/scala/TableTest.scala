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
    
    // assert that player cannot play a card which he does not own
    val (table1, attached1) = table.attachCard(PlayedCardAtPlayerRequest(ac.id, p2.id, p1.id))
    assert(!attached1)
    
    // assert that player cannot play a notstarting card at the beginnig
    val (table2, attached2) = table1.attachCard(PlayedCardInTreeRequest(dc.id, p2.id, ac.id))
    assert(!attached2)
    
    // p1 plays his ac at p2
    val (table3, attached3) = table2.attachCard(PlayedCardAtPlayerRequest(ac.id, p1.id, p2.id))
    assert(attached3)
    assert(!table3.state.player(p1).hand.cards.contains(ac))

    // assert that now p2 cannot play starting card
    val (table4, attached4) = table3.attachCard(PlayedCardAtPlayerRequest(ac2.id, p2.id, p1.id))
    assert(!attached4)
    
    // p1 increases priority of attack
    val (table5, attached5) = table4.attachCard(PlayedCardInTreeRequest(pic.id, p1.id, ac.id))
    assert(attached5)
    
    // p2's health should have got damage
    val state5 = table5.evaluate
    assert(state5.player(p2).health.value == p2.health.value - 1)
  }
}
