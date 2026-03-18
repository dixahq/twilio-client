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

package com.dixa.twilio.model.iam

import com.dixa.twilio.model.EnumWithTwilioString
import com.dixa.twilio.model.general.{Application, ServiceSid}
import com.dixa.twilio.model.video.Room

import scala.collection.immutable

/** Represents a grant that defines which Twilio product and actions the Access Token holder is
  * permitted to use. Each grant is product-specific and is embedded in the Access Token's payload.
  * A single Access Token can carry multiple grants. More info:
  * https://www.twilio.com/docs/iam/access-tokens
  */
sealed trait TwilioGrant extends EnumWithTwilioString.EnumEntry

object TwilioGrant extends EnumWithTwilioString[TwilioGrant] {

  override def values: immutable.IndexedSeq[TwilioGrant] = findValues

  case class VoiceGrant(
      incomingAllow: Boolean = true,
      outgoingAppSid: Option[Application.Sid] = None
  ) extends TwilioGrant {
    override def twilioString: String = "voice"
  }

  case class ChatGrant(serviceSid: ServiceSid) extends TwilioGrant {
    override def twilioString: String = "chat"
  }

  case class SyncGrant(serviceSid: ServiceSid) extends TwilioGrant {
    override def twilioString: String = "sync"
  }

  case class VideoGrant(room: Option[Room] = None) extends TwilioGrant {
    override def twilioString: String = "video"
  }

  case class RawGrant(grantKey: String, json: String) extends TwilioGrant {
    override def twilioString: String = grantKey
  }
}
