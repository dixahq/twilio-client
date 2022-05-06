package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.twiml.{PhantomTypes, Response, TwimlElement}

/** Representation of the Say Verb from TwiML
  *
  * Creating a [[Response]] via the [[Response.build]] method, is the preferred way to use this
  * trait.
  */
sealed trait SayVerb extends TwimlElement.Verb {}

object SayVerb {

  final class Builder[B <: PhantomTypes.Buildable] private[SayVerb] (text: String) {

    def withText(text: String): Builder[PhantomTypes.BuildableTrue] =
      new Builder[PhantomTypes.BuildableTrue](text = text)

    def build()(
        implicit ev: B =:= PhantomTypes.BuildableTrue
    ): SayVerb = SayVerbImpl(text)
  }
  type BuilderStartState = Builder[PhantomTypes.BuildableFalse]
  type BuildFunction     = BuilderStartState => SayVerb

  def build(fun: BuildFunction): SayVerb = fun(
    new BuilderStartState("")
  )

  private final case class SayVerbImpl(text: String) extends SayVerb {
    override val xmlCompact: String = s"""<Say>$text</Say>"""

    override def xmlPretty: String = xmlCompact
  }
}
