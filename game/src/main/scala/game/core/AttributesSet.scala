package game.core

import scala.reflect.ClassTag

trait AttributesSet[A <: Attribute] {

  def get[T <: A]()(implicit tag: ClassTag[T]): Option[T]

  def forceGet[T <: A]()(implicit tag: ClassTag[T]): T = get[T] match {
    case Some(t) => t
    case None => throw new Exception(s"AttributesSet does not contain attribute extending $tag.")
  }

  def apply(builder: AttributeBuilder[A]): AttributesSet[A]
}

object AttributesSet {
  def apply[A <: Attribute](attributes: Seq[A]) = new DefaultAttributesSet(attributes)
}

class DefaultAttributesSet[A <: Attribute](attributes: Seq[A]) extends AttributesSet[A] {

  def get[T <: A]()(implicit tag: ClassTag[T]) = attributes collectFirst {case t: T => t}

  def apply(builder: AttributeBuilder[A]) = new DefaultAttributesSet(attributes map { a => builder(a) })
}