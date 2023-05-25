package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChildActorsExercise.WordCounterMaster.{Initialize, WordCountReply, WordCountTask}

object ChildActorsExercise extends App {
  // Distributed word counting

  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(text: String)
    case class WordCountReply(count: Int)
  }
  class WordCounterMaster extends Actor {
    import WordCounterMaster._
    override def receive: Receive = {
      case Initialize(n) =>
        val children = (0 until n).map(x => context.actorOf(Props[WordCounterWorker], s"children-$x")).toList
        context.become(initialized(children, 0, 0))
    }

    def initialized(children: List[ActorRef], currentChildIndex: Int, wordCount: Int): Receive = {
      case WordCountTask(text) =>
        children(currentChildIndex) ! text
        val nextIndex = (currentChildIndex + 1) % children.length
        context.become(initialized(children, nextIndex, wordCount))
      case WordCountReply(count) =>
        val newWordCount = wordCount + count
        println(s"[master] word count: $newWordCount")
        context.become(initialized(children, currentChildIndex, newWordCount))
    }
  }

  class WordCounterWorker extends Actor {
    override def receive: Receive = {
      case text: String =>
        println(s"[${self.path.name}]counting words for text: $text")
        sender() ! WordCountReply(text.split(" ").length)
    }
  }

  val system = ActorSystem("WordCounter")
  val wordCounterMaster = system.actorOf(Props[WordCounterMaster], "wcmaster")
  wordCounterMaster ! Initialize(10)
  wordCounterMaster ! WordCountTask("create a WordCouterMater")
  wordCounterMaster ! WordCountTask("send Initialize(10) to wordCounterMaster")
  wordCounterMaster ! WordCountTask("send \"Akka is awesome\" to wordCounterMaster")
  wordCounterMaster ! WordCountTask("wcm will send a WordCountTask(\"...\") to one of its children")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("master replies with 3 to the sender")
  wordCounterMaster ! WordCountTask("use round robin logic")

  /**
   * create a WordCouterMater
   * send Initialize(10) to wordCounterMaster
   * send "Akka is awesome" to wordCounterMaster
   *   wcm will send a WordCountTask("...") to one of its children
   *      child replies with With a WordCountReply(3) to the master
   *   master replies with 3 to the sender
   *
   *   use round robin logic
   */
}
