package game.core

import scala.reflect.ClassTag
import game.core.Attribute.AttributeTransformer

trait AttributesSet[A <: Attribute] {

  def get[T <: A]()(implicit tag: ClassTag[T]): Option[T]

  def forceGet[T <: A]()(implicit tag: ClassTag[T]): T = get[T] match {
    case Some(t) => t
    case None => throw new Exception(s"AttributesSet does not contain attribute extending $tag.")
  }

  def transformed(transformer: AttributeTransformer[A]): AttributesSet[A]
}

object AttributesSet {
  def apply[A <: Attribute](attributes: Seq[A]) = new DefaultAttributesSet(attributes)
}

case class DefaultAttributesSet[A <: Attribute](attributes: Seq[A]) extends AttributesSet[A] {

  def get[T <: A]()(implicit tag: ClassTag[T]) = attributes collectFirst { case t: T => t }

  def transformed(transformer: AttributeTransformer[A]) = new DefaultAttributesSet(attributes map { transformer.applyOrElse(_, {a: A => a}) })
}