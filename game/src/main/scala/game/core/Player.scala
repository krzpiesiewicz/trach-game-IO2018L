package game.core

import Player.PlayerId

case class Player(id: PlayerId, attributes: AttributesSet[PlayerAttribute]) {
  
  def canEqual(a: Any) = a.isInstanceOf[Player]

  override def equals(a: Any) = a match {
    case c: Player => c.canEqual(this) && id == c.id
    case _ => false 
  }
  
  lazy val hand = attributes.forceGet[Hand]
}

object Player {
  type PlayerId = Int
}
