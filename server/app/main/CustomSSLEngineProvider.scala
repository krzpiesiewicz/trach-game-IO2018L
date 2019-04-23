package main

import javax.net.ssl._
import play.core.ApplicationProvider
import play.server.api._

import play.api.Logging

class CustomSSLEngineProvider(appProvider: ApplicationProvider) extends SSLEngineProvider with Logging {

  override def createSSLEngine(): SSLEngine = {
    val sslContext = SSLContext.getDefault
    val engine = sslContext.createSSLEngine
    
    logger.info(engine.getSupportedProtocols().foldLeft[String]("protocols:") {
      case (buff, next) => buff + " " + next
    })
    
    logger.info(engine.getSupportedCipherSuites.foldLeft[String]("cipher suites:") {
      case (buff, next) => buff + " " + next
    })
    
    engine
  }
}