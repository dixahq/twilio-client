package com.dixa.twilio.model.twiml.noun

import com.dixa.twilio.model.StringUtil
import com.dixa.twilio.model.twiml.verb.DialVerb
import com.dixa.twilio.model.twiml.{PhantomTypes, Response, TwimlElement}
import com.dixa.twilio.model.voice.Conference

/** Represent the Conference noun in TwiML
  *
  * Creating a [[Response]] via the [[Response.build]] method, is the preferred way to use this
  * trait.
  */
sealed trait ConferenceNoun extends TwimlElement.Noun with DialVerb.DialNoun {}

object ConferenceNoun {

  final class Builder[B <: PhantomTypes.Buildable] private[ConferenceNoun] (
      beep: Option[Conference.Beep],
      waitUrl: Option[String],
      conferenceFriendlyName: Conference.FriendlyName
  ) {

    def withBeep(beep: Conference.Beep): Builder[B] =
      new Builder[B](Some(beep), waitUrl, conferenceFriendlyName)

    /** Set a empty wait URL. Note that this is not the same, as not setting it.
      *
      * By default if we omit the wait url, Twilio will play default waiting music, but by explicit
      * setting it to a empty value, no waiting music will be played.
      */
    def withWaitUrlEmpty(): Builder[B] = new Builder[B](beep, Some(""), conferenceFriendlyName)

    // We should also have a method for setting a none empty wait url, but it was not
    // obvius what what be the best input type for that (java.net.URL) wont work for the
    // relative paths. So I have not included it, as I do not need it at time of writing this.

    def withConferenceFriendlyName(
        name: Conference.FriendlyName
    ): Builder[PhantomTypes.BuildableTrue] =
      new Builder[PhantomTypes.BuildableTrue](beep, waitUrl, name)

    // At time of writing, there is still a huge list of attribute that can be used, but
    // this class is missing support for. So add then when needed.

    def build()(
        implicit evb: B =:= PhantomTypes.BuildableTrue
    ): ConferenceNoun = ConferenceNounImpl(beep, waitUrl, conferenceFriendlyName)

  }

  type BuilderStartState = Builder[PhantomTypes.BuildableFalse]
  type BuildFunction     = BuilderStartState => ConferenceNoun

  def build(fun: BuildFunction): ConferenceNoun = fun(
    new BuilderStartState(None, None, Conference.FriendlyName(""))
  )

  private final case class ConferenceNounImpl(
      beep: Option[Conference.Beep],
      waitUrl: Option[String],
      conferenceFriendlyName: Conference.FriendlyName
  ) extends ConferenceNoun {
    override val xmlCompact: String = {
      val beepAtt             = beep.map(x => s""" beep="${x.twilioString}"""").getOrElse("")
      val waitUrlAtt          = waitUrl.map(x => s""" waitUrl="$x"""").getOrElse("")
      val escapedFriendlyName = StringUtil.xmlEscape(conferenceFriendlyName.toString)
      s"""<Conference$beepAtt$waitUrlAtt>$escapedFriendlyName</Conference>"""
    }

    override def xmlPretty: String = xmlCompact
  }
}
