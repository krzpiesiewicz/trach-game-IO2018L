package game.core

import scala.language.implicitConversions
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
  
  type T <: Card
  val tag: ClassTag[T]
  
  def canEqual(a: Any) = a.isInstanceOf[Card]

  override def equals(a: Any) = a match {
    case c: Card => c.canEqual(this) && tag == c.tag && id == c.id
    case _ => false 
  }
}

class TypedCard[C <: Card]()(implicit tg: ClassTag[C]) extends Card {
  override type T = C
  override val tag = tg
  private def card: C = this.asInstanceOf[C]
}

object TypedCard {
  implicit def apply(card: Card): TypedCard[card.T] = card
  
  implicit def toC[C <: Card](typedCard: TypedCard[C]): C = typedCard.card
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
