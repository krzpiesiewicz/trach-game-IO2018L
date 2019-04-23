package db

case class User(userId: Long, username: String)

object Database {
  
  private object Locker
  
  private var nextUserId: Long = 1
  
  private var nextGamePlayId: Long = 1
  
  def getFreeUserId(): Long = Locker.synchronized {
    val userId = nextUserId
    nextUserId = nextUserId + 1
    userId
  }
  
  def getFreeGamePlayId(): Long = Locker.synchronized {
    val gamePlayId = nextGamePlayId
    nextGamePlayId = nextGamePlayId + 1
    gamePlayId
  }
}