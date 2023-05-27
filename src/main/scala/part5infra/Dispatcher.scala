package part5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object Dispatcher extends App {
  class Counter extends Actor with ActorLogging {
    var count = 0

    override def receive: Receive = {
      case message =>
        count += 1
        log.info(s"[$count] $message")
    }
  }

  val system = ActorSystem("DispatchersDemo")
  // ConfigFactory.load().getConfig("dispatchersDemo")

  // method #1 - Programmatic/in code
  val actors = for (i <- 1 to 10) yield system.actorOf(Props[Counter].withDispatcher("my-dispatcher"), s"counter_${i}")
  val r = new Random()
//  for (i <- 1 to 1000) {
//    actors(r.nextInt(10)) ! i
//  }


  // method #2 - from config
  val rtjvmActor = system.actorOf(Props[Counter], "rtjvm")

  /**
   * Dispatchers implement the ExecutionContext trait
   */

  class DBActor extends Actor with ActorLogging {
    //using a dedicated dispatcher
    implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("my-dispatcher")
      //context.system.dispatchers.lookup("my-dispatcher")
      //context.dispatcher

    override def receive: Receive = {
      case message => Future {
        Thread.sleep(5000)
        log.info(s"Success: $message")
      }
    }
  }

  val dbActor = system.actorOf(Props[DBActor])
  dbActor ! "The meaning of life is 42"

  val nonBlockingActor = system.actorOf(Props[Counter])
  for (i <- 1 to 1000) {
    val message = s"important message ${i}"
    dbActor ! message
    nonBlockingActor ! message
  }
}
