package part1recap

import scala.reflect.runtime.universe.Try

object GeneralRecap extends App {
  val aCondition: Boolean = false

  var aVariable = 42
  aVariable += 1

  // Expressions
  val aConditionVal = if (aCondition) 42 else 65

  //code block

  val aCodeBlock = {
    if (aCondition) 74
    56
  }

  // types
  // Unit <-- side effects
  val theUnit = println("Hello, Scala")
  def aFunctio(x: Int) = x + 1

  // recursion - TAIL recursion
  def factorial(n: Int, acc: Int): Int =
    if (n <=0 ) acc
    else factorial(n - 1, acc * n)


  // OOP
  class Animal
  class Dog extends Animal
  val aDog: Animal = new Dog

  trait Carnivore {
    def eat(a: Animal): Unit
  }

  class Cocodrile extends Animal with Carnivore {
    override def eat(a: Animal): Unit = println("crunch!")
  }

  // method notations
  val sCroc = new Cocodrile
  sCroc.eat(aDog)

  val aCarnivore = new Carnivore {
    override def eat(a: Animal): Unit = println("roar")
  }

  aCarnivore.eat(aDog)

  // Generics
  abstract class MyList[+A]

  // companion object and singleton objects

  object MyList

  // case classes
  case class Person(name: String, age: Int) // a LOT in this course

  // Exceptions
  val aPotentialFailure = try {
    throw new RuntimeException("I'm innocent, I swear!") // nothing
  } catch {
    case e: Exception => "I caught an exception!"
  } finally {
    println("some logs")
  }


  val incrementer = new Function[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  val incremented = incrementer(42) // 43
  // Equivalent to incrementer.apply(42)

  val anonymousIncremented = (x: Int) => x + 1
  // Int => Int === Function1[Int, Int]

  // FP is all about working with functions asa first-class
  List(1,2,3).map(incrementer)
  // map = HOF (Higher order function

  // for comprehensions
  val pairs = for  {
    num <- List(1,2,3,4)
    char <- List('a', 'b', 'c', 'd')
  } yield num + "-" + char

  // Scala rewrite List(1,2,3,4).flatMap(num => List('a', 'b', 'c', 'd').map(char => num + "-" + char))

  // Seq, Array, List, vector, Map, Tupes, Sets

  // "Collections"
  // Option and Try

  val anOption = Some(2)

  // Deprecated ??
  val aTry = Try {
    throw new RuntimeException()
  }

  // pattern matching
  val unknown = 2
  val order = unknown match {
    case 1 => "first"
    case 2 => "second"
    case _ => "unknown"
  }

  val bob = Person("Bob", 22)
  val greeting = bob match  {
    case Person(n, _) => s"Hi, my name is $n"
    case _ => "I don't know my name"
  }

  // ALL THE patterns
}

