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

package com.dixa.twilio.model.content

import com.dixa.twilio.model.EnumWithTwilioString
import com.dixa.twilio.model.iam.TwilioAccount

import scala.collection.immutable

final case class ContentApproval(
    sid: ContentTemplate.Sid,
    accountSid: Option[TwilioAccount.Sid],
    whatsapp: Option[ContentApproval.WhatsappApproval]
)

object ContentApproval {

  final case class WhatsappApproval(
      name: Option[String],
      category: Option[String],
      contentType: Option[String],
      status: ApprovalStatus,
      rejectionReason: Option[String],
      allowCategoryChange: Boolean
  )

  sealed abstract class ApprovalStatus(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object ApprovalStatus extends EnumWithTwilioString[ApprovalStatus] {
    override val values: immutable.IndexedSeq[ApprovalStatus] = findValues
    case object Unsubmitted extends ApprovalStatus("unsubmitted")
    case object Received    extends ApprovalStatus("received")
    case object Pending     extends ApprovalStatus("pending")
    case object Approved    extends ApprovalStatus("approved")
    case object Rejected    extends ApprovalStatus("rejected")
    case object Disabled    extends ApprovalStatus("disabled")
    case object Paused      extends ApprovalStatus("paused")
  }

  sealed abstract class WhatsappCategory(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object WhatsappCategory extends EnumWithTwilioString[WhatsappCategory] {
    override val values: immutable.IndexedSeq[WhatsappCategory] = findValues
    case object Utility        extends WhatsappCategory("UTILITY")
    case object Marketing      extends WhatsappCategory("MARKETING")
    case object Authentication extends WhatsappCategory("AUTHENTICATION")
  }
}
