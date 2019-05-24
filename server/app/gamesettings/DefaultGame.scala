package gamesettings

import game.core._
import game.standardtrach._
import game.standardtrach.DefaultAttributes._

object DefaultGame {
  
  lazy val multiplayerTemporaryGameState: GameState = {
    implicit val cardFactory = new DefaultCardFactory
    
    val p1Cards = Seq(
        Card[AttackCard](),
        Card[AttackCard](),
        Card[PriorityIncrementerCard](),
        Card[DefenceCard](),
        Card[PriorityIncrementerCard]())
        
    val p1 = Player(1, DefaultAttributesSet(Seq(
        DefaultHand(cards = p1Cards),
        DefaultHealth(),
        DefaultActiveCards())))
        
    val p2Cards = Seq(
        Card[DefenceCard](),
        Card[AttackCard](),
        Card[PriorityIncrementerCard](),
        Card[DefenceCard](),
        Card[DefenceCard]())
        
    val p2 = Player(2, DefaultAttributesSet(Seq(
        DefaultHand(cards = p2Cards),
        DefaultHealth(),
        DefaultActiveCards())))
        
    val p3Cards = Seq(
        Card[AttackCard](),
        Card[AttackCard](),
        Card[PriorityIncrementerCard](),
        Card[PriorityIncrementerCard](),
        Card[DefenceCard]())
        
    val p3 = Player(3, DefaultAttributesSet(Seq(
        DefaultHand(cards = p3Cards),
        DefaultHealth(),
        DefaultActiveCards())))
    
    val circle = CircleOfPlayers(Array(p1, p2, p3))
//    val circle = CircleOfPlayers(Array(p1, p2))
    
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
    
    val cards = p1Cards ++ p2Cards ++ p3Cards ++ coveredCards
    
    val state = NormalState(new DefaultAttributesSet(Seq(
        DefaultPlayers(circle),
        DefaultAllCards(cards),
        DefaultCoveredCardsStack(coveredCards.toVector),
        DefaultDiscardedCardsStack(Vector.empty),
        DefaultGlobalActiveCards(Vector.empty),
        DefaultRoundsManager(p1),
        CardTrees()
        )))
    
    state
  }
}
