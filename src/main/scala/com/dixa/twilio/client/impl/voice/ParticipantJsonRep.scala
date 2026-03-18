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

package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Conference.Participant
import com.dixa.twilio.model.voice.{Call, Conference}
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

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
    call_sid_to_coach: Option[String] = None
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

private[voice] object ParticipantJsonRep {

  implicit val participantJsonRepReader: Reader[ParticipantJsonRep] =
    macroR[ParticipantJsonRep]
}
