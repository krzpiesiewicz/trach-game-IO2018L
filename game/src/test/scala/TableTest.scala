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

class TableTest extends FunSuite {
  
  object data {
    implicit val cardFactory = new DefaultCardFactory
    
    val ac = Card[AttackCard]()
    val ac2 = Card[AttackCard]()
    val dc = Card[DefenceCard]()
    val pic = Card[PriorityIncrementerCard]()
    val pic2 = Card[PriorityIncrementerCard]()
    
    val cards = Seq(ac, pic, dc, ac2, pic2)
    
    val p1 = Player(1, DefaultAttributesSet(Seq(
        new DefaultHand(cards = Seq(ac, pic)),
        new DefaultHealth())))
    val p2 = Player(2, DefaultAttributesSet(Seq(
        new DefaultHand(cards = Seq(dc, ac2, pic2)),
        new DefaultHealth())))
    
    val circle = CircleOfPlayers(Array(p1, p2))
    
    val state = NormalState(new DefaultAttributesSet(Seq(
        DefaultPlayers(circle),
        DefaultAllCards(cards),
        DefaultCoveredCardsStack(Vector.empty))))
    
    val table = Table(state)
  }
  
  test("Table test - attack") {
    import data._
    
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
