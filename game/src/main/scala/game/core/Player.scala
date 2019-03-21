package game.core

import Player.PlayerId

trait Player {
  val id: PlayerId
  
  def attributes: AttributesSet[PlayerAttribute]
  
  def canEqual(a: Any) = a.isInstanceOf[Player]

  override def equals(a: Any) = a match {
    case c: Player => c.canEqual(this) && id == c.id
    case _ => false 
  }
}

object Player {
  type PlayerId = Int
}
