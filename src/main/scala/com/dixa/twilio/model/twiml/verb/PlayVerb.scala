package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.StringUtil
import com.dixa.twilio.model.dtmf.DtmfString
import com.dixa.twilio.model.twiml.{TwimlConstraints, TwimlElement}

import scala.annotation.nowarn

/** Representation of the Play Verb from TwiML
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  *
  * Twilio documentation: https://www.twilio.com/docs/voice/twiml/play
  */
sealed trait PlayVerb extends TwimlElement.Verb {}

object PlayVerb {

  final class Builder[B <: TwimlConstraints.Buildable] private[PlayVerb] (
      url: String,
      digits: Option[DtmfString]
  ) {

    def withSoundFileUrl(url: String): Builder[TwimlConstraints.BuildableTrue] =
      new Builder[TwimlConstraints.BuildableTrue](url = url, digits)

    def withDigits(dtmfString: DtmfString): Builder[TwimlConstraints.BuildableTrue] =
      new Builder[TwimlConstraints.BuildableTrue](url, Some(dtmfString))

    @nowarn(value = "cat=unused-params")
    def build()(
        implicit ev: B =:= TwimlConstraints.BuildableTrue
    ): PlayVerb = PlayVerbImpl(url, digits)
  }
  type BuilderStartState = Builder[TwimlConstraints.BuildableFalse]
  type BuildFunction     = BuilderStartState => PlayVerb

  def build(fun: BuildFunction): PlayVerb = fun(
    new BuilderStartState("", None)
  )

  private final case class PlayVerbImpl(url: String, digits: Option[DtmfString]) extends PlayVerb {
    override val xmlCompact: String = {
      val digitsAttribute = digits.map(d => s""" digits="${d.twilioString}"""").getOrElse("")
      s"""<Play$digitsAttribute>${StringUtil.xmlEscape(url)}</Play>"""
    }

    override def xmlPretty: String = xmlCompact
  }
}
