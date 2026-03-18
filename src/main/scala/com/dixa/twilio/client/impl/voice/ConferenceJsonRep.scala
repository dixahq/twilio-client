// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.model.PublicEdgeLocation
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Conference.ConferenceWithParticipants
import com.dixa.twilio.model.voice.{Call, Conference}
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

import java.time.Instant

private[voice] object ConferenceJsonRep {

  /** Class representing the Twilio JSON representation of a sub object of a Conference.
    *
    * This is a sub resource of [[TwilioConferenceJsonResp]] so see that for details.
    */
  private[voice] final case class TwilioConferenceSubUrisRep(participants: String)

  private implicit val twilioConferenceSubUrisRepReader: Reader[TwilioConferenceSubUrisRep] =
    macroR[TwilioConferenceSubUrisRep]

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
      reason_conference_ended: Option[String] = None,
      call_sid_ending_conference: Option[String] = None,
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
        PublicEdgeLocation.withEdgeOrRegionId(region.toLowerCase),
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
        PublicEdgeLocation.withEdgeOrRegionId(region.toLowerCase),
        reason_conference_ended.flatMap(Conference.EndReason.fromTwilioString),
        call_sid_ending_conference.map(Call.Sid.unsafe),
      )
    }
  }

  private[voice] object TwilioConferenceJsonResp {

    implicit val upickleReader: Reader[TwilioConferenceJsonResp] =
      macroR[TwilioConferenceJsonResp]
  }
}
