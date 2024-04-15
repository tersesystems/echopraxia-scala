package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{Attributes, Field, Value, Condition => JCondition, FieldBuilderResult => JFieldBuilderResult, Level => JLevel, LoggingContext => JLoggingContext}
import com.tersesystems.echopraxia.api.Value.ArrayValue
import com.tersesystems.echopraxia.api.Value.ObjectValue
import com.tersesystems.echopraxia.spi.PresentationHintAttributes

import java.util
import java.util.stream
import java.util.stream.Collectors

trait LowPriorityImplicits {

  final implicit class RichCondition(javaCondition: JCondition) {
    @inline
    def asScala: Condition = Condition((level, context) => javaCondition.test(level.asJava, context.asJava))
  }

  final implicit class RichLoggingContext(context: JLoggingContext) {
    @inline
    def asScala: LoggingContext = new ScalaLoggingContext(context)
  }

  final implicit class RichLevel(level: JLevel) {
    @inline
    def asScala: Level = Level.asScala(level)
  }

  final implicit class RichValue[T](value: Value[T]) {

    def asCardinal: Value[T] = {
      value.withAttributes(value.attributes().plus(PresentationHintAttributes.asCardinal()))
    }

    def abbreviateAfter(after: Int): Value[T] = {
      value.withAttributes(value.attributes().plus(PresentationHintAttributes.abbreviateAfter(after)))
    }

    def withToStringValue(s: String): Value[T] = {
      value.withAttributes(value.attributes().plus(PresentationHintAttributes.withToStringValue(s)))
    }
  }

  final implicit class RichArrayValue(val value: ArrayValue) {

    def asCardinal: ArrayValue = {
      value.withAttributes(value.attributes().plus(PresentationHintAttributes.asCardinal()))
    }

    def abbreviateAfter(after: Int): ArrayValue = {
      value.withAttributes(value.attributes().plus(PresentationHintAttributes.abbreviateAfter(after)))
    }

    def withToStringValue(s: String): ArrayValue = {
      value.withAttributes(value.attributes().plus(PresentationHintAttributes.withToStringValue(s)))
    }

    def +(v: Value[_]): ArrayValue = value.add(v)

    def ++(vs: Traversable[Value[_]]): ArrayValue = addAll(vs)

    def addAll(newValues: Traversable[Value[_]]): ArrayValue = {
      val joinedValues = new util.ArrayList(value.raw())
      for (f <- newValues) {
        joinedValues.add(f)
      }
      Value.array(joinedValues)
    }

    def ++(newValues: util.Collection[Value[_]]): ArrayValue = value.addAll(newValues)
  }

  final implicit class RichObjectValue(value: ObjectValue) {

    def asCardinal: ObjectValue = {
      value.withAttributes(value.attributes().plus(PresentationHintAttributes.asCardinal()))
    }

    def abbreviateAfter(after: Int): ObjectValue = {
      value.withAttributes(value.attributes().plus(PresentationHintAttributes.abbreviateAfter(after)))
    }

    def withToStringValue(s: String): ObjectValue = {
      value.withAttributes(value.attributes().plus(PresentationHintAttributes.withToStringValue(s)))
    }

    def +(field: Field): ObjectValue = value.add(field)

    def addAll(fields: Traversable[Field]): ObjectValue = {
      val joinedFields = new util.ArrayList(value.raw)
      for (f <- fields) {
        joinedFields.add(f)
      }
      Value.`object`(joinedFields)
    }

    def ++(fields: Traversable[Field]): ObjectValue = addAll(fields)

    def ++(fields: util.Collection[Field]): ObjectValue = value.addAll(fields)
  }

  final implicit class RichFieldBuilderResult(result: JFieldBuilderResult) {
    @inline
    def concat(other: JFieldBuilderResult): JFieldBuilderResult = { () =>
      stream.Stream.concat(result.fields().stream(), other.fields().stream()).collect(Collectors.toList())
    }

    @inline
    def ++(other: JFieldBuilderResult): JFieldBuilderResult = concat(other)
  }

}

object Implicits extends LowPriorityImplicits
