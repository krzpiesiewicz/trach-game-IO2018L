package game.example

import game.abstracts._
import game.abstracts.Card.CardId

final class AtomicButtonCard protected () extends DefaultCardWithPriority("AtomicButton", 1) with OffensiveCard with StartingCard

final class AttackCard protected () extends DefaultCardWithPriority("Attack", 4) with OffensiveCard with StartingCard

final class MassedAttackCard protected () extends DefaultCardWithPriority("MassedAttack", 4) with OffensiveCard with StartingCard

final class ShelterCard protected () extends DefaultCardWithPriority("Shelter", 1) with DefensiveCard[AtomicButtonCard] with StartingCard

final class DefenceCard protected () extends DefaultCardWithPriority("Defence", 4) with DefensiveCard[AttackCard]

final class ReversalCard protected () extends DefaultCardWithPriority("Reversal", 4) with DefensiveCard[AttackCard]

final class PriorityIncrementerCard protected () extends DefaultCard("PriorityIncrementer") with ModificationCard

final class TrenningCard protected () extends DefaultCard("Trenning") with AttributeCard with StartingCard

final class CatCard protected () extends DefaultCard("Cat") with AttributeCard with StartingCard
