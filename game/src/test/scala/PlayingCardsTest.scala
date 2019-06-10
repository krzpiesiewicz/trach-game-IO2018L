import org.scalatest._

import game.core._
import game.standardtrach.actions._
import game.standardtrach.actions.AllCardTreesEvaluation

class PlayingCardsTest extends FunSuite {
  
  test("attack, priority_inc, defence") {
    import DafaultData.data1._
       
    // assert that player cannot play a card at the beginning of another player's round.
    assertThrows[Exception](new PlayingCards(TreeWithCards(PlayedCardAtPlayer(ac2, p2, p1)), p2)(state).state)
    
    // assert that player cannot play a nonstarting card at the beginning.
    assertThrows[Exception](new PlayingCards(CardInnerNode(PlayedCardInTree(pic, p1, ac)), p2)(state).state)
    
    // p1 plays his ac at p2
    val state2 = new PlayingCards(TreeWithCards(PlayedCardAtPlayer(ac, p1, p2)), p1)(state).state
    assert(!state2.player(p1).hand.cards.contains(ac))
    
    // p1 increases priority of attack
    val state3 = new PlayingCards(CardInnerNode(PlayedCardInTree(pic, p1, ac)), p1)(state2).state

    // p2's health should have got damage after the tree evaluation
    val state4a = new AllCardTreesEvaluation(state3).state
    assert(state4a.player(p2).health.value == p2.health.value - 1)
    
    // p2 cannot play a simple defence against attack with increased priority
    assertThrows[Exception](new PlayingCards(CardInnerNode(PlayedCardInTree(dc, p2, ac)), p2)(state3).state)
    
    // but now in another galaxy, p2 played dc with pic2 to defend himself
    val state4b = new PlayingCards(
        CardInnerNode(PlayedCardInTree(dc, p2, ac)).attach(PlayedCardInTree(pic2, p2, dc)), p2)(state3).state
    
    // p2's health should not have got damage 
    assert(state4b.player(p2).health.value == p2.health.value)
  }
  
  test("attack, defence and priority_inc to defence") {
    import DafaultData.multiplayerTemporaryData._
    
    // p1 plays his ac at p2
    val state2 = new PlayingCards(TreeWithCards(PlayedCardAtPlayer(ac, p1, p2)), p1)(state).state
    
    // p2 plays his dc at ac in response
    val state3 = new PlayingCards(CardInnerNode(PlayedCardInTree(dc, p2, ac)), p2)(state2).state
    
    // p2 plays his pic at dc to increase priority of defence
    val state4 = new PlayingCards(CardInnerNode(PlayedCardInTree(pic, p2, dc)), p2)(state3).state
  }
  
  test("attack, priority_inc to attack and cannot defence to priority_inc") {
    import DafaultData.multiplayerTemporaryData._
    
    // p1 plays his ac at p2
    val state2 = new PlayingCards(TreeWithCards(PlayedCardAtPlayer(ac, p1, p2)), p1)(state).state
    
    // p2 plays pic at ac to increase priority of attack
    val state3 = new PlayingCards(CardInnerNode(PlayedCardInTree(pic, p2, ac)), p2)(state2).state
    
    // p2 cannot play dc at pic
    assertThrows[Exception](new PlayingCards(CardInnerNode(PlayedCardInTree(dc, p2, pic)), p2)(state3).state)
  }
}
