import game.core._
import game.core.actions._

import game.standardtrach._
import game.standardtrach.actions._
import game.standardtrach.actions.attacks._
import game.standardtrach.actions.PriorityIncrementer
import game.standardtrach.DefaultAttributes._
import game.gameplay._

object DafaultData {
  object data1 {
    implicit val cardFactory = new DefaultCardFactory
    
    val ac = Card[AttackCard]()
    val ac2 = Card[AttackCard]()
    val dc = Card[DefenceCard]()
    val pic = Card[PriorityIncrementerCard]()
    val pic2 = Card[PriorityIncrementerCard]()
    
    val cards = Seq(ac, pic, dc, ac2, pic2)
    
    val p1 = Player(1, DefaultAttributesSet(Seq(
        DefaultHand(cards = Seq(ac, pic)),
        DefaultHealth(),
        DefaultActiveCards())))
    val p2 = Player(2, DefaultAttributesSet(Seq(
        DefaultHand(cards = Seq(dc, ac2, pic2)),
        DefaultHealth(),
        DefaultActiveCards())))
    
    val circle = CircleOfPlayers(Array(p1, p2))
    
    val state = NormalState(new DefaultAttributesSet(Seq(
        DefaultPlayers(circle),
        DefaultAllCards(cards),
        DefaultCoveredCardsStack(Vector.empty),
        DefaultDiscardedCardsStack(Vector.empty),
        DefaultGlobalActiveCards(Vector.empty)
        )))
    
    val table = Table(state)
  }
}