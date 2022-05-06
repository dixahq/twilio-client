package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.voice.TwilioConference

/** Represent the Conference noun in TwiML
  *
  * Creating a [[Response]] via the [[Response.build]] method, is the preferred way to use this
  * trait.
  */
sealed trait Conference extends TwimlElement.Noun with Dial.DialNoun {}

object Conference {

  final class Builder[B <: PhantomTypes.Buildable] private[Conference] (
      beep: Option[TwilioConference.Beep],
      waitUrl: Option[String],
      conferenceFriendlyName: TwilioConference.FriendlyName
  ) {

    def withBeep(beep: TwilioConference.Beep): Builder[B] =
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
        name: TwilioConference.FriendlyName
    ): Builder[PhantomTypes.BuildableTrue] =
      new Builder[PhantomTypes.BuildableTrue](beep, waitUrl, name)

    // At time of writing, there is still a huge list of attribute that can be used, but
    // this class is missing support for. So add then when needed.

    def build()(
        implicit evb: B =:= PhantomTypes.BuildableTrue
    ): Conference = ConferenceImpl(beep, waitUrl, conferenceFriendlyName)

  }

  type BuilderStartState = Builder[PhantomTypes.BuildableFalse]
  type BuildFunction     = BuilderStartState => Conference

  def build(fun: BuildFunction): Conference = fun(
    new BuilderStartState(None, None, TwilioConference.FriendlyName(""))
  )

  private final case class ConferenceImpl(
      beep: Option[TwilioConference.Beep],
      waitUrl: Option[String],
      conferenceFriendlyName: TwilioConference.FriendlyName
  ) extends Conference {
    override val xmlCompact: String = {
      val beepAtt    = beep.map(x => s""" beep="${x.twilioString}"""").getOrElse("")
      val waitUrlAtt = waitUrl.map(x => s""" waitUrl="$x"""").getOrElse("")
      s"""<Conference$beepAtt$waitUrlAtt>$conferenceFriendlyName</Conference>"""
    }

    override def xmlPretty: String = xmlCompact
  }
}
