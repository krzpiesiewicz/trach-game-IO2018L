import org.scalatest.FunSuite
import com.typesafe.scalalogging.Logger

import game.Logging.logger
import game.example._
import game.core._
import game.core.Card.CardId

class GameTest extends FunSuite {
  
  test("CardFactory test") {
    implicit val cardFactory = new DefaultCardFactory
    
    val c0 = Card[AttackCard]()
    logger.info(s"${c0.id}, ${c0.tag}")
    
    val c1 = Card[CatCard](id = 5)
    logger.info(s"${c1.id}, ${c1.tag}")
    
    val c2 = Card[CatCard]()
    logger.info(s"${c2.id}, ${c2.tag}")
  }
}
