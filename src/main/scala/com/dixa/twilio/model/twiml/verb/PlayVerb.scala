package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.StringUtil
import com.dixa.twilio.model.dtmf.DtmfString
import com.dixa.twilio.model.twiml.TwimlConstraints.{Buildable, BuildableFalse, BuildableTrue}
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

  // Play specific constraints (Phantom types)
  sealed trait SoundFileAdded
  sealed trait SoundFileAddedTrue  extends SoundFileAdded
  sealed trait SoundFileAddedFalse extends SoundFileAdded

  sealed trait DigitsAdded
  sealed trait DigitsAddedTrue  extends DigitsAdded
  sealed trait DigitsAddedFalse extends DigitsAdded

  final class Builder[B <: Buildable, S <: SoundFileAdded, D <: DigitsAdded] private[PlayVerb] (
      url: String,
      digits: Option[DtmfString]
  ) {

    /** Add a url for a sound file to play.
      *
      * Only a single call to this method is allowed, as the Play verb only support a single file.
      * But you can just use two consecutive Play verb, if you need to play to files in a row.
      *
      * You can add both this and [[withDigits]], and in such cases the digits are played before the
      * sound file.
      */
    @nowarn(value = "cat=unused-params")
    def withSoundFileUrl(url: String)(
        implicit ev: S =:= SoundFileAddedFalse
    ): Builder[BuildableTrue, SoundFileAddedTrue, D] =
      new Builder[BuildableTrue, SoundFileAddedTrue, D](url = url, digits)

    /** Add DTMF digits to play
      *
      * Only a single call to this method is allowed, as the Play verb only support one single
      * string of DTMF digits.
      *
      * You can add both this and [[withSoundFileUrl]], and in such cases the digits are played
      * before the sound file.
      */
    @nowarn(value = "cat=unused-params")
    def withDigits(dtmfString: DtmfString)(
        implicit ev: D =:= DigitsAddedFalse
    ): Builder[BuildableTrue, S, DigitsAddedTrue] =
      new Builder[BuildableTrue, S, DigitsAddedTrue](url, Some(dtmfString))

    @nowarn(value = "cat=unused-params")
    def build()(
        implicit ev: B =:= TwimlConstraints.BuildableTrue
    ): PlayVerb = PlayVerbImpl(url, digits)
  }
  type BuilderStartState = Builder[BuildableFalse, SoundFileAddedFalse, DigitsAddedFalse]
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
