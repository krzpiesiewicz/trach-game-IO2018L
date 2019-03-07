package game.abstracts

import Character.CharacterId

class CircleOfCharacters(val charactersOrder: Array[Character]) {

  lazy val characters: Map[CharacterId, Character] = charactersOrder.foldLeft[Map[CharacterId, Character]](Map.empty) {
    case (map, char) => map + (char.id -> char)
  }
  
  def map(fun: (Character) => Character) = new CircleOfCharacters(charactersOrder map fun)

  def nextTo(character: Character): Character = charactersOrder(index(index(character), 1))
  def prevTo(character: Character): Character = charactersOrder(index(index(character), -1))

  private def index(idx: Int, change: Int) = (idx + change) % charactersOrder.length
  
  protected def index(character: Character) = charactersIndexes.get(character.id) match {
    case Some(idx) => idx
    case None => throw new Exception(s"Character of id=${character.id} not in the cirlce.")
  }
  
  protected lazy val charactersIndexes: Map[CharacterId, Int] = charactersOrder.zipWithIndex.foldLeft[Map[CharacterId, Int]](Map.empty) {
    case (map, (char, idx)) => map + (char.id -> idx)
  }
}
