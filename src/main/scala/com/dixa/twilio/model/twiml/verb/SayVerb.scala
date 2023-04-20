package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.twiml.{TwimlConstraints, TwimlElement}

import scala.annotation.nowarn
import scala.collection.immutable

/** Representation of the Say Verb from TwiML
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  */
sealed trait SayVerb extends TwimlElement.Verb {
  override final protected val tagName: String = "Say"

  override final protected val tagAttributes: immutable.Seq[(String, String)] = Nil

  override final protected val tagSubElements: immutable.Seq[TwimlElement] = Nil
}

object SayVerb {

  final class Builder[B <: TwimlConstraints.Buildable] private[SayVerb] (text: String) {

    def withText(text: String): Builder[TwimlConstraints.BuildableTrue] =
      new Builder[TwimlConstraints.BuildableTrue](text = text)

    @nowarn(value = "cat=unused-params")
    def build()(
        implicit ev: B =:= TwimlConstraints.BuildableTrue
    ): SayVerb = SayVerbImpl(text)
  }
  type BuilderStartState = Builder[TwimlConstraints.BuildableFalse]
  type BuildFunction     = BuilderStartState => SayVerb

  def build(fun: BuildFunction): SayVerb = fun(
    new BuilderStartState("")
  )

  private final case class SayVerbImpl(text: String) extends SayVerb {
    override protected def tagValue: Option[String] = Some(text)
  }
}
