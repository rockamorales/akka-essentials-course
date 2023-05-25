package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object IntroAkkaConfig extends App {
  class SimpleLoggingActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message =>
        log.info(message.toString)
        log.debug(message.toString)
    }
  }

  /**
   * 1 - inline configuration
   */

  val configString =
    """
      | akka {
      |   logLevel = "DEBUG"
      | }
      |""".stripMargin

  val config = ConfigFactory.parseString(configString)
  val system = ActorSystem("ConfigurationDemo", ConfigFactory.load(config))
  val actor = system.actorOf(Props[SimpleLoggingActor])

  actor ! "A Message to remember"


  /**
   * 2. Configuration in a file
   */

  val defaultConfigFileSystem = ActorSystem("DefaultConfigFileDemo")
  val defaultConfigActor = defaultConfigFileSystem.actorOf(Props[SimpleLoggingActor])

  defaultConfigActor ! "remember me"


  /**
   * 3 - separate configuration in the same file
   */

  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val specialConfigSystem = ActorSystem("SpecialConfigDemo", specialConfig)
  val specialConfigActor = specialConfigSystem.actorOf(Props[SimpleLoggingActor])
  specialConfigActor ! "Remember me, I'm special"

  /**
   * 4. Separate config in another file
   */

  val separateConfig = ConfigFactory.load("secretFolder/secretConfiguration.conf")
  println(s"separate config log level ${separateConfig.getString("akka.loglevel")}")

  /**
   * 5 - Different file formats
   * JSON, Properties files are other format options
   */
  val jsonConfig = ConfigFactory.load("json/jsonConfig.json")
  println(s"json config: ${jsonConfig.getString("aJsonProperty")}")
  println(s"json config: ${jsonConfig.getString("akka.loglevel")}")

}
