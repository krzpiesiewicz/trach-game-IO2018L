import game.core._
import game.core.actions._

import game.standardtrach._
import game.standardtrach.actions._
import game.standardtrach.actions.attacks._
import game.standardtrach.actions.PriorityIncrementer
import game.standardtrach.DefaultAttributes._

object DafaultData {
  object data1 {
    implicit val cardFactory = new DefaultCardFactory
    
    val ac = Card[AttackCard]()
    val ac2 = Card[AttackCard]()
    val dc = Card[DefenceCard]()
    val pic = Card[PriorityIncrementerCard]()
    val pic2 = Card[PriorityIncrementerCard]()
    val shc = Card[ShelterCard]()
    
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
        DefaultCoveredCardsStack(Vector(shc)),
        DefaultDiscardedCardsStack(Vector.empty),
        DefaultGlobalActiveCards(Vector.empty),
        DefaultRoundsManager(p1)
        )))
    
    val table = Table(state)
  }
  
  object multiplayerTemporaryData {
    implicit val cardFactory = new DefaultCardFactory
    
    val ac = Card[AttackCard]()
    
    val p1Cards = Seq(
        ac,
        Card[AttackCard](),
        Card[PriorityIncrementerCard](),
        Card[DefenceCard](),
        Card[PriorityIncrementerCard]())
        
    val p1 = Player(1, DefaultAttributesSet(Seq(
        DefaultHand(cards = p1Cards),
        DefaultHealth(),
        DefaultActiveCards())))
        
    val dc = Card[DefenceCard]()
    val pic = Card[PriorityIncrementerCard]()
    
    val p2Cards = Seq(
        dc,
        Card[AttackCard](),
        pic,
        Card[DefenceCard](),
        Card[DefenceCard]())
        
    val p2 = Player(2, DefaultAttributesSet(Seq(
        DefaultHand(cards = p2Cards),
        DefaultHealth(),
        DefaultActiveCards())))
        
//    val p3Cards = Seq(
//        Card[PriorityIncrementerCard](),
//        Card[PriorityIncrementerCard](),
//        Card[PriorityIncrementerCard](),
//        Card[PriorityIncrementerCard](),
//        Card[DefenceCard]())
//        
//    val p3 = Player(3, DefaultAttributesSet(Seq(
//        DefaultHand(cards = p3Cards),
//        DefaultHealth(),
//        DefaultActiveCards())))
    
//    val circle = CircleOfPlayers(Array(p1, p2, p3))
    val circle = CircleOfPlayers(Array(p1, p2))
    
    val coveredCards = Seq(
        Card[AttackCard](),
        Card[DefenceCard](),
        Card[AttackCard](),
        Card[AttackCard](),
        Card[PriorityIncrementerCard](),
        Card[AttackCard](),
        Card[AttackCard](),
        Card[DefenceCard](),
        Card[AttackCard](),
        Card[PriorityIncrementerCard](),
        Card[DefenceCard](),
        Card[DefenceCard]())
    
    val cards = p1Cards ++ p2Cards /*++ p3Cards*/ ++ coveredCards
    
    val state = NormalState(new DefaultAttributesSet(Seq(
        DefaultPlayers(circle),
        DefaultAllCards(cards),
        DefaultCoveredCardsStack(coveredCards.toVector),
        DefaultDiscardedCardsStack(Vector.empty),
        DefaultGlobalActiveCards(Vector.empty),
        DefaultRoundsManager(p1)
        )))
        
     val table = Table(state)
  }
}