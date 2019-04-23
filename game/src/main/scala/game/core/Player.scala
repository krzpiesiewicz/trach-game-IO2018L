package game.core

import Player.PlayerId
import game.core.Attribute.AttributeTransformer

case class Player(val id: PlayerId, val attributes: AttributesSet[PlayerAttribute]) {
  
  def canEqual(a: Any) = a.isInstanceOf[Player]

  override def equals(a: Any) = a match {
    case p: Player => p.canEqual(this) && id == p.id
    case _ => false 
  }
  
  override def toString = s"Player(id=$id)"
  
  lazy val hand = attributes.forceGet[Hand]
  
  lazy val health = attributes.forceGet[Health]
  
  lazy val activeCards = attributes.forceGet[PlayerActiveCards]
  
  lazy val targetChooser = attributes.forceGet[TargetChooser]
  
  /**
   * Returns a player with attributes transformed by given transformer.
   */
  def transformed(transformer: AttributeTransformer[PlayerAttribute]): Player = Player(id, attributes.transformed(transformer))
  
  def owns(card: Card): Boolean = hand.cards contains card
}

object Player {
  type PlayerId = Int
  
  def apply(id: PlayerId, attributes: AttributesSet[PlayerAttribute]) = new Player(id, attributes)
}
