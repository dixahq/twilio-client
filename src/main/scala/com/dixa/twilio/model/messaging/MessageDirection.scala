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

package com.dixa.twilio.model.messaging

import com.dixa.twilio.model
import com.dixa.twilio.model.EnumWithTwilioString

import scala.collection.immutable

sealed abstract class MessageDirection(override val twilioString: String)
    extends EnumWithTwilioString.EnumEntry
object MessageDirection extends model.EnumWithTwilioString[MessageDirection] {
  override def values: immutable.IndexedSeq[MessageDirection] = findValues

  case object Inbound       extends MessageDirection("inbound")
  case object OutboundApi   extends MessageDirection("outbound-api")
  case object OutboundCall  extends MessageDirection("outbound-call")
  case object OutboundReply extends MessageDirection("outbound-reply")
}
