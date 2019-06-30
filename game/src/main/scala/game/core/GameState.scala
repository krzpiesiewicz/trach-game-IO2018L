package game.core

import game.core.Card.CardId
import game.core.Player.PlayerId
import game.core.Attribute.AttributeTransformer

trait GameState {
  def attributes: AttributesSet[GlobalAttribute]
  
  lazy val playersMap = attributes.forceGet[Players].circleOfPlayers.playersMap
  
  lazy val cardsMap = attributes.forceGet[AllCards].cardsMap
  
  lazy val cardsStacks = attributes.forceGet[CardsStacks]
  
  lazy val globalActiveCards = attributes.forceGet[GlobalActiveCards]
  
  lazy val roundsManager = attributes.forceGet[RoundsManager]
  
  lazy val cardTrees = attributes.forceGet[CardTrees]
  
  /**
   * Throws an exception if there is no card of id = cardObj.id.
   */
  def card(cardObj: Card): Card = card(cardObj.id)
  
  /**
   * Throws an exception if there is no card of id = cardId.
   */
  def card(cardId: CardId): Card = cardsMap.get(cardId).get
  
  /**
   * Throws an exception if there is no player of id = playerObj.id.
   */
  def player(playerObj: Player): Player = player(playerObj.id)
  
  /**
   * Throws an exception if there is no player of id = playerId.
   */
  def player(playerId: PlayerId): Player = playersMap.get(playerId).get
  
  /**
   * Returns a new gamestate with attributes transformed by given transformer.
   */
  def transformed(transformer: AttributeTransformer[GlobalAttribute]): GameState
  
  /**
   * Returns a state with the next round (the round of the next player).
   */
  def withNextRound: GameState = this transformed {
    case rm: RoundsManager => rm.withNextRound(attributes.forceGet[Players].circleOfPlayers)
  }
  
  /**
   * Checks if it is beginning of the round.
   */
  def isBeginningOfTheRound: Boolean = cardTrees.trees.isEmpty
}

case class NormalState(override val attributes: AttributesSet[GlobalAttribute]) extends GameState {
  def transformed(transformer: AttributeTransformer[GlobalAttribute]) = {
    val normalState = NormalState(attributes.transformed(transformer))
    val alivePlayers = normalState.playersMap.values.filter(!_.isDead).toSeq
    
    if (alivePlayers.length >= 2)
      normalState
    else if (alivePlayers.isEmpty)
      NoOneAlive(normalState.attributes)
    else // only one player alive
      GameWin(normalState.attributes, alivePlayers.head)
  }
}

trait EndState extends GameState {
  def transformed(transformer: AttributeTransformer[GlobalAttribute]) = this
}

case class NoOneAlive(override val attributes: AttributesSet[GlobalAttribute]) extends EndState

case class GameWin(override val attributes: AttributesSet[GlobalAttribute], winner: Player) extends EndState
