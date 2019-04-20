package game

import com.typesafe.scalalogging._
import org.slf4j.LoggerFactory

object Logging extends LazyLogging {
  override lazy val logger = Logger(LoggerFactory.getLogger(this.getClass))
}