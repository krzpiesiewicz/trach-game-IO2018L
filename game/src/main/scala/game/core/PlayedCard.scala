package game.core

import scala.reflect.ClassTag

/**
 * PlayedCard describes a player's try to play a card.
 */
trait PlayedCard[C <: Card] {
  def card: C
  def player: Player
  type T = C
}

trait PlayedStartingCard[C <: Card] extends PlayedCard[C]

case class PlayedCardAtPlayer[C <: Card](val card: C, val player: Player, val targetPlayer: Player)
  extends PlayedStartingCard[C]

case class PlayedCardAtActiveCard[C <: Card](val card: C, val player: Player, val activeCard: Card)
  extends PlayedStartingCard[C]

case class PlayedCardInTree[C <: Card](val card: C, val player: Player, val parentCard: Card)
  extends PlayedCard[C]
