package part1recap

import scala.concurrent.Future

object ThreadModelLimitations extends App {

  /**
   * OOP encapsulation is ony valid in the SINGLE THREADED MODEL
   *
   */

  class BankAccount(private var amount: Int) {
    override def toString: String = "" + amount

    def withdraw(money: Int) = this.amount -= money
    def deposit(money: Int) = this.amount += money

    def getAmount = amount
  }

//  val account = new BankAccount((2000))
//  for (_ <- 1 to 1000) {
//    new Thread(() => account.deposit(1)).start()
//  }
//
//  println(account.getAmount)

  // OOP encapsulation is broken in a multithreaded env

  // synchronization! Locks to the rescue
  // Locks introduce more problems deadlocks, livelocks

  /**
   * 2. Delegate a task to the background or send a signal to the thread
   * delegating something to thread is a PAIN
   */

  // yu have a running thread and you want to pass w runnable to that thread
  var task: Runnable = null
  val runningThread: Thread = new Thread(() => {
    while (true) {
      while (task == null) {
        runningThread.synchronized {
          println("[background] waiting for a task...")
          runningThread.wait()
        }
      }
      task.synchronized {
        println("[background] I have a task!")
        task.run()
        task = null
      }
    }
  })

  def delegateToBackgroundThread(r: Runnable) = {
    if (task == null) task = r
    runningThread.synchronized {
      runningThread.notify()
    }
  }

//  runningThread.start()
//  Thread.sleep(1000)
//  delegateToBackgroundThread(() => println(42))
//  Thread.sleep(1000)
//  delegateToBackgroundThread(() => println("this should run in the background"))


  /**
   * 3. tracing and deaing with errors in a multithreaded env is a PAIN
   *
   */
  // !M numbers in between 10 threads
  import scala.concurrent.ExecutionContext.Implicits.global
  val futures = (0 to 9)
    .map(i => 100000 * i until 100000 * (i + 1))
    .map(range => Future {
      if (range.contains(546735)) throw new RuntimeException("Invalid number")
      range.sum
    })

  val sumFuture = Future.reduceLeft(futures)(_ + _)
  sumFuture.onComplete(println)
}
