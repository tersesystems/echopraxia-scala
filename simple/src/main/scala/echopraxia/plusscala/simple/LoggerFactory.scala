package echopraxia.plusscala.simple

import echopraxia.logging.spi.Caller
import echopraxia.logging.spi.CoreLoggerFactory

object LoggerFactory {

  def getLogger(): Logger = getLogger(Caller.resolveClassName())

  def getLogger(clazz: Class[?]): Logger = getLogger(clazz.getName)

  def getLogger(name: String): Logger = {
    val core = CoreLoggerFactory.getLogger(classOf[Logger].getName, name)
    new Logger(core)
  }
}
