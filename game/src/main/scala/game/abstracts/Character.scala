package game.abstracts

import Character.CharacterId

trait Character {
  val id: CharacterId
  
  def attributes: AttributesSet[CharacterAttribute]
  
  def canEqual(a: Any) = a.isInstanceOf[Character]

  override def equals(a: Any) = a match {
    case c: Character => c.canEqual(this) && id == c.id
    case _ => false 
  }
}

object Character {
  type CharacterId = Int
}
