package echopraxia.plusscala.flow

import echopraxia.api.Field
import echopraxia.api.FieldBuilderResult
import echopraxia.api.Value
import echopraxia.plusscala.api.*

trait FlowFieldBuilder extends ValueTypeClasses {

  def enteringTemplate: String

  def exitingTemplate: String

  def throwingTemplate: String

  def entering: FieldBuilderResult

  def exiting(value: Value[?]): FieldBuilderResult

  def throwing(ex: Throwable): FieldBuilderResult
}

trait DefaultFlowFieldBuilder extends FieldBuilder with FlowFieldBuilder {
  override val enteringTemplate: String = "{}"

  override val exitingTemplate: String = "{} => {}"

  override val throwingTemplate: String = "{} ! {}"

  override def entering: FieldBuilderResult = {
    DefaultFlowFieldBuilder.entryTag
  }

  override def exiting(retValue: Value[?]): FieldBuilderResult = {
    list(DefaultFlowFieldBuilder.exitTag, value(DefaultFlowFieldBuilder.Result, retValue))
  }

  override def throwing(ex: Throwable): FieldBuilderResult = {
    list(DefaultFlowFieldBuilder.throwingTag, exception(ex))
  }
}

object DefaultFlowFieldBuilder extends DefaultFlowFieldBuilder {
  val Tag: String   = "flowTag"
  val Entry: String = "entry"
  val Exit: String  = "exit"

  val Throwing: String = "throwing"
  val Result: String   = "result"

  val entryTag: Field    = value(Tag, Entry)
  val exitTag: Field     = value(Tag, Exit)
  val throwingTag: Field = value(Tag, Throwing)
}
