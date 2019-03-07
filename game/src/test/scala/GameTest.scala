import org.scalatest.FunSuite
import com.typesafe.scalalogging.Logger

import game.Logging.logger
import game.example._
import game.abstracts._
import game.abstracts.Card.CardId
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.google.inject.Guice
import game.abstracts.Card.CardId

class GameTest extends FunSuite {
  
  implicit val cardFactory = new DefaultCardFactory
  
  test("simple game") {
    val c0 = Card[AttackCard]()
    logger.info(c0.id.toString())
    
    val c1 = Card[CatCard]()
    logger.info(c1.id.toString())
  }
}
