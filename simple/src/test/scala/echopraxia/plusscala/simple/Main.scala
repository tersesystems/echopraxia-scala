package echopraxia.plusscala.simple

import echopraxia.api.Field
import echopraxia.plusscala.api.FieldBuilder.ToFieldBuilderResult
import echopraxia.plusscala.api.{FieldBuilder, HeterogeneousFieldSupport}

import java.util.{Currency, UUID}
import scala.concurrent.Future

object Main {
  def main(args: Array[String]): Unit = {
    System.setProperty("ECHOPRAXIA_STDOUT_LOGGING_LEVEL", "true")
    val printer = new Printer()
    printer.print()
  }
}

class NoImplicits {
  private val USD = Currency.getInstance("USD")

  private val logger = LoggerFactory.getLogger(getClass)
  object MyFieldBuilder extends FieldBuilder with Logging
  private val fb = MyFieldBuilder

  logger.info("{}", fb.keyValue("foo" -> "foo"))
  logger.info("{}", fb.keyValue(Option(USD)))
  val either: Either[Currency, String] = Left(USD)
  logger.info("{}", fb.keyValue(either))
}

class Printer extends Logging with HeterogeneousFieldSupport {
  private val logger = LoggerFactory.getLogger(getClass)

  private val USD = Currency.getInstance("USD")

  def print(): Unit = {
    val person1 = Person("Person1", 12)
    val person2 = Person("Person2", 15)

    // This uses the "personToField" mapping
    logger.info("template shows {}", person1)
    logger.info(person1)

    // Can define custom mapping with tuples
    logger.info("person1" -> person1)
    logger.info("person1" -> person1, "person2" -> person2)

    // Options work out of the box
    val optPerson: Option[Person] = Option(person1)
    logger.info("optPerson" -> optPerson)

    logger.info("optPerson" -> None)

    // As does either
    logger.info("eitherPerson" -> Left(person1))

    // And so do lists
    logger.info("people" -> Seq(person1, person2))

    // and maps (defined in Logging.scala)
    logger.info("people" -> Map("person1" -> person1, "person2" -> person2))

    // And even tuples  (defined in Logging.scala)
    logger.info("intToPersonMap" -> (1 -> person1))

    // support for exceptions
    logger.error(new IllegalStateException())

    // Log with conditional logging
    if (logger.isInfoEnabled) {
      logger.info("p1" -> person1, "p2" -> person2, "p3" -> person1)
    }

    logger.info(
      "deferred argument construction {} {} {}",
      () => ToFieldBuilderResult(Seq[Field]("p1" -> person1, "p2" -> person2, "p3" -> person1))
    )

    // Complex objects are no problem
    val book1 = Book(
      Category("reference"),
      Author("Nigel Rees"),
      Title("Sayings of the Century"),
      Price(amount = 8.95, currency = Currency.getInstance("USD"))
    )
    logger.info(book1)

    // You can also render case classes using custom presentation while using
    // structured JSON, i.e. this will render $8.95 in pattern layout
    // while still rendering as a JSON object
    logger.info(Price(8.95, USD))

    val creditCard = CreditCard("4111 1111 1111 1111", "04/23")
    logger.info("{}", creditCard)

    // If you want to render fields as an object, you can use ToObjectValue
    logger.info("object" -> ToObjectValue(book1, person1)) // object={book={}, person={}}

    // For heterogeneous fields you'll need to use `Seq[Field]` explicitly, or use info.v as seen below
    logger.info("object" -> Seq[Field](book1, person1)) // object=[book={}, person={}]

    // For heterogeneous values you'll want to specify Seq[Value[_]] to give implicit conversion some clues
    logger.info("oneTrueString" -> Seq(ToValue(1), ToValue(true), ToValue("string")))

    // You can also use "withFields" to render JSON on every message (this will not show in line format)
    logger.withFields(book1, person1).info("testing")

    // Can also log using class name
    logger.info(UUID.randomUUID)

    // Logging futures is also possible, and can include names (defined in Logging.scala)
    logger.info(Future.successful(true))
    logger.info(Future.successful("String"))
  }
}
