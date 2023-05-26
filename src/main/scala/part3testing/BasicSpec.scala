package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.BeforeAndAfterAll
import part3testing.BasicSpec.{Blackhole, LabTestActor, SimpleActor}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.math.random
import scala.util.Random

class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  // Setup
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  //General structure of a test
  "a simple actor" should {
    "send back the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "hello, test"
      echoActor ! message
      expectMsg(message)
    }
  }

  "A blackhole actor" should  {
    "send back some message" in {
      val blackhole = system.actorOf(Props[Blackhole])
      val message = "hello, test"
      blackhole ! message
      expectNoMessage(1 second)
    }
  }

  "A lab test actor" should {
    val labTestActor = system.actorOf(Props[LabTestActor])
    "turn a string into uppercase" in {
      labTestActor ! "I love akka"
      val reply = expectMsgType[String]
      assert(reply == "I LOVE AKKA")
//      expectMsg("I LOVE AKKA")
    }

    "reply to a greeting" in {
      labTestActor ! "greeting"
      expectMsgAnyOf("hi", "hello")
    }

    "reply with favorite tech" in {
      labTestActor ! "favoriteTech"
      expectMsgAllOf("Scala", "Akka")
    }

    "reply with cool tech in a different way" in {
      labTestActor ! "favoriteTech"
      val messages = receiveN(2) //Seq[Any]

      //free to do more complicated assertions
    }

    "reply with cool tech in a fancy way" in {
      labTestActor ! "favoriteTech"
      expectMsgPF() {
        case "Scala" =>
        case "Akka" =>
      }
    }
  }

  // message assertions

}

// recommended
object BasicSpec {
  // store all methods and values you might need for your test

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message => sender() ! message
    }
  }

  class Blackhole extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabTestActor extends Actor {
    val random = new Random()
    override def receive: Receive = {
      case "greeting" =>
        if (random.nextBoolean()) sender() ! "hi" else sender() ! "hello"
      case "favoriteTech" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case message: String => sender() ! message.toUpperCase()
    }
  }
}
