import scala.util.Random

/**
  * Classes, traits and objects
  */
trait IntroClasses

class IntroClass(name: String) extends IntroClasses

// Companion object
object IntroClass {
  val default = new IntroClass("Hallo")
}

// Case class
case class IntroCaseClass(name: String) extends IntroClasses

// Case object
case object IntroCaseObject


// New object
val introClass = new IntroClass("Hallo")

val introCaseClass = IntroCaseClass("Hallo")


// equals / hashCode

val anotherIntroClass = new IntroClass("Hallo")

Map(introClass -> 5, anotherIntroClass -> 6)

val anotherIntroCaseClass = IntroCaseClass("Hallo")

Map(introCaseClass -> 5, anotherIntroCaseClass -> 6)


// copy
val modifiedAnotherIntroCaseClass = anotherIntroCaseClass.copy(name = "Hallo Welt")


/**
  * Methods, call-by-value, call-by-name
  */

case class IntroCaseClassWithMethod(name: String) {

  def lengthOfName(toAdd: Int): Int = {
    name.length + toAdd
  }
}

IntroCaseClassWithMethod("LaLeLu").lengthOfName(2)

case class IntroCaseClassWithMethodRequiringAFunction(name: String) {

  def lengthName(transformer: String => Int): Int = {
    name.length + transformer(name)
  }
}

IntroCaseClassWithMethodRequiringAFunction("LaLeLu").lengthName(name => Random.nextInt(name.length))


/**
  * Options and Pattern Matching
  */

case class IntroCaseClassWithOption(maybeName: Option[String] = None)

val maybeIntroClass = IntroCaseClassWithOption(Some("LaLeLu"))
val maybeIntroClassNone = IntroCaseClassWithOption()

val maybeName = maybeIntroClass.maybeName
maybeIntroClassNone.maybeName

maybeName match {
  case None => None
  case Some(x) => x
}

maybeIntroClassNone.maybeName match {
  case None => None
  case Some(x) => x
}






/**
  * Higher order functions
  */

val aList = IntroCaseClassWithMethod("LaLeLu") :: IntroCaseClassWithMethod("Lulu") :: Nil

// .map

aList.map(_.name)

aList.map(_.lengthOfName(2))

// .flatMap

// .groupBy

// .sum

// .reduce