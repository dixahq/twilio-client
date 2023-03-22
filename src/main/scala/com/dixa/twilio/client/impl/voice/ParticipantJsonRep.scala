package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Conference.Participant
import com.dixa.twilio.model.voice.{Call, Conference}

import java.time.Instant

private[voice] final case class ParticipantJsonRep(
    account_sid: String,
    call_sid: String,
    label: Option[String],
    conference_sid: String,
    date_created: String,
    date_updated: String,
    end_conference_on_exit: Boolean,
    muted: Boolean,
    hold: Boolean,
    status: String,
    start_conference_on_enter: Boolean,
    coaching: Boolean,
    call_sid_to_coach: Option[String]
) {
  private[voice] def toModel: Participant = {
    Participant(
      accountSid = TwilioAccount.Sid.unsafe(account_sid),
      callSid = Call.Sid.unsafe(call_sid),
      label = label.map(Participant.Label),
      callSidToCoach = call_sid_to_coach.map(Call.Sid.unsafe),
      coaching = coaching,
      conferenceSid = Conference.Sid.unsafe(conference_sid),
      dateCreated = Instant.from(Formatter.dateTime.parse(date_created)),
      dateUpdated = Instant.from(Formatter.dateTime.parse(date_updated)),
      endConferenceOnExit = end_conference_on_exit,
      muted = muted,
      hold = hold,
      startConferenceOnEnter = start_conference_on_enter,
      status = Participant.Status.fromTwilioStringUnsafe(status)
    )
  }
}
