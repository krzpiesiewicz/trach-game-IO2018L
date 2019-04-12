package game.core

import Player.PlayerId

case class CircleOfPlayers(val playersOrder: Array[Player]) {

  lazy val playersMap: Map[PlayerId, Player] = playersOrder.foldLeft[Map[PlayerId, Player]](Map.empty) {
    case (map, player) => map + (player.id -> player)
  }
  
  def map(fun: (Player) => Player) = new CircleOfPlayers(playersOrder map fun)

  def nextTo(player: Player): Player = playersOrder(index(index(player), 1))
  def prevTo(player: Player): Player = playersOrder(index(index(player), -1))
  
  def updatePlayer(player: Player) = new CircleOfPlayers(playersOrder map {p => if(p == player) player else p})

  private def index(idx: Int, change: Int) = (idx + change) % playersOrder.length
  
  protected def index(player: Player) = playersIndexesMap.get(player.id) match {
    case Some(idx) => idx
    case None => throw new Exception(s"Player of id=${player.id} not in the cirlce.")
  }
  
  protected lazy val playersIndexesMap: Map[PlayerId, Int] = playersOrder.zipWithIndex.foldLeft[Map[PlayerId, Int]](Map.empty) {
    case (map, (player, idx)) => map + (player.id -> idx)
  }
}
