package game.core

import game.core.Card.CardId
import game.core.Player.PlayerId

trait GameState {
  def attributes: AttributesSet[GlobalAttribute]
  
  lazy val playersMap = attributes.forceGet[Players].circleOfPlayers.playersMap
  
  lazy val cardsMap = attributes.forceGet[AllCards].cardsMap
  
  /**
   * Throws an exception if there is no card of id = cardId.
   */
  def card(cardId: CardId): Card = cardsMap.get(cardId).get
  
  /**
   * Throws an exception if there is no player of id = playerId.
   */
  def player(playerId: PlayerId): Player = playersMap.get(playerId).get
}

class InitialState(players: Players, roundsManager: RoundsManager) extends GameState {
  val attributes = AttributesSet(Seq(players, roundsManager))
}

class NormalState(override val attributes: AttributesSet[GlobalAttribute]) extends GameState

trait EndState extends GameState