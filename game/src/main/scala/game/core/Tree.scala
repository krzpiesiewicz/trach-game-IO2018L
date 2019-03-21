package game.core

trait Builder {
  def compose(another: Builder): Builder
}

trait Tree[T, B <: Builder] {
  def evaluate: B
}

case class Node[T, B <: Builder](children: Seq[Tree[T, B]], builder: B) extends Tree[T, B] {
  
  def evaluate: B = {
    children.foldLeft(builder) {(rootBuilder, tree) => tree.evaluate}
  }
}