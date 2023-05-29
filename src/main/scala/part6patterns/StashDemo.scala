package part6patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}

object StashDemo extends App {
  /**
   * ResourceActor
   *  - open => it can receive read/write requests to the resource
   *  - otherwise it will postpone all read/write requests until the state is open
   *
   *  ResourceActor is closed
   *    -Open => switch to open state
   *    - Read, Write messages are POSTPONED
   *
   *  ResourceActor is open
   *    - Read, Write are handled
   *    - Close => switch to the closed state
   *
   *  [Open, Read, Read, Write]
   *    - switch to the open state
   *    - read the data
   *    - read the data
   *    - write the data
   *
   *  [Read, Open, Write]
   *    - stash Read
   *      Stash: [Read]
   *    - open => switch to open state
   *      Mailbox: [Read, Write]
   *    - read and write are handled
   */

  case object Open
  case object Close
  case object Read
  case class Write(data: String)

  class ResourceActor extends Actor with ActorLogging with Stash {
    private var innerData: String = ""

    override def receive: Receive = closed

    def closed: Receive = {
      case Open =>
        log.info("Opening resource")
        unstashAll()
        context.become(open)
      case message =>
        log.info(s"Stashing $message because I can't handle it in the closed state")
        stash()
    }

    def open: Receive = {
      case Read =>
        log.info(s"I have read the $innerData")
      case Write(data) =>
        log.info(s"I'm writing $data")
        innerData = data
      case Close =>
        log.info("Closing resource")
        context.become(closed)
        unstashAll()
      case message =>
        log.info(s"Stashing $message because I can't handle it in the open state")
        stash()
    }

  }

  val system = ActorSystem("StashDemo")
  val resourceActor = system.actorOf(Props[ResourceActor])
  resourceActor ! Read // stashed
  resourceActor ! Open // switch open; I have read ""
  resourceActor ! Open // stashed
  resourceActor ! Write("I Love stash") // i am writing I love stash
  resourceActor ! Close // switch to close; switch open
  resourceActor ! Read // I have read I love stash


}
