package com.tersesystems.echopraxia.plusscala

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.tersesystems.echopraxia.plusscala.api._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import java.util

class ConditionSpec extends AnyFunSpec with BeforeAndAfterEach with Matchers {

  private val logger = LoggerFactory.getLogger(getClass, MyFieldBuilder)

  describe("findBoolean") {

    it("should match") {
      val condition: Condition = (_, ctx) => ctx.findBoolean("$.foo").getOrElse(false)
      logger.debug(condition, "found a foo == true", _.bool("foo", true))

      matchThis("found a foo == true")
    }

    it("should return None if no match") {
      val condition: Condition = (_, ctx) => ctx.findBoolean("$.foo").getOrElse(false)
      logger.debug(condition, "found a foo == true")

      noMatch
    }

    it("should return None if match is not boolean") {
      val condition: Condition = (_, ctx) => ctx.findBoolean("$.foo").getOrElse(false)
      logger.debug(condition, "found a foo == true", _.string("foo", "bar"))

      noMatch
    }
  } // findBoolean

  describe("findString") {

    it("should return some on match") {
      val condition: Condition = (_, ctx) => ctx.findString("$.foo").contains("bar")
      logger.debug(condition, "found a foo == bar", _.string("foo", "bar"))

      matchThis("found a foo == bar")
    }

    it("should none on no match") {
      val condition: Condition = (_, ctx) => ctx.findString("$.foo").contains("bar")
      logger.debug(condition, "found a foo == bar")

      noMatch
    }

    it("should none on wrong type") {
      val condition: Condition = (_, ctx) => ctx.findString("$.foo").contains("bar")
      logger.debug(condition, "found a foo == bar", _.number("foo", 1))

      noMatch
    }
  } // findString

  describe("findNumber") {
    it("should some on match") {
      val condition: Condition = (_, ctx) => ctx.findNumber("$.foo").exists(_.intValue() == 1)
      logger.debug(condition, "found a number == 1", _.number("foo", 1))

      matchThis("found a number == 1")
    }
  } // findNumber

  describe("findNull") {
    it("should work with findNull") {
      val condition: Condition = (_, ctx) => ctx.findNull("$.foo")
      logger.debug(condition, "found a null!", _.nullField("foo"))

      matchThis("found a null!")
    }
  }

  describe("findList") {
    it("should work with list of same type") {
      val condition: Condition = (_, ctx) => {
        val list   = ctx.findList("$.foo")
        val result = list.contains("derp")
        result
      }
      logger.debug(
        condition,
        "found a list with derp in it!",
        _.array("foo", Array("derp"))
      )

      matchThis("found a list with derp in it!")
    }

    it("should work with list with different type values") {
      val condition: Condition = (_, ctx) => {
        val nummatch = ctx.findList("$.foo").contains(1)
        val strmatch = ctx.findList("$.foo").contains("derp")
        nummatch && strmatch
      }
      logger.debug(
        condition,
        "found a list with 1 in it!",
        fb => {
          import fb._
          // have to explicitly make Seq[Value[_]] here
          fb.array("foo", Seq(ToValue("derp"), ToValue(1), ToValue(false)))
        }
      )

      matchThis("found a list with 1 in it!")
    }

    it("should match on list containing objects") {
      val condition: Condition = (_, ctx) => {
        val obj = ctx.findList("$.array")
        obj.head match {
          case map: Map[String, Any] =>
            map.get("a").contains(1) && map.get("c").contains(false)
          case _ =>
            false
        }
      }
      logger.debug(
        condition,
        "complex object",
        fb => {
          fb.array(
            "array",
            Seq(
              fb.number("a" -> 1),
              fb.string("b" -> "two"),
              fb.bool("c"   -> false)
            )
          )
        }
      )
    }
  }

  describe("object") {
    it("should match on simple object") {
      logger
        .withCondition((_, ctx) => ctx.findObject("$.foo").get("key").equals("value"))
        .debug("simple map", fb => fb.obj("foo", fb.string("key" -> "value")))

      matchThis("simple map")
    }

    it("should not match on no argument") {
      val condition: Condition = (_, ctx) => {
        ctx.findObject("$.foo").isDefined
      }
      logger.debug(condition, "no match", _.number("bar", 1))

      noMatch
    }

    it("should not match on incorrect type") {
      val condition: Condition = (_, ctx) => {
        ctx.findObject("$.foo").isDefined
      }
      logger.debug(condition, "no match", _.number("foo", 1))

      noMatch
    }

    it("should match on a complex object") {
      val condition: Condition = (_, ctx) => {
        val obj         = ctx.findObject("$.foo")
        val value1: Any = obj.get("a")
        value1 == (1)
      }
      logger.debug(
        condition,
        "complex object",
        fb =>
          fb.obj(
            "foo",
            Seq(
              fb.number("a" -> 1),
              fb.string("b" -> "two"),
              fb.bool("c"   -> false)
            )
          )
      )

      matchThis("complex object")
    }
  } // object

  it("should match on list") {
    val condition: Condition = (_, ctx) => {
      val opt: Seq[Any] = ctx.findList("$.foo")
      opt.nonEmpty
    }
    logger.debug(condition, "match list", fb => fb.array("foo" -> Seq(1, 2, 3)))

    matchThis("match list")
  }

  it("should make a list contain scala maps with int") {
    val isWill = Condition { (_: Level, context: LoggingContext) =>
      val list = context.findList("$.person[?(@.name == 'will')]")
      val map  = list.head.asInstanceOf[Map[String, Any]]
      map("age") == 1
    }
    logger.debug(isWill, "match list", _.obj("person" -> Person("will", 1)))

    matchThis("match list")
  }

  it("should make a list contain scala maps with string") {
    val isWill = Condition { (_: Level, context: LoggingContext) =>
      val list = context.findList("$.person[?(@.name == 'will')]")
      val map  = list.head.asInstanceOf[Map[String, Any]]
      map("name") == "will"
    }
    logger.withFieldBuilder(MyFieldBuilder).debug(isWill, "match list", _.obj("person" -> Person("will", 1)))

    matchThis("match list")
  }

  it("should make a list contain scala maps with float") {
    val isWill = Condition { (_: Level, context: LoggingContext) =>
      val map = context.findObject("$.obj").get
      map("float") == 0.0f
    }
    logger.debug(isWill, "match list", fb => fb.obj("obj" -> fb.number("float" -> 0.0f)))

    matchThis("match list")
  }

  it("should deal with BigInt") {
    val isWill = Condition { (_: Level, context: LoggingContext) =>
      val obj = context.findObject("$.obj").get
      obj("bigint") == BigInt("1100020323232341313413")
    }

    logger.debug(
      isWill,
      "match list",
      fb => {
        fb.obj("obj" -> fb.number("bigint" -> BigInt("1100020323232341313413")))
      }
    )

    matchThis("match list")
  }

  it("should deal with BigDecimal") {
    val isWill = Condition { (_: Level, context: LoggingContext) =>
      val govt = context.findObject("$.government").get
      govt("debt") == BigDecimal("1100020323232341313413")
    }

    val usGovernment = Government("US", debt = BigDecimal("1100020323232341313413"))
    logger.withFieldBuilder(MyFieldBuilder).debug(isWill, "match list", _.obj("government" -> usGovernment))

    matchThis("match list")
  }

  it("should not match on an object mismatch") {
    val condition: Condition = (_, ctx) => {
      val opt: Option[_] = ctx.findObject("$.foo")
      opt.isDefined
    }
    logger.debug(condition, "no match", _.bool("foo" -> true))

    noMatch
  }

  it("should match on level") {
    val condition: Condition = (level, _) => level.isGreater(Level.DEBUG)

    logger.info(condition, "matches on level")
    matchThis("matches on level")
  }

  describe("throwable") {

    it("should match a subclass of throwable with tuple") {
      val t = new Exception()
      logger.info("matches on throwable {}", _.exception("derp" -> t))
      matchThis("matches on throwable derp=java.lang.Exception")
    }

    it("should match a subclass of throwable with arg") {
      val t = new Exception()
      logger.info("matches on throwable {}", _.exception(t))
      matchThis("matches on throwable exception=java.lang.Exception")
    }

  }

  private def noMatch = {
    val listAppender: ListAppender[ILoggingEvent] = getListAppender
    val list: util.List[ILoggingEvent]            = listAppender.list
    list must be(empty)
  }

  private def matchThis(message: String) = {
    val listAppender: ListAppender[ILoggingEvent] = getListAppender
    val list: util.List[ILoggingEvent]            = listAppender.list
    val event: ILoggingEvent                      = list.get(0)
    event.getFormattedMessage must be(message)
  }

  override def beforeEach(): Unit = {
    getListAppender.list.clear()
  }

  private def loggerContext: LoggerContext = {
    org.slf4j.LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
  }

  private def getListAppender: ListAppender[ILoggingEvent] = {
    loggerContext
      .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
      .getAppender("LIST")
      .asInstanceOf[ListAppender[ILoggingEvent]]
  }
}

case class Person(name: String, age: Int)

case class Government(name: String, debt: BigDecimal)

trait MyFieldBuilder extends FieldBuilder {
  implicit val personToValue: ToObjectValue[Person] = { person: Person =>
    ToObjectValue(
      keyValue("name", ToValue(person.name)),
      keyValue("age", ToValue(person.age))
    )
  }

  implicit val govtToValue: ToObjectValue[Government] = { govt: Government =>
    ToObjectValue(
      keyValue("name", ToValue(govt.name)),
      keyValue("debt", ToValue(govt.debt))
    )
  }
}

object MyFieldBuilder extends MyFieldBuilder
