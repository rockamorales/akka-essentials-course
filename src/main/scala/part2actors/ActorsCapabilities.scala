package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorsCapabilities extends App {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi" => context.sender() ! "Hello there!"
      case message: String => println(s"[${context.self}] I have received $message")
      case number: Int => println(s"[simple actor] I have received a NUMBER: $number")
      case SpecialMessage(contents) => println(s"I have received something SPECIAL: $contents")
      case SendMessageToYourSelf(content) => self ! content
      case SayHiTo(ref) => ref ! "Hi!"
      case WirelessPhoneMessage(content, ref) => ref forward content + "s" // keeps the original message
    }
  }

  val system = ActorSystem("actorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")
  simpleActor ! "hello, actor"

  // 1 - messages can be of any type
  // a) messages must be IMMUTABLE
  // b) messages must be SERIALIZABLE
  // in practice use case classes and case objects
  simpleActor ! 42

  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("Some special content")


  // 2 - actors have information about their context and about themself
  //   we can also use self member instead of context.self

  case class SendMessageToYourSelf(content: String)
  simpleActor ! SendMessageToYourSelf("I am an actor...")

  // 3 - actors can reply to messages
  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")
  case class SayHiTo(ref: ActorRef)
  alice ! SayHiTo(bob)

  // context.sender() or sender() contains the reference to the actor that sends the last message

  // who is the sender for this application ( which is not an actor)
  // 4 - dead letters
  alice ! "Hi!" // reply to "me"

  //5 - actors can forward messages
  // forwarding - sending a message with the original sender
  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  alice ! WirelessPhoneMessage("Hi", bob)

  /**
   * Exercises
   * 1. A Counter actor
   *    - Increment
   *    - Decrement
   *    - Print
   *
   * 2. Bank account as an actor
   *    - Deposit an amount
   *    - Withdraw and amount
   *    - Statement
   *
   *    interact with some other kind of actor
   */

  // DOMAIN of the counter
  case object Counter {
    case object Increment
    case object Decrement
    case object Print
  }
  class Counter extends Actor {
    import Counter._
    var count = 0
    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print => println(s"[counter] My current count is $count")
    }
  }

  val counter = system.actorOf(Props[Counter], "myCounter")

  import Counter._
  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 3).foreach(_ => counter ! Decrement)
  counter ! Print

  object BankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object Statement
    case class TransactionSuccess(message: String)
    case class TransactionFailure(reason: String)
  }
  // bank account
  class BankAccount extends Actor {
    import BankAccount._
    var funds = 0

    override def receive: Receive = {
      case Deposit(amount) =>
        if (amount < 0) sender() ! TransactionFailure("Invalid deposit amount")
        else {
          funds += amount
        }
        sender() ! TransactionSuccess(s"successfully deposited $amount")

      case Withdraw(amount) =>
        if (amount < 0) sender() ! TransactionFailure("Invalid withdraw amount")
        else if (amount > funds) sender()! TransactionFailure("Insufficient funds")
        else sender() ! TransactionSuccess(s"successfully withdraw $amount")
      case Statement => sender() ! s"Your balance is $funds"
    }
  }

  object Person {
    case class LiveTheLife(account: ActorRef)
  }

  class Person extends Actor {
    import Person._
    import BankAccount._

    override def receive: Receive = {
      case LiveTheLife(account) =>
        account ! Deposit(1000)
        account ! Withdraw(9000)
        account ! Withdraw(500)
      case message => println(message.toString)
    }

  }

  import Person._
  val account = system.actorOf(Props[BankAccount], "bankAccount")
  val person = system.actorOf(Props[Person], "billionaire")
  person ! LiveTheLife(account)

  //
}
