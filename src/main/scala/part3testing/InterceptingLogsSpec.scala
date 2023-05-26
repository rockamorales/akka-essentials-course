package part3testing

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import com.sun.security.jgss.AuthorizationDataEntry
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class InterceptingLogsSpec extends TestKit(ActorSystem("InterceptingLogsSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll
{

  override def afterAll(): Unit =
    TestKit.shutdownActorSystem(system)

  import InterceptingLogsSpec._
  val item = "Rock the JVM akka course"
  val creditCard= "1234-1234-1234-1234"
  "A checkout flow" should {
    "correctly load the dispatch of an order" in {
      EventFilter.info(pattern = s"Order [0-9]+ for item $item has been dispatched.", occurrences = 1) intercept {
        // our test code
        val checkoutRef = system.actorOf(Props[CheckoutActor])
        checkoutRef ! Checkout(item, creditCard)
      }
    }
  }
}


object InterceptingLogsSpec {

  case class DispatchOrder(item: String)
  case class Checkout(item: String, creditCard: String)
  case class AuthorizeCard(creditCard: String)
  case object PaymentAccepted
  case object PaymentDenied
  case object OrderConfirm
  
  class CheckoutActor extends Actor {
    private val paymentManager = context.actorOf(Props[PaymentManager])
    private val fulfillmentManager = context.actorOf(Props[FulfillmentManager])
    override def receive: Receive = ???


    def awaitingCheckout: Receive = {
      case Checkout(item, card) => 
        paymentManager ! AuthorizeCard(card)
        context.become(pendingPayment(item))
    }

    def pendingFulfillment(item: String): Receive = {
      case OrderConfirm => context.become(awaitingCheckout)
    }

    def pendingPayment(item: String): Receive = {
      case PaymentAccepted => 
        fulfillmentManager ! DispatchOrder(item)
        context.become(pendingFulfillment(item))
      case PaymentDenied =>
    }
    
  }

  class PaymentManager extends Actor {
    override def receive: Receive = {
      case AuthorizeCard(card) => 
        if (card.startsWith("0")) sender() ! PaymentDenied
        else sender() ! PaymentAccepted
    }
  }

  class FulfillmentManager extends Actor with ActorLogging{
    var orderId = 43
    override def receive: Receive = {
      case DispatchOrder(item: String) =>
        orderId += 1
        log.info(s"Order $orderId for item $item has been dispatched")
        sender() ! OrderConfirm
    }
  }

}