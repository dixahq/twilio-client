// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.model.twiml.noun

import com.dixa.twilio.model.{HttpMethod, Region}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.twiml.TwimlElement
import com.dixa.twilio.model.twiml.TwimlElement.TagAttributeBuilder
import com.dixa.twilio.model.twiml.verb.DialVerb
import com.dixa.twilio.model.voice.{Call, Conference}

import scala.collection.immutable

/** Represent the Conference noun in TwiML
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  */
sealed trait ConferenceNoun extends TwimlElement.Noun with DialVerb.DialNoun {}

object ConferenceNoun {

  /** ConferenceNoun specific phantom types, used to enforce build constraints compile time. */
  object PhantomTypes {
    sealed trait ConferenceFriendlyNameSet
    sealed trait ConferenceFriendlyNameSetTrue  extends ConferenceFriendlyNameSet
    sealed trait ConferenceFriendlyNameSetFalse extends ConferenceFriendlyNameSet

    sealed trait WaitUrlSet
    sealed trait WaitUrlSetTrue  extends WaitUrlSet
    sealed trait waitUrlSetFalse extends WaitUrlSet

    sealed trait WaitUrlEmptySet
    sealed trait WaitUrlEmptySetTrue  extends WaitUrlEmptySet
    sealed trait WaitUrlEmptySetFalse extends WaitUrlEmptySet

    sealed trait RecordStartSet
    sealed trait RecordStartSetTrue  extends RecordStartSet
    sealed trait RecordStartSetFalse extends RecordStartSet

    sealed trait RecordStatusCallbackSet
    sealed trait RecordStatusCallbackSetTrue  extends RecordStatusCallbackSet
    sealed trait RecordStatusCallbackSetFalse extends RecordStatusCallbackSet

    sealed trait StatusCallbackSet
    sealed trait StatusCallbackSetTrue  extends StatusCallbackSet
    sealed trait StatusCallbackSetFalse extends StatusCallbackSet

  }

  final class Builder[
      ConferenceFriendlyNameSet <: PhantomTypes.ConferenceFriendlyNameSet,
      WaitUrlSet <: PhantomTypes.WaitUrlSet,
      WaitUrlEmptySet <: PhantomTypes.WaitUrlEmptySet,
      RecordStartSet <: PhantomTypes.RecordStartSet,
      StatusCallbackSet <: PhantomTypes.StatusCallbackSet,
      RecordStatusCallbackSet <: PhantomTypes.RecordStatusCallbackSet
  ] private[ConferenceNoun] (
      beep: Option[Conference.Beep],
      conferenceFriendlyName: Conference.FriendlyName,
      muted: Option[Boolean],
      startConferenceOnEnter: Option[Boolean],
      endConferenceOnExit: Option[Boolean],
      participantLabel: Option[Conference.Participant.Label],
      jitterBufferSize: Option[Conference.Participant.JitterBufferSize],
      waitUrl: Option[String],
      waitMethod: Option[HttpMethod],
      maxParticipants: Option[Conference.MaxParticipants],
      record: Option[Conference.Record],
      region: Option[Region],
      trim: Option[Conference.Trim],
      coach: Option[Call.Sid],
      statusCallbackEvent: Option[Seq[Conference.StatusCallbackEvent]],
      statusCallback: Option[CallbackUrl],
      statusCallbackMethod: Option[HttpMethod],
      recordingStatusCallback: Option[CallbackUrl],
      recordingStatusCallbackMethod: Option[HttpMethod],
      recordingStatusCallbackEvent: Option[Seq[Conference.RecordingStatusCallbackEvent]]
  ) {

    private def copy[
        ConferenceFriendlyNameSetNew <: PhantomTypes.ConferenceFriendlyNameSet,
        WaitUrlSetNew <: PhantomTypes.WaitUrlSet,
        WaitUrlEmptySetNew <: PhantomTypes.WaitUrlEmptySet,
        RecordStartSetNew <: PhantomTypes.RecordStartSet,
        StatusCallbackSetNew <: PhantomTypes.StatusCallbackSet,
        RecordStatusCallbackSetNew <: PhantomTypes.RecordStatusCallbackSet
    ](
        beep: Option[Conference.Beep] = this.beep,
        conferenceFriendlyName: Conference.FriendlyName = this.conferenceFriendlyName,
        muted: Option[Boolean] = this.muted,
        startConferenceOnEnter: Option[Boolean] = this.startConferenceOnEnter,
        endConferenceOnExit: Option[Boolean] = this.endConferenceOnExit,
        participantLabel: Option[Conference.Participant.Label] = this.participantLabel,
        jitterBufferSize: Option[Conference.Participant.JitterBufferSize] = this.jitterBufferSize,
        waitUrl: Option[String] = this.waitUrl,
        waitMethod: Option[HttpMethod] = this.waitMethod,
        maxParticipants: Option[Conference.MaxParticipants] = this.maxParticipants,
        record: Option[Conference.Record] = this.record,
        region: Option[Region] = this.region,
        trim: Option[Conference.Trim] = this.trim,
        coach: Option[Call.Sid] = this.coach,
        statusCallbackEvent: Option[Seq[Conference.StatusCallbackEvent]] = this.statusCallbackEvent,
        statusCallback: Option[CallbackUrl] = this.statusCallback,
        statusCallbackMethod: Option[HttpMethod] = this.statusCallbackMethod,
        recordingStatusCallback: Option[CallbackUrl] = this.recordingStatusCallback,
        recordingStatusCallbackMethod: Option[HttpMethod] = this.recordingStatusCallbackMethod,
        recordingStatusCallbackEvent: Option[Seq[Conference.RecordingStatusCallbackEvent]] =
          this.recordingStatusCallbackEvent
    ): Builder[
      ConferenceFriendlyNameSetNew,
      WaitUrlSetNew,
      WaitUrlEmptySetNew,
      RecordStartSetNew,
      StatusCallbackSetNew,
      RecordStatusCallbackSetNew
    ] =
      new Builder(
        beep,
        conferenceFriendlyName,
        muted,
        startConferenceOnEnter,
        endConferenceOnExit,
        participantLabel,
        jitterBufferSize,
        waitUrl,
        waitMethod,
        maxParticipants,
        record,
        region,
        trim,
        coach,
        statusCallbackEvent,
        statusCallback,
        statusCallbackMethod,
        recordingStatusCallback,
        recordingStatusCallbackMethod,
        recordingStatusCallbackEvent
      )

    type BuilderWithSameTypes =
      Builder[
        ConferenceFriendlyNameSet,
        WaitUrlSet,
        WaitUrlEmptySet,
        RecordStartSet,
        StatusCallbackSet,
        RecordStatusCallbackSet
      ]

    /** See documentation on [[com.dixa.twilio.model.voice.Conference.Beep]] for details. */
    def withBeep(beep: Conference.Beep): BuilderWithSameTypes = copy(beep = Some(beep))

    def withConferenceFriendlyName(
        name: Conference.FriendlyName
    ): Builder[
      PhantomTypes.ConferenceFriendlyNameSetTrue,
      WaitUrlSet,
      WaitUrlEmptySet,
      RecordStartSet,
      StatusCallbackSet,
      RecordStatusCallbackSet
    ] =
      copy(conferenceFriendlyName = name)

    /** The muted attribute lets you specify whether a participant can speak on the conference.
      *
      * If this attribute is set to true, the participant will only be able to listen to people on
      * the conference. This attribute defaults to false.
      *
      * To change a conference participant's muted attribute during a call use to the Conference
      * Participant API.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-muted
      * @see
      *   https://www.twilio.com/docs/voice/api/conference-participant
      */
    def withMuted(muted: Boolean): BuilderWithSameTypes = copy(muted = Some(muted))

    /** This attribute tells a conference to start when this participant joins the conference, if it
      * is not already started.
      *
      * This is true by default.
      *
      * If this is false and the participant joins a conference that has not started, they are muted
      * and hear background music until a participant joins where startConferenceOnEnter is true.
      * This is useful for implementing moderated conferences.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-startConferenceOnEnter
      */
    def withStartConferenceOnEnter(startConferenceOnEnter: Boolean): BuilderWithSameTypes =
      copy(startConferenceOnEnter = Some(startConferenceOnEnter))

    /** If a participant has this attribute set to true, then when that participant leaves, the
      * conference ends and all other participants drop out.
      *
      * This defaults to false.
      *
      * This is useful for implementing moderated conferences that bridge two calls and allow either
      * call leg to continue executing TwiML if the other hangs up.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-endConferenceOnExit
      */
    def withEndConferenceOnExit(endConferenceOnExit: Boolean): BuilderWithSameTypes =
      copy(endConferenceOnExit = Some(endConferenceOnExit))

    /** A unique label for the participant which will be added into the conference as a result of
      * executing the TwiML.
      *
      * The label provided here can be used subsequently to read or update participant attributes
      * using the Twilio REST API. The participantLabel must be unique across all participants in
      * the conference, and there is a max limit of 128 characters.
      *
      * If a participant with the same label already exists in the conference, 16025 error
      * notification will be reported, and visible on Twilio Console. The call will not be added
      * into the conference and instead continue to the next TwiML verb.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-participantLabel
      */
    def withParticipantLabel(participantLabel: Conference.Participant.Label): BuilderWithSameTypes =
      copy(participantLabel = Some(participantLabel))

    /** Set the jitter buffer size for the participants.
      *
      * See documentation on [[com.dixa.twilio.model.voice.Conference.Participant.JitterBufferSize]]
      * for details.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-jitterBufferSize
      */
    def withJitterBufferSize(
        jitterBufferSize: Conference.Participant.JitterBufferSize
    ): BuilderWithSameTypes = copy(jitterBufferSize = Some(jitterBufferSize))

    /** Set a empty wait URL. Note that this is not the same, as not setting it.
      *
      * By default if we omit the wait url, Twilio will play default waiting music, but by explicit
      * setting it to a empty value, no waiting music will be played.
      *
      * You will not be allowed to call this, if you already called [[withWaitUrl]]
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-waitUrl
      */
    def withWaitUrlEmpty()(
        implicit ev: WaitUrlSet =:= PhantomTypes.waitUrlSetFalse
    ): Builder[
      ConferenceFriendlyNameSet,
      WaitUrlSet,
      PhantomTypes.WaitUrlEmptySetTrue,
      RecordStartSet,
      StatusCallbackSet,
      RecordStatusCallbackSet
    ] =
      copy(waitUrl = Some(""))

    /** The waitUrl attribute lets you specify a URL for music that plays before the conference has
      * started. The URL may return an MP3 file, a WAV file, or a TwiML document that contains
      * <Play>, <Say>, <Pause>, or <Redirect> verbs. If the waitUrl responds with TwiML, <Record>,
      * <Dial>, <Hangup>, and <Gather> verbs are not allowed. This defaults to a selection of
      * Creative Commons licensed background music, but you can replace it with your own music and
      * messages. If you do not wish anything to play while waiting for the conference to start,
      * specify the empty string (set waitUrl to "").
      *
      * If no waitUrl is specified, Twilio will use its own HoldMusic Twimlet that reads a public
      * AWS S3 Bucket for audio files. The default waitUrl is:
      * http://twimlets.com/holdmusic?Bucket=com.twilio.music.classical
      *
      * Please note, if the request to your waitUrl fails, the Conference will not be fully
      * established. To avoid the call being disconnected, you can either add additional TwiML after
      * the initial <Dial> <Conference>, or programmatically provide fallback behavior via the
      * action callback.
      *
      * You will not be allowed to call this, if you already called [[withWaitUrlEmpty]]
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-waitUrl
      */
    def withWaitUrl(
        waitUrl: CallbackUrl
    )(
        implicit ev: WaitUrlEmptySet =:= PhantomTypes.WaitUrlEmptySetFalse
    ): Builder[
      ConferenceFriendlyNameSet,
      PhantomTypes.WaitUrlSetTrue,
      WaitUrlEmptySet,
      RecordStartSet,
      StatusCallbackSet,
      RecordStatusCallbackSet
    ] =
      copy(waitUrl = Some(waitUrl.toString))

    /** This attribute indicates which HTTP method to use when requesting waitUrl.
      *
      * It defaults to 'POST'. Be sure to use 'GET' if you are directly requesting static audio
      * files such as WAV or MP3 files so that Twilio properly caches the files.
      *
      * You cannot call this, unless you have called [[withWaitUrl]] first.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-waitMethod
      */
    def withWaitMethod(waitMethod: HttpMethod)(
        implicit ev: WaitUrlSet =:= PhantomTypes.WaitUrlSetTrue
    ): BuilderWithSameTypes =
      copy(waitMethod = Some(waitMethod))

    /** This attribute indicates the maximum number of participants you want to allow within a named
      * conference room. The maximum number of participants is 250.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-maxParticipants
      */
    def withMaxParticipants(maxParticipants: Conference.MaxParticipants): BuilderWithSameTypes =
      copy(maxParticipants = Some(maxParticipants))

    /** The record attribute lets you record an entire <Conference>
      *
      * When set to record-from-start (as this method does), the recording begins when the first two
      * participants are bridged. The hold music is never recorded. If a recordingStatusCallback URL
      * is given, Twilio will make a request to the specified URL with recording details when the
      * recording is available to access.
      *
      * Note that there is not method for setting it to not record, as that is the default behavior.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#record
      */
    def withRecordFromStart(): Builder[
      ConferenceFriendlyNameSet,
      WaitUrlSet,
      WaitUrlEmptySet,
      PhantomTypes.RecordStartSetTrue,
      StatusCallbackSet,
      RecordStatusCallbackSet
    ] =
      copy(record = Some(Conference.Record.RecordFromStart))

    /** The region attribute specifies the region where Twilio should mix the conference. Specifying
      * a value for region overrides Twilio's automatic region selection logic and should only be
      * used if you are confident you understand where your conferences should be mixed. Twilio sets
      * the region parameter from the first participant that specifies the parameter and will ignore
      * the parameter from subsequent participants.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-region
      */
    def withRegion(region: Region): BuilderWithSameTypes = copy(region = Some(region))

    /** The trim attribute lets you specify whether to trim leading and trailing silence from your
      * audio files. trim defaults to trim-silence, which removes any silence at the beginning or
      * end of your recording. This may cause the duration of the recording to be slightly less than
      * the duration of the call.
      *
      * Can only be called if [[withRecordFromStart]] has been called first.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-trim
      */
    def withTrim(trim: Conference.Trim)(
        implicit ev: RecordStartSet =:= PhantomTypes.RecordStartSetTrue
    ): BuilderWithSameTypes = copy(trim = Some(trim))

    /** Coach accepts a call SID of a call that is currently connected to an in-progress conference.
      * Specifying a call SID that does not exist or is no longer connected to the conference will
      * result in the call failing to the action URL and throwing a 13240 error.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-coach
      */
    def withCoach(callSid: Call.Sid): BuilderWithSameTypes = copy(coach = Some(callSid))

    /** The statusCallbackEvent attribute allows you to specify which conference state changes
      * should generate a Webhook to the URL specified in the statusCallback attribute. The
      * available values are start, end, join, leave, mute, hold, modify, speaker, and announcement.
      * To specify multiple values separate them with a space. Events are set by the first
      * Participant to join the conference, subsequent statusCallbackEvents will be ignored. If you
      * specify conference events you can see a log of the events fired for a given conference in
      * the conference logs in the console.
      *
      * You can only call this, if you have already called [[withStatusCallback]]
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-statusCallbackEvent
      */
    def withStatusCallbackEvent(
        events: Iterable[Conference.StatusCallbackEvent]
    )(
        implicit ev: StatusCallbackSet =:= PhantomTypes.StatusCallbackSetTrue
    ): BuilderWithSameTypes = {
      val sorted = events.toSeq.sortBy(_.documentationOrder)
      copy(statusCallbackEvent = Some(sorted))
    }

    /** he statusCallback attribute takes a URL as an argument. Conference events specified in the
      * statusCallbackEvent parameter will be sent to this URL.
      *
      * The statusCallback URL is set by the first Participant to join the conference, subsequent
      * setting of the statusCallback will be ignored.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-statusCallback
      */
    def withStatusCallback(callback: CallbackUrl): Builder[
      ConferenceFriendlyNameSet,
      WaitUrlSet,
      WaitUrlEmptySet,
      RecordStartSet,
      PhantomTypes.StatusCallbackSetTrue,
      RecordStatusCallbackSet
    ] =
      copy(statusCallback = Some(callback))

    /** The HTTP method Twilio should use when requesting the above URL. Defaults to POST
      *
      * You can only call this, if you have already called [[withStatusCallback]]
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-statusCallbackMethod
      */
    def withStatusCallbackMethod(callbackMethod: HttpMethod)(
        implicit ev: StatusCallbackSet =:= PhantomTypes.StatusCallbackSetTrue
    ): BuilderWithSameTypes =
      copy(statusCallbackMethod = Some(callbackMethod))

    /** The recordingStatusCallback attribute takes a relative or absolute URL as an argument.
      *
      * If a conference recording was requested via the record attribute and a
      * recordingStatusCallback URL is given, Twilio will make a GET or POST request to the
      * specified URL when the recording is available to access.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-recording-status-callback
      */
    def withRecordingStatusCallback(callback: CallbackUrl)(
        implicit ev: RecordStartSet =:= PhantomTypes.RecordStartSetTrue
    ): Builder[
      ConferenceFriendlyNameSet,
      WaitUrlSet,
      WaitUrlEmptySet,
      RecordStartSet,
      StatusCallbackSet,
      PhantomTypes.RecordStatusCallbackSetTrue
    ] =
      copy(recordingStatusCallback = Some(callback))

    /** This attribute indicates which HTTP method to use when requesting recordingStatusCallback.
      * It defaults to 'POST'.
      *
      * Can only be called if [[withRecordingStatusCallback]] has already been called.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-recording-status-callback-method
      */
    def withRecordingStatusCallbackMethod(method: HttpMethod)(
        implicit ev: RecordStatusCallbackSet =:= PhantomTypes.RecordStatusCallbackSetTrue
    ): BuilderWithSameTypes =
      copy(recordingStatusCallbackMethod = Some(method))

    def withRecordingStatusCallbackEvent(
        events: Iterable[Conference.RecordingStatusCallbackEvent]
    )(
        implicit ev: RecordStatusCallbackSet =:= PhantomTypes.RecordStatusCallbackSetTrue
    ): BuilderWithSameTypes = {
      val sorted = events.toSeq.sortBy(_.documentationOrder)
      copy(recordingStatusCallbackEvent = Some(sorted))
    }

    def build()(
        implicit evb: ConferenceFriendlyNameSet =:= PhantomTypes.ConferenceFriendlyNameSetTrue
    ): ConferenceNoun =
      ConferenceNounImpl(
        beep,
        conferenceFriendlyName,
        muted,
        startConferenceOnEnter,
        endConferenceOnExit,
        participantLabel,
        jitterBufferSize,
        waitUrl,
        waitMethod,
        maxParticipants,
        record,
        region,
        trim,
        coach,
        statusCallbackEvent,
        statusCallback,
        statusCallbackMethod,
        recordingStatusCallback,
        recordingStatusCallbackMethod,
        recordingStatusCallbackEvent
      )

  }

  type BuilderStartState =
    Builder[
      PhantomTypes.ConferenceFriendlyNameSetFalse,
      PhantomTypes.waitUrlSetFalse,
      PhantomTypes.WaitUrlEmptySetFalse,
      PhantomTypes.RecordStartSetFalse,
      PhantomTypes.StatusCallbackSetFalse,
      PhantomTypes.RecordStatusCallbackSetFalse
    ]
  type BuildFunction = BuilderStartState => ConferenceNoun

  object Builder {
    val empty =
      new BuilderStartState(
        None,
        Conference.FriendlyName(""),
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None
      )
  }

  def build(fun: BuildFunction): ConferenceNoun = fun(Builder.empty)

  private final case class ConferenceNounImpl(
      beep: Option[Conference.Beep],
      conferenceFriendlyName: Conference.FriendlyName,
      muted: Option[Boolean],
      startConferenceOnEnter: Option[Boolean],
      endConferenceOnExit: Option[Boolean],
      participantLabel: Option[Conference.Participant.Label],
      jitterBufferSize: Option[Conference.Participant.JitterBufferSize],
      waitUrl: Option[String],
      waitMethod: Option[HttpMethod],
      maxParticipants: Option[Conference.MaxParticipants],
      record: Option[Conference.Record],
      region: Option[Region],
      trim: Option[Conference.Trim],
      coach: Option[Call.Sid],
      statusCallbackEvent: Option[Seq[Conference.StatusCallbackEvent]],
      statusCallback: Option[CallbackUrl],
      statusCallbackMethod: Option[HttpMethod],
      recordingStatusCallback: Option[CallbackUrl],
      recordingStatusCallbackMethod: Option[HttpMethod],
      recordingStatusCallbackEvent: Option[Seq[Conference.RecordingStatusCallbackEvent]]
  ) extends ConferenceNoun {

    override protected def tagName: String = "Conference"

    override protected def tagAttributes: immutable.Seq[(String, String)] =
      new TagAttributeBuilder()
        .addBoolean("muted", muted)
        .add("beep", beep)
        .addBoolean("startConferenceOnEnter", startConferenceOnEnter)
        .addBoolean("endConferenceOnExit", endConferenceOnExit)
        .add("participantLabel", participantLabel)
        .add("jitterBufferSize", jitterBufferSize)
        .addString("waitUrl", waitUrl)
        .add("waitMethod", waitMethod)
        .add("maxParticipants", maxParticipants)
        .add("record", record)
        .add("region", region)
        .add("trim", trim)
        .add("coach", coach)
        .addString("statusCallbackEvent", statusCallbackEvent.map(_.mkString(" ")))
        .add("statusCallback", statusCallback)
        .add("statusCallbackMethod", statusCallbackMethod)
        .add("recordingStatusCallback", recordingStatusCallback)
        .add("recordingStatusCallbackMethod", recordingStatusCallbackMethod)
        .addString(
          "recordingStatusCallbackEvent",
          recordingStatusCallbackEvent.map(_.mkString(" "))
        )
        .build

    override protected def tagSubElements: immutable.Seq[TwimlElement] = Nil

    override protected def tagValue: Option[String] = Some(conferenceFriendlyName.toString)
  }
}
