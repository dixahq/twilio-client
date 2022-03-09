package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.TwilioConference
import com.dixa.twilio.model.voice.TwilioConference.TwilioConferenceWithParticipants

private[voice] object ConferenceJsonResp {

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
      status: String,
      friendly_name: String,
      account_sid: String,
      sid: String,
      subresource_uris: TwilioConferenceSubUrisRep
  ) {
    private[voice] def toModel(
        participants: Seq[TwilioConference.Participant]
    ): TwilioConferenceWithParticipants = {
      TwilioConferenceWithParticipants(
        TwilioConference.Sid(sid),
        TwilioConference.Status.fromApiName(status),
        TwilioConference.FriendlyName(friendly_name),
        TwilioAccount.Sid(account_sid),
        participants.toVector
      )
    }

    private[voice] def toModel: TwilioConference.DefaultImpl = {
      TwilioConference(
        TwilioConference.Sid(sid),
        TwilioConference.Status.fromApiName(status),
        TwilioConference.FriendlyName(friendly_name),
        TwilioAccount.Sid(account_sid)
      )
    }
  }
}
