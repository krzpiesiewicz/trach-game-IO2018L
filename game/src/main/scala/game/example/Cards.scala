package game.example

import game.core._

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
