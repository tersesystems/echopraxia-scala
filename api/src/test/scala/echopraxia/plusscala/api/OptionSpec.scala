package echopraxia.plusscala.api

import echopraxia.api.Field
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter

// The tests here compile in 2.13 but do not compile in 2.12
class OptionSpec extends AnyWordSpec with Matchers with EchopraxiaBase {

  "option" should {

    "work with primitives" in {
      val field: Field = "test" -> Option(1)
      field.toString must be("test=1")
    }

    "work with Some" in {
      val field: Field = "test" -> Some(1)
      field.toString must be("test=1")
    }

    "work with None" in {
      // XXX works in 2.13, does not work in 2.12
      val field: Field = "test" -> None
      field.toString must be("test=null")
    }

    "work with objects" in {
      implicit val readableInstant: ToValue[Instant] = (v: Instant) => ToValue(v.toString)
      val field: Field                               = "test" -> Option(Instant.ofEpochMilli(0))
      field.toString must be("test=1970-01-01T00:00:00Z")
    }

    "work with custom attributes" in {
      implicit val readableInstant: ToValue[Instant] = (v: Instant) => {
        val datetime  = LocalDateTime.ofInstant(v, ZoneOffset.UTC)
        val formatter = DateTimeFormatter.ofPattern("M/d/YY hh:mm a")
        ToValue(v.toString).withToStringValue(formatter.format(datetime))
      }
      val field: Field = "test" -> Option(Instant.ofEpochMilli(0))
      field.toString must be("test=1/1/70 12:00 AM")
    }
  }
}
