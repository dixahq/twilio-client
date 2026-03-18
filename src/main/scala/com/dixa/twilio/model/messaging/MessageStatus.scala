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

package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.EnumWithTwilioString

import scala.collection.immutable

sealed abstract class MessageStatus(override val twilioString: String)
    extends EnumWithTwilioString.EnumEntry
object MessageStatus extends EnumWithTwilioString[MessageStatus] {
  override val values: immutable.IndexedSeq[MessageStatus] = findValues

  case object Accepted    extends MessageStatus("accepted")
  case object Scheduled   extends MessageStatus("scheduled")
  case object Canceled    extends MessageStatus("canceled")
  case object Queued      extends MessageStatus("queued")
  case object Sending     extends MessageStatus("sending")
  case object Sent        extends MessageStatus("sent")
  case object Failed      extends MessageStatus("failed")
  case object Delivered   extends MessageStatus("delivered")
  case object Undelivered extends MessageStatus("undelivered")
  case object Receiving   extends MessageStatus("receiving")
  case object Received    extends MessageStatus("received")

  /** WhatsApp only */
  case object Read extends MessageStatus("read")
}
