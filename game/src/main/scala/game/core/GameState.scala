package game.core

import game.core.Card.CardId
import game.core.Player.PlayerId
import game.core.Attribute.AttributeTransformer

trait GameState {
  def attributes: AttributesSet[GlobalAttribute]
  
  lazy val playersMap = attributes.forceGet[Players].circleOfPlayers.playersMap
  
  lazy val cardsMap = attributes.forceGet[AllCards].cardsMap
  
  lazy val coveredCardsStack = attributes.forceGet[CoveredCardsStack]
  
  lazy val discardedCardsStack = attributes.forceGet[DiscardedCardsStack]
  
  lazy val globalActiveCards = attributes.forceGet[GlobalActiveCards]
  
  lazy val roundsManager = attributes.forceGet[RoundsManager]
  
  /**
   * Throws an exception if there is no card of id = cardId.
   */
  def card(cardId: CardId): Card = cardsMap.get(cardId).get
  
  /**
   * Throws an exception if there is no player of id = playerId.
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
}

case class NormalState(override val attributes: AttributesSet[GlobalAttribute]) extends GameState {
  def transformed(transformer: AttributeTransformer[GlobalAttribute]) = new NormalState(attributes.transformed(transformer))
}

trait EndState extends GameState

object GameState {
  
  def removeCardFromPlayersHand(player: Player, card: Card, state: GameState): GameState = {
    val p = state.player(player)
    if (p.hand.cards contains card) {
      val coveredStack = state.attributes.forceGet[CoveredCardsStack]
      val (poppedCardOpt, restOfStack) = coveredStack.pop match {
        case Some((poppedCard, restOfStack)) => (Some(poppedCard), restOfStack)
        case None => (None, coveredStack)
      }
      state transformed {
        case stack: CoveredCardsStack => restOfStack;
        case players: Players => players.updatePlayer(p.transformed {
          case hand: Hand => hand.replacedCard(card, poppedCardOpt);
        });
      }
    } else
      state
  }
}