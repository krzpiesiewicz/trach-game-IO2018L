package game.core

import scala.reflect.ClassTag

import com.google.inject.AbstractModule
import com.google.inject.Inject

import Card._
import com.google.inject.name._
import com.google.inject._

trait Card {
  @Inject
  @Named("id")
  val id: CardId = 0
  
  val code: CardCode
  
  def canEqual(a: Any) = a.isInstanceOf[Card]

  override def equals(a: Any) = a match {
    case c: Card => c.canEqual(this) && code == c.code
    case _ => false 
  }
}

trait CardFactory {
  def createId: CardId
}

class DefaultCardFactory extends CardFactory {
  var lastId: CardId = 0
  
  def createId: CardId = {
    lastId += 1
    lastId
  }
}

object Card {
  type CardId = Int
  type CardCode = String
  
  class MyModule(val cardId: CardId) extends AbstractModule {
    override def configure(): Unit = {
      bind(classOf[CardId]).annotatedWith(Names.named("id")).toInstance(cardId)
    }
  }
  
  def apply[C <: Card](id: CardId, args: Object*)(implicit tag: ClassTag[C]): C = {
    val injector = Guice.createInjector(new MyModule(id));
    val constructor = tag.runtimeClass.getConstructors()(0)
    val instance = constructor.newInstance(args: _*).asInstanceOf[C]
    injector.injectMembers(instance)
    instance
  }
  
  def apply[C <: Card](args: Object*)(implicit tag: ClassTag[C], cardFactory: CardFactory): C = {
    val id = cardFactory.createId
    apply(id = id, args: _*)
  }
}

trait CardWithPriority extends Card {
  val priority: Int
}

trait StartingCard extends Card

trait AttributeCard extends Card

trait ModificationCard extends Card

trait OffensiveCard extends Card

trait DefensiveCard[C <: OffensiveCard] extends Card

abstract class DefaultCard(val code: CardCode) extends Card

abstract class DefaultCardWithPriority(val code: CardCode, val priority: Int) extends Card

