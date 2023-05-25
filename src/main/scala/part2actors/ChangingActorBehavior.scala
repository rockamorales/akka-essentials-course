package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChangingActorBehavior extends App {
  object Mom {
    case class Food(food: String)
    case class Ask(message: String)
    case class MomStart(kidRef: ActorRef)
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }

  object FussyKid {
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
  }

  class FussyKid extends Actor {
    import FussyKid._
    import Mom._
    var state = HAPPY
    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) =>
        if (state == HAPPY) sender() ! KidAccept
        else sender() ! KidReject
    }
  }

  class StatelessFussyKid extends Actor {
    import FussyKid._
    import Mom._
    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false)
      case Food(CHOCOLATE) =>
      case Ask(_) => sender() ! KidAccept
    }
    def sadReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false)
      case Food(CHOCOLATE) => context.unbecome()
      case Ask(_) => sender() ! KidReject
    }
  }

  class Mom extends Actor {
    import Mom._
    import FussyKid._
    override def receive: Receive = {
      case MomStart(kidRef) =>
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Ask("Do you want to play?")
      case KidAccept => println("Yay, my kid is happy!")
      case KidReject => println("My kid is sad")
    }
  }

  import Mom._
  val system = ActorSystem("changingActorBehaviorDemo")
  val fussyKid = system.actorOf(Props[FussyKid])
  val statelessFussyKid = system.actorOf(Props[StatelessFussyKid])
  val mom = system.actorOf(Props[Mom])
  mom ! MomStart(statelessFussyKid)

  // context.become can receive a second option parameter: true/false -- true - discard old state - default; false - stack old states


  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }
  // word count actor
  class CounterActor extends Actor {
    import Counter._
    def receive: PartialFunction[Any, Unit] = {
      countReceive(0)
    }

    def countReceive(currentCount: Int): Receive = {
      case Increment =>
        println(s"[$currentCount] incrementing")
        context.become(countReceive(currentCount + 1))
      case Decrement =>
        println(s"[$currentCount] decrement")
        context.become(countReceive(currentCount - 1))
      case Print => println(s"[counter] count: $currentCount")

    }



  }

  import Counter._
  val counter = system.actorOf(Props[CounterActor], "myCounter")

  import Counter._

  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 3).foreach(_ => counter ! Decrement)
  counter ! Print


  case class Vote (candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {
//    var candidate: Option[String] = None

    override def receive: Receive = {
      case Vote(c) => context.become(voted(c))
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }

    def voted(candidate: String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])
  class VoteAggregator extends Actor {
    override def receive: Receive = receiveWithState(Set(), Map())
    def receiveWithState(stillWaiting: Set[ActorRef], currentStats: Map[String, Int]): Receive = {
      case AggregateVotes(citizens) =>
        citizens.foreach { citizenRef =>
          citizenRef ! VoteStatusRequest
        }
        context.become(receiveWithState(citizens, currentStats))
      case VoteStatusReply(None) =>
        sender() ! VoteStatusRequest
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidate = currentStats.getOrElse(candidate, 0)

        val newCurrentStats = currentStats + (candidate -> (currentVotesOfCandidate + 1))
        if (newStillWaiting.isEmpty) {
          println(s"[aggregator] poll stats: $newCurrentStats")
        } else {
          context.become(receiveWithState(newStillWaiting, newCurrentStats))
        }

    }
  }

  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = system.actorOf(Props[VoteAggregator])
  Thread.sleep(1000)
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))

}
