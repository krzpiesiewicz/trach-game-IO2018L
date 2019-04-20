package game.core

import scala.language.implicitConversions
import game.core.Card.CardId
import game.core.Player.PlayerId
import game.core._

/**
 * PlayedCardRequest is a type for communication between the game logic server and external users.
 * On the server side it is mapped to PlayedCard according to the game state.
 */
trait PlayedCardRequest {
  def cardId: CardId
  def playerId: PlayerId
}

trait PlayedStartingCardRequest extends PlayedCardRequest

case class PlayedCardAtPlayerRequest(cardId: CardId, playerId: PlayerId, targetPlayerId: PlayerId) extends PlayedStartingCardRequest

case class PlayedCardAtActiveCardRequest(cardId: CardId, playerId: PlayerId, activeCardId: CardId) extends PlayedStartingCardRequest

case class PlayedCardInTreeRequest(cardId: CardId, playerId: PlayerId, parentCardId: CardId) extends PlayedCardRequest

object PlayedCardRequest {
  /**
   * It checks if a request is correct according to a game state. It means checking if all ids can be mapped for existing objects
   * (throws an exception if something does not exist in game state).
   * It does not check if the player owns the card, nor other similar constraints!
   */
  implicit def toPlayedCard(pcr: PlayedCardRequest)(implicit state: GameState) = pcr match {
    case pcr: PlayedCardAtPlayerRequest => toPlayedCardAtPlayer(pcr)
    case pcr: PlayedCardAtActiveCardRequest => toPlayedCardAtActiveCard(pcr)
    case pcr: PlayedCardInTreeRequest => toPlayedCardInTree(pcr)
  }

  implicit def toPlayedCardAtPlayer(pcr: PlayedCardAtPlayerRequest)(implicit state: GameState) = new PlayedCardAtPlayer(
    state.card(pcr.cardId),
    state.player(pcr.playerId),
    state.player(pcr.targetPlayerId))

  implicit def toPlayedCardAtActiveCard(pcr: PlayedCardAtActiveCardRequest)(implicit state: GameState) = new PlayedCardAtActiveCard(
    state.card(pcr.cardId),
    state.player(pcr.playerId),
    state.card(pcr.activeCardId))

  implicit def toPlayedCardInTree(pcr: PlayedCardInTreeRequest)(implicit state: GameState) = new PlayedCardInTree(
    state.card(pcr.cardId),
    state.player(pcr.playerId),
    state.card(pcr.parentCardId))
}