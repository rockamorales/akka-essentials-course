package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {

  //part 1 - actor systems
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  //part2 create actors
  // actors are uniquely identified
  // messages are passed and processed asynchronously
  // Each actor may respond differently
  // Actors are (really) encapsulated

  // word count actor
  class WordCountActor extends Actor {
    var totalWords = 0

    def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        println(s"[word counter] I have received: $message")
        totalWords += message.split(" ").length
      case msg => println(s"[word counter] I cannot understand ${msg.toString}")
    }

  }

  // part3 - instantiating our actor
  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")

  // part 4 - communicate!
  wordCounter ! "I am learning Akka and it's pretty damn cool!" // ! is also known as "tell"
  wordCounter ! "A different message"
  // Asynchronous

  // with constructor arguments
  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"Hi, my name is $name")
    }
  }

  // legal but discouraged
  val person = actorSystem.actorOf(Props(new Person("Bob")))

  person ! "hi"

  // the recommended method is to create a companion Object with a factory method for Props

  object Person {
    def props(name: String) = Props(new Person(name))
  }

}
