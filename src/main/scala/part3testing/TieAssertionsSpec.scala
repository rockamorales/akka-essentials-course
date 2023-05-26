package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.util.Random
import scala.concurrent.duration._
import scala.language.postfixOps

class TimedAssertionsSpec extends TestKit(
    ActorSystem("TimedAssertionSpec", ConfigFactory.load().getConfig("specialTimedAssertionsConfig")))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll
{
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TimedAssertionsSpec._
  "A worker actor" should {
    val worker = system.actorOf(Props[WorkerActor])
    "reply with the meaning of life in timely manner" in {
      within(500 millis, 1 second) {
        worker ! "work"
        expectMsg(WorkResult(42))
      }
    }
    "reply with valid work at a reasonable cadence" in {
      within(1 second) {
        worker ! "workSequence"
        val results: Seq[Int] = receiveWhile[Int](max=2 seconds, idle=500 millis, messages=10) {
          case WorkResult(result) => result
        }
        assert(results.sum > 5)
      }
    }

    // failed using testProbe
    "reply to a test probe in a timely manner" in {
      within(1 second) {
        val probe = TestProbe()
        probe.send(worker, "work")
        probe.expectMsg(WorkResult(42))
      }
    }
  }
}

object TimedAssertionsSpec {
  // testing scenario
  case class WorkResult(result: Int)
  class WorkerActor extends Actor {
    override def receive: Receive = {
      case "work" =>
        Thread.sleep(500)
        sender() ! WorkResult(42)
      case "workSequence" =>
        val r = new Random()
        for (i <- 1 to 10) {
          Thread.sleep(r.nextInt(50))
          sender() ! WorkResult(1)
        }
    }
  }
}
