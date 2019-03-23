package game.core

import scala.language.existentials
import scala.language.implicitConversions
import scala.reflect.ClassTag

import game.core.Card.CardId
import game.core.Player.PlayerId

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

/**
 * PlayedCard describes a player's try to play a card. 
 */
trait PlayedCard[C <: Card] {
  def card: C
  def player: Player
  type T = C
}

import TypedCard._

protected abstract class PlayedCardClass[C <: Card] (c: TypedCard[C], val player: Player) {
  val card: C = c
}

trait PlayedStartingCard[C <: Card] extends PlayedCard[C]

final class PlayedCardAtPlayer[C <: Card](card: TypedCard[C], player: Player, val targetPlayer: Player)
extends PlayedCardClass[C](card, player) with PlayedStartingCard[C]

final class PlayedCardAtActiveCard[C <: Card](card: TypedCard[C], player: Player, val activeCard: Card)
extends PlayedCardClass[C](card, player) with PlayedStartingCard[C]

final class PlayedCardInTree[C <: Card](card: TypedCard[C], player: Player, val parentCard: Card)
extends PlayedCardClass[C](card, player) with PlayedCard[C]

object PlayedCard {
  
  /**
   * It checks if a request is correct according to a game state. It means checking if all ids can be mapped for existing objects.
   * It does not check if the player owns the card, nor other similar constraints. 
   */
  implicit def apply(pcr: PlayedCardRequest)(implicit state: GameState) = pcr match {
    case pcr: PlayedCardAtPlayerRequest => fromPlayedCardAtPlayerRequest(pcr)
    case pcr: PlayedCardAtActiveCardRequest => fromPlayedCardAtActiveCardRequest(pcr)
    case pcr: PlayedCardInTreeRequest => fromPlayedCardInTreeRequest(pcr)
  }
  
  implicit def fromPlayedCardAtPlayerRequest(pcr: PlayedCardAtPlayerRequest)(implicit state: GameState) = new PlayedCardAtPlayer(
    state.card(pcr.cardId),
    state.player(pcr.playerId),
    state.player(pcr.targetPlayerId)
  )
  
  implicit def fromPlayedCardAtActiveCardRequest(pcr: PlayedCardAtActiveCardRequest)(implicit state: GameState) = new PlayedCardAtActiveCard(
    state.card(pcr.cardId),
    state.player(pcr.playerId),
    state.card(pcr.activeCardId)
  )
  
  implicit def fromPlayedCardInTreeRequest(pcr: PlayedCardInTreeRequest)(implicit state: GameState) = new PlayedCardInTree(
    state.card(pcr.cardId),
    state.player(pcr.playerId),
    state.card(pcr.parentCardId)
  )
}
