package part1recap

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MultithreadingRecap extends App {
  // creating threads on the JVM
  val aThread = new Thread(() => println("I'm running in parallel"))
  aThread.start()
  aThread.join()

  // problems with threads is that they are unpredictable
  val threadHello = new Thread(() => (1 to 1000).foreach(_ => println("hello")))
  val threadGoodBy = new Thread(() => (1 to 1000).foreach(_ => println("goodbye")))

  threadHello.start()
  threadGoodBy.start()

  // different runs produce different results

  class BankAccount(private var amount: Int) {
    override def toString: String = "" + amount
    def withdraw(money: Int) = this.amount -= money

    def safeWithdraw(money: Int) = this.synchronized {
      this.amount -= money
    }
  }

  /*
      BA(10000)
      T1 -> withdraw 1000
      T2 -> withdraw 2000

      T1 -> this.amount = this.amount - ... // PREEMPTED by the OS
      T2 -> this.amount = this.amount - 2000 = 8000

      T1 -> -1000 = 9000

      => result = 9000

      this.amount = this.amount - 1000 is not ATOMIC


      Adding @volatile to the class member helps to use atomic read, but not write. Also only works for primitive types

      use of synchronized blocks solve the problem
   */

  // inter-thread communication on the JVM
  // wait - notify mechanism

  // Scala Futures
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    //long computation - on a different thread
    42
  }

  //callbacks
  future.onComplete {
    case Success(42) => println("I found the meaning of life")
    case Failure(_) => println("Something happened with the meaning of life")
  }


  val aProcessedFuture = future.map(_ + 1) // Future with 43
  val aFlatFuture = future.flatMap { value =>
    Future(value + 2)
  }

  val filteredFuture = future.filter(_ % 2 == 0) // NoSuchEleentException

  //for comprehensions
  val aNonsenseFunture = for {
    meaningOfLife <- future
    filteredMeaning <- filteredFuture
  } yield meaningOfLife + filteredMeaning

  // anThen, recover/recoverWith

  // Promises: Writable futures
}
