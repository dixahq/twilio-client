package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.StringUtil
import com.dixa.twilio.model.twiml.{Response, TwimlConstraints, TwimlElement}

import scala.annotation.nowarn

/** Representation of the Say Verb from TwiML
  *
  * Creating a [[Response]] via the [[Response.build]] method, is the preferred way to use this
  * trait.
  */
sealed trait SayVerb extends TwimlElement.Verb {}

object SayVerb {

  final class Builder[B <: TwimlConstraints.Buildable] private[SayVerb] (text: String) {

    def withText(text: String): Builder[TwimlConstraints.BuildableTrue] =
      new Builder[TwimlConstraints.BuildableTrue](text = text)

    @nowarn
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
    override val xmlCompact: String = s"""<Say>${StringUtil.xmlEscape(text)}</Say>"""

    override def xmlPretty: String = xmlCompact
  }
}
