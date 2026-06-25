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

package com.dixa.twilio.client.twilioClient.content

import com.dixa.twilio.client.TwilioTestConstants
import com.dixa.twilio.model.content.{
  ContentApproval,
  ContentTemplate,
  ContentTemplateWithApproval,
  ContentType
}
import com.dixa.twilio.model.iam.TwilioAccount

import java.time.Instant

trait ContentSharedFixture {

  val accountSid: TwilioAccount.Sid = TwilioTestConstants.accountSid

  val contentSid: ContentTemplate.Sid =
    ContentTemplate.Sid.unsafe("HXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")

  val textTemplate: ContentTemplate = ContentTemplate(
    sid = contentSid,
    accountSid = accountSid,
    friendlyName = "dixa_support_ticket_changed",
    language = "en",
    variables = Map("1" -> "John Doe", "2" -> "123456"),
    types = Map(
      "twilio/text" -> ContentType.Text(
        "Hello, {{1}}.\n Thanks for contacting Dixa Support. Your ticket number is #{{2}}. We will be in touch shortly. Check for more contact options on our website."
      )
    ),
    dateCreated = Instant.parse("2026-06-18T08:45:32Z"),
    dateUpdated = Instant.parse("2026-06-23T14:10:40Z")
  )

  val unsubmittedApproval: ContentApproval.WhatsappApproval = ContentApproval.WhatsappApproval(
    name = "",
    category = "",
    contentType = "",
    status = ContentApproval.ApprovalStatus.Unsubmitted,
    rejectionReason = None,
    allowCategoryChange = true
  )

  val approvedApproval: ContentApproval.WhatsappApproval = ContentApproval.WhatsappApproval(
    name = "api_rating_request",
    category = "MARKETING",
    contentType = "twilio/text",
    status = ContentApproval.ApprovalStatus.Approved,
    rejectionReason = None,
    allowCategoryChange = true
  )

  val textTemplateWithUnsubmittedApproval: ContentTemplateWithApproval =
    ContentTemplateWithApproval(template = textTemplate, approvals = Some(unsubmittedApproval))

  // Shared JSON fragments

  def approvalRequestsUnsubmittedJson: String =
    """|"approval_requests": {
       |  "allow_category_change": true,
       |  "category": "",
       |  "content_type": "",
       |  "direct_send": null,
       |  "flows": null,
       |  "name": "",
       |  "rejection_reason": "",
       |  "send_ttl_seconds": null,
       |  "status": "unsubmitted"
       |}""".stripMargin

  def contentTemplateJson(sid: String = contentSid.toString): String =
    s"""|{
        |  "account_sid": "${accountSid}",
        |  "date_created": "2026-06-18T08:45:32Z",
        |  "date_updated": "2026-06-23T14:10:40Z",
        |  "friendly_name": "dixa_support_ticket_changed",
        |  "language": "en",
        |  "sid": "$sid",
        |  "types": {
        |    "twilio/text": {
        |      "body": "Hello, {{1}}.\\n Thanks for contacting Dixa Support. Your ticket number is #{{2}}. We will be in touch shortly. Check for more contact options on our website."
        |    }
        |  },
        |  "variables": { "1": "John Doe", "2": "123456" }
        |}""".stripMargin

  def contentTemplateWithApprovalJson(sid: String = contentSid.toString): String =
    s"""|{
        |  "account_sid": "${accountSid}",
        |  $approvalRequestsUnsubmittedJson,
        |  "date_created": "2026-06-18T08:45:32Z",
        |  "date_updated": "2026-06-23T14:10:40Z",
        |  "friendly_name": "dixa_support_ticket_changed",
        |  "language": "en",
        |  "sid": "$sid",
        |  "types": {
        |    "twilio/text": {
        |      "body": "Hello, {{1}}.\\n Thanks for contacting Dixa Support. Your ticket number is #{{2}}. We will be in touch shortly. Check for more contact options on our website."
        |    }
        |  },
        |  "variables": { "1": "John Doe", "2": "123456" }
        |}""".stripMargin

  def twilioAuthErrorJson: String =
    """|{
       |  "code": 20003,
       |  "detail": "Your AccountSid or AuthToken was incorrect.",
       |  "message": "Authentication Error - No credentials provided",
       |  "more_info": "https://www.twilio.com/docs/errors/20003",
       |  "status": 401
       |}""".stripMargin

  def twilioNotFoundJson: String =
    """|{
       |  "code": 20404,
       |  "message": "The requested resource was not found",
       |  "more_info": "https://www.twilio.com/docs/errors/20404",
       |  "status": 404
       |}""".stripMargin
}
