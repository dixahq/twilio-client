package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.model.{ApiVersion, Region}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{Call, Conference}
import com.dixa.twilio.model.voice.Conference.ConferenceWithParticipants

import java.time.Instant

private[voice] object ConferenceJsonRep {

  /** Class representing the Twilio JSON representation of a sub object of a Conference.
    *
    * This is a sub resource of [[TwilioConferenceJsonResp]] so see that for details.
    */
  private[voice] final case class TwilioConferenceSubUrisRep(participants: String)

  /** Class representing the Twilio JSON representation of a conference.
    *
    * This is placed here in the conference package, as multiple request related to conferences will
    * return the JSON that this class represent.
    *
    * Only the fields needed has been mapped, but there are a lot more in the JSON from Twilio, so
    * just add extra fields whenever needed. Full descriptions of how the JSON looks like can be
    * found here: https://www.twilio.com/docs/voice/api/conference-resource
    */
  private[voice] final case class TwilioConferenceJsonResp(
      account_sid: String,
      date_created: String,
      date_updated: String,
      api_version: String,
      friendly_name: String,
      region: String,
      sid: String,
      status: String,
      reason_conference_ended: Option[String],
      call_sid_ending_conference: Option[String],
      subresource_uris: TwilioConferenceSubUrisRep
  ) {
    private[voice] def toModel(
        participants: Seq[Conference.Participant]
    ): ConferenceWithParticipants = {
      ConferenceWithParticipants(
        Conference.Sid.unsafe(sid),
        Conference.Status.fromTwilioStringUnsafe(status),
        Conference.FriendlyName(friendly_name),
        TwilioAccount.Sid.unsafe(account_sid),
        Instant.from(Formatter.dateTime.parse(date_created)),
        Instant.from(Formatter.dateTime.parse(date_updated)),
        ApiVersion(api_version),
        Region.fromTwilioStringUnsafe(region),
        reason_conference_ended.flatMap(Conference.EndReason.fromTwilioString),
        call_sid_ending_conference.map(Call.Sid.unsafe),
        participants.toVector
      )
    }

    private[voice] def toModel: Conference.DefaultImpl = {
      Conference(
        Conference.Sid.unsafe(sid),
        Conference.Status.fromTwilioStringUnsafe(status),
        Conference.FriendlyName(friendly_name),
        TwilioAccount.Sid.unsafe(account_sid),
        Instant.from(Formatter.dateTime.parse(date_created)),
        Instant.from(Formatter.dateTime.parse(date_updated)),
        ApiVersion(api_version),
        Region.fromTwilioStringUnsafe(region),
        reason_conference_ended.flatMap(Conference.EndReason.fromTwilioString),
        call_sid_ending_conference.map(Call.Sid.unsafe),
      )
    }
  }
}
