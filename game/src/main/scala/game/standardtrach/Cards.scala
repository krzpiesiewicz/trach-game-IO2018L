package game.standardtrach

import game.core._
import scala.reflect.ClassTag

final class AtomicButtonCard protected () extends TypedCard[AtomicButtonCard] with CardWithPriority {
  val priority = 1
}

final class AttackCard protected () extends TypedCard[AttackCard] with CardWithPriority {
  val priority = 4
}

final class MassedAttackCard protected () extends TypedCard[MassedAttackCard] with CardWithPriority {
  val priority = 4
}

final class ShelterCard protected () extends TypedCard[ShelterCard] with CardWithPriority {
  val priority = 1
}

final class DefenceCard protected () extends TypedCard[DefenceCard] with CardWithPriority {
  val priority = 4
}

final class ReversalCard protected () extends TypedCard[ReversalCard] with CardWithPriority {
  val priority = 4
}

final class PriorityIncrementerCard protected () extends TypedCard[PriorityIncrementerCard]

final class TrenningCard protected () extends TypedCard[TrenningCard]

final class CatCard protected () extends TypedCard[CatCard]

object Cards {

  def nameOf(c: Card): String = cardsMap.tagsToNames.getOrElse(c.tag, "unknown_card")

  def tagForName(name: String): Option[ClassTag[_ <: Card]] = cardsMap.namesToTags.get(name)

  private val cardsMap = CardsMap().
    withCard[AtomicButtonCard]("atomic_bomb").
    withCard[AttackCard]("attack").
    withCard[MassedAttackCard]("mass_attack").
    withCard[ShelterCard]("shelter").
    withCard[DefenceCard]("defence").
    withCard[ReversalCard]("reflection").
    withCard[PriorityIncrementerCard]("priority_inc").
    withCard[TrenningCard]("trenning").
    withCard[CatCard]("cat")

  private case class CardsMap(val namesToTags: Map[String, ClassTag[_ <: Card]] = Map.empty, val tagsToNames: Map[ClassTag[_ <: Card], String] = Map.empty) {
    def withCard[C <: Card](name: String)(implicit tag: ClassTag[C]) = CardsMap(namesToTags + (name -> tag), tagsToNames + (tag -> name))
  }
}