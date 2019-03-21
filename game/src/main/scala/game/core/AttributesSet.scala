package game.core

import scala.reflect.ClassTag

trait AttributesSet[A <: Attribute] {

  def get[T <: A]()(implicit tag: ClassTag[T]): Option[A]

  def apply(builder: AttributeBuilder[A]): AttributesSet[A]
}

object AttributesSet {
  def apply[A <: Attribute](attributes: Seq[A]) = new DefaultAttributesSet(attributes)
}

class DefaultAttributesSet[A <: Attribute](attributes: Seq[A]) extends AttributesSet[A] {

  def get[T <: A]()(implicit tag: ClassTag[T]) = attributes find { a =>
    a match {
      case value: T => true
      case _ => false
    }
  }

  def apply(builder: AttributeBuilder[A]) = new DefaultAttributesSet(attributes map { a => builder(a) })
}