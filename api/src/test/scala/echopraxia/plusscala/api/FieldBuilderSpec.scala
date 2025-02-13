package echopraxia.plusscala.api

import echopraxia.api.Field
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import java.math.BigInteger
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Currency

class FieldBuilderSpec extends AnyFunSpec with Matchers {

  describe("FieldBuilder") {

    it("should work with java.lang.Byte") {
      val fb   = FieldBuilder
      val byte = java.lang.Byte.MIN_VALUE
      fb.keyValue("byte", byte)
    }

    it("should work with java.lang.Byte as a tuple") {
      val fb   = FieldBuilder
      val byte = java.lang.Byte.MIN_VALUE
      fb.keyValue("byte" -> byte)
    }

    it("should work with java.lang.Byte using fb.number") {
      val fb   = FieldBuilder
      val byte = java.lang.Byte.MIN_VALUE
      fb.number("byte", byte)
    }

    it("should work with java.lang.Byte using fb.number as tuple") {
      val fb   = FieldBuilder
      val byte = java.lang.Byte.MIN_VALUE
      fb.number("byte" -> byte)
    }

    it("should work with scala.Byte") {
      val fb   = FieldBuilder
      val byte = Byte.MinValue
      fb.keyValue("byte", byte)
    }

    it("should work with scala.Byte as a tuple") {
      val fb   = FieldBuilder
      val byte = Byte.MinValue
      fb.keyValue("byte" -> byte)
    }

    it("should work with scala.Byte using fb.number") {
      val fb   = FieldBuilder
      val byte = Byte.MinValue
      fb.number("byte", byte)
    }

    it("should work with scala.Byte using fb.number as a tuple") {
      val fb   = FieldBuilder
      val byte = Byte.MinValue
      fb.number("byte" -> byte)
    }

    it("should work with java.lang.Short") {
      val fb    = FieldBuilder
      val short = java.lang.Short.MIN_VALUE
      fb.keyValue("short", short)
    }

    it("should work with java.lang.Integer") {
      val fb      = FieldBuilder
      val integer = java.lang.Integer.valueOf(1)
      fb.keyValue("int", integer)
    }

    it("should not die when given a null integer") {
      val fb                         = FieldBuilder
      val integer: java.lang.Integer = null
      fb.keyValue("int", integer)
    }

    it("should work with java.lang.Long") {
      val fb   = FieldBuilder
      val long = java.lang.Long.valueOf(1)
      fb.keyValue("long", long)
    }

    it("should work with java.lang.Float") {
      val fb    = FieldBuilder
      val float = java.lang.Float.valueOf(1)
      fb.keyValue("float", float)
    }

    it("should work with java.lang.Double") {
      val fb     = FieldBuilder
      val double = java.lang.Double.valueOf(1)
      fb.keyValue("double", double)
    }

    it("should work with java.lang.BigInteger") {
      val fb         = FieldBuilder
      val bigInteger = BigInteger.ZERO
      fb.keyValue("bigInteger", bigInteger)
    }

    it("should work with java.lang.BigDecimal") {
      val fb         = FieldBuilder
      val bigDecimal = BigDecimal.valueOf(1)
      fb.keyValue("bigDecimal", bigDecimal)
    }

    it("should work with java.lang.Boolean") {
      val fb   = FieldBuilder
      val bool = java.lang.Boolean.TRUE
      fb.keyValue("bool", bool)
    }

    it("should work with a value attribute") {
      val fb    = MyFieldBuilder
      val epoch = Instant.EPOCH

      // this works only if ToValueAttribute is a path dependent type, and then hanging it off
      // the singleton object will work.
      fb.keyValue("instant", epoch).toString must be("instant=1/1/70 12:00 AM")
    }

    it("should work with array of value attribute") {
      val fb    = MyFieldBuilder
      val epoch = Instant.EPOCH

      fb.keyValue("instants", Seq(epoch)).toString must be("instants=[1/1/70 12:00 AM]")
    }

    it("should work with array of value attribute using fb.array") {
      val fb    = MyFieldBuilder
      val epoch = Instant.EPOCH

      // found you :-D
      fb.array("instants", Seq(epoch)).toString must be("instants=[1/1/70 12:00 AM]")
    }

    it("should work with object") {
      val fb = MyFieldBuilder

      val objectField: Field = fb.obj("object", fb.keyValue("foo" -> "bar"))
      objectField.toString must be("object={foo=bar}")
    }

    it("should work with object with toStringFormat") {
      val fb = MyFieldBuilder

      val currency           = Currency.getInstance("USD")
      val objectField: Field = fb.obj("usCurrency", currency)
      objectField.toString must be("usCurrency=$")
    }
  }

  trait MyFieldBuilder extends FieldBuilder {
    implicit val instantToValue: ToValue[Instant] = instant => {
      val datetime  = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
      val formatter = DateTimeFormatter.ofPattern("M/d/YY hh:mm a")
      val s         = formatter.format(datetime)
      ToValue(instant.toString).withToStringValue(s)
    }

    implicit val currencyToValue: ToObjectValue[Currency] = (currency: Currency) =>
      ToObjectValue(
        keyValue("currencyCode" -> currency.getCurrencyCode)
      ).withToStringValue(currency.getSymbol()).asObject()
  }
  object MyFieldBuilder extends MyFieldBuilder
}
