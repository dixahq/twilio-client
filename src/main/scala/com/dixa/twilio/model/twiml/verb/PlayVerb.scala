package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.StringUtil
import com.dixa.twilio.model.twiml.{TwimlConstraints, TwimlElement}

import scala.annotation.nowarn

/** Representation of the Say Verb from TwiML
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  */
sealed trait PlayVerb extends TwimlElement.Verb {}

object PlayVerb {

  final class Builder[B <: TwimlConstraints.Buildable] private[PlayVerb] (url: String) {

    def withSoundFileUrl(url: String): Builder[TwimlConstraints.BuildableTrue] =
      new Builder[TwimlConstraints.BuildableTrue](url = url)

    @nowarn(value = "cat=unused-params")
    def build()(
        implicit ev: B =:= TwimlConstraints.BuildableTrue
    ): PlayVerb = PlayVerbImpl(url)
  }
  type BuilderStartState = Builder[TwimlConstraints.BuildableFalse]
  type BuildFunction     = BuilderStartState => PlayVerb

  def build(fun: BuildFunction): PlayVerb = fun(
    new BuilderStartState("")
  )

  private final case class PlayVerbImpl(text: String) extends PlayVerb {
    override val xmlCompact: String = s"""<Play>${StringUtil.xmlEscape(text)}</Play>"""

    override def xmlPretty: String = xmlCompact
  }
}
