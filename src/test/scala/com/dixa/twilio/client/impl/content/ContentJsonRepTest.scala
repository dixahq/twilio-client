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

package com.dixa.twilio.client.impl.content

import com.dixa.twilio.model.content.{ContentApproval, ContentTemplate, ContentType}
import com.dixa.twilio.model.iam.TwilioAccount
import org.scalatest.wordspec.AnyWordSpec

import java.time.Instant

final class ContentJsonRepTest extends AnyWordSpec {

  private val accountSid  = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
  private val contentSid  = ContentTemplate.Sid.unsafe("HXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
  private val dateCreated = Instant.parse("2026-06-18T08:45:32Z")
  private val dateUpdated = Instant.parse("2026-06-23T14:10:40Z")

  "ContentJsonRep.parseContentTemplate" should {

    "parse a twilio/text template" in {
      val json = ujson.read(s"""{
        "account_sid": "$accountSid",
        "sid": "$contentSid",
        "friendly_name": "my_template",
        "language": "en",
        "variables": { "1": "John" },
        "types": { "twilio/text": { "body": "Hello {{1}}" } },
        "date_created": "2026-06-18T08:45:32Z",
        "date_updated": "2026-06-23T14:10:40Z"
      }""")

      val result = ContentJsonRep.parseContentTemplate(json)
      assert(
        result === Right(
          ContentTemplate(
            sid = contentSid,
            accountSid = accountSid,
            friendlyName = "my_template",
            language = "en",
            variables = Map("1" -> "John"),
            types = Map("twilio/text" -> ContentType.Text("Hello {{1}}")),
            dateCreated = dateCreated,
            dateUpdated = dateUpdated
          )
        )
      )
    }

    "parse a twilio/media template with body and media URLs" in {
      val json = ujson.read(s"""{
        "account_sid": "$accountSid",
        "sid": "$contentSid",
        "friendly_name": "media_template",
        "language": "en",
        "variables": {},
        "types": {
          "twilio/media": {
            "body": "Here is your receipt",
            "media": ["https://example.com/receipt.pdf"]
          }
        },
        "date_created": "2026-06-18T08:45:32Z",
        "date_updated": "2026-06-23T14:10:40Z"
      }""")

      val result = ContentJsonRep.parseContentTemplate(json)
      assert(
        result === Right(
          ContentTemplate(
            sid = contentSid,
            accountSid = accountSid,
            friendlyName = "media_template",
            language = "en",
            variables = Map.empty,
            types = Map(
              "twilio/media" -> ContentType.Media(
                body = Some("Here is your receipt"),
                media = List("https://example.com/receipt.pdf")
              )
            ),
            dateCreated = dateCreated,
            dateUpdated = dateUpdated
          )
        )
      )
    }

    "parse a twilio/media template with null body" in {
      val json = ujson.read(s"""{
        "account_sid": "$accountSid",
        "sid": "$contentSid",
        "friendly_name": "media_no_body",
        "language": "en",
        "variables": {},
        "types": {
          "twilio/media": {
            "body": null,
            "media": ["https://example.com/image.jpg", "https://example.com/image2.jpg"]
          }
        },
        "date_created": "2026-06-18T08:45:32Z",
        "date_updated": "2026-06-23T14:10:40Z"
      }""")

      val result = ContentJsonRep.parseContentTemplate(json)
      assert(
        result.toOption.flatMap(_.types.get("twilio/media")) === Some(
          ContentType.Media(
            body = None,
            media = List("https://example.com/image.jpg", "https://example.com/image2.jpg")
          )
        )
      )
    }

    "parse an unsupported content type as Unknown preserving raw JSON" in {
      val json = ujson.read(s"""{
        "account_sid": "$accountSid",
        "sid": "$contentSid",
        "friendly_name": "future_template",
        "language": "en",
        "variables": {},
        "types": { "twilio/future": { "foo": "bar" } },
        "date_created": "2026-06-18T08:45:32Z",
        "date_updated": "2026-06-23T14:10:40Z"
      }""")

      val result = ContentJsonRep.parseContentTemplate(json)
      result.toOption.flatMap(_.types.get("twilio/future")) match {
        case Some(ContentType.Unknown(typeKey, rawJson)) =>
          assert(typeKey === "twilio/future")
          assert(ujson.read(rawJson)("foo").str === "bar")
        case other => fail(s"Expected Unknown, got $other")
      }
    }

    "use empty Map when variables field is null" in {
      val json = ujson.read(s"""{
        "account_sid": "$accountSid",
        "sid": "$contentSid",
        "friendly_name": "no_vars",
        "language": "en",
        "variables": null,
        "types": { "twilio/text": { "body": "Hi" } },
        "date_created": "2026-06-18T08:45:32Z",
        "date_updated": "2026-06-23T14:10:40Z"
      }""")

      val result = ContentJsonRep.parseContentTemplate(json)
      assert(result.map(_.variables) === Right(Map.empty))
    }

    "return Left when required field is missing" in {
      val json   = ujson.read("""{ "sid": "not-a-valid-sid" }""")
      val result = ContentJsonRep.parseContentTemplate(json)
      assert(result.isLeft)
    }
  }

  "ContentJsonRep.parseContentTemplateWithApproval" should {

    "parse an unsubmitted approval_requests" in {
      val json = ujson.read(s"""{
        "account_sid": "$accountSid",
        "sid": "$contentSid",
        "friendly_name": "my_template",
        "language": "en",
        "variables": {},
        "types": { "twilio/text": { "body": "Hi" } },
        "date_created": "2026-06-18T08:45:32Z",
        "date_updated": "2026-06-23T14:10:40Z",
        "approval_requests": {
          "allow_category_change": true,
          "category": "",
          "content_type": "",
          "direct_send": null,
          "flows": null,
          "name": "",
          "rejection_reason": "",
          "send_ttl_seconds": null,
          "status": "unsubmitted"
        }
      }""")

      val result = ContentJsonRep.parseContentTemplateWithApproval(json)
      assert(
        result.map(_.approvals) === Right(
          Some(
            ContentApproval.WhatsappApproval(
              name = "",
              category = "",
              contentType = "",
              status = ContentApproval.ApprovalStatus.Unsubmitted,
              rejectionReason = None,
              allowCategoryChange = true
            )
          )
        )
      )
    }

    "parse an approved approval_requests" in {
      val json = ujson.read(s"""{
        "account_sid": "$accountSid",
        "sid": "$contentSid",
        "friendly_name": "my_template",
        "language": "en",
        "variables": {},
        "types": { "twilio/text": { "body": "Hi" } },
        "date_created": "2026-06-18T08:45:32Z",
        "date_updated": "2026-06-23T14:10:40Z",
        "approval_requests": {
          "allow_category_change": true,
          "category": "MARKETING",
          "content_type": "twilio/text",
          "direct_send": false,
          "flows": null,
          "name": "my_template",
          "rejection_reason": "",
          "send_ttl_seconds": null,
          "status": "approved"
        }
      }""")

      val result = ContentJsonRep.parseContentTemplateWithApproval(json)
      assert(
        result.map(_.approvals) === Right(
          Some(
            ContentApproval.WhatsappApproval(
              name = "my_template",
              category = "MARKETING",
              contentType = "twilio/text",
              status = ContentApproval.ApprovalStatus.Approved,
              rejectionReason = None,
              allowCategoryChange = true
            )
          )
        )
      )
    }

    "return None for approvals when approval_requests is null" in {
      val json = ujson.read(s"""{
        "account_sid": "$accountSid",
        "sid": "$contentSid",
        "friendly_name": "my_template",
        "language": "en",
        "variables": {},
        "types": { "twilio/text": { "body": "Hi" } },
        "date_created": "2026-06-18T08:45:32Z",
        "date_updated": "2026-06-23T14:10:40Z",
        "approval_requests": null
      }""")

      val result = ContentJsonRep.parseContentTemplateWithApproval(json)
      assert(result.map(_.approvals) === Right(None))
    }

    "populate rejection_reason when non-empty" in {
      val json = ujson.read(s"""{
        "account_sid": "$accountSid",
        "sid": "$contentSid",
        "friendly_name": "my_template",
        "language": "en",
        "variables": {},
        "types": { "twilio/text": { "body": "Hi" } },
        "date_created": "2026-06-18T08:45:32Z",
        "date_updated": "2026-06-23T14:10:40Z",
        "approval_requests": {
          "allow_category_change": false,
          "category": "UTILITY",
          "content_type": "twilio/text",
          "direct_send": null,
          "flows": null,
          "name": "my_template",
          "rejection_reason": "Template does not match the selected category",
          "send_ttl_seconds": null,
          "status": "rejected"
        }
      }""")

      val result = ContentJsonRep.parseContentTemplateWithApproval(json)
      assert(
        result.map(_.approvals.flatMap(_.rejectionReason)) ===
          Right(Some("Template does not match the selected category"))
      )
    }
  }

  "ContentJsonRep.parseApproval" should {

    "parse a full approval fetch response with whatsapp data" in {
      val json = ujson.read(s"""{
        "sid": "$contentSid",
        "account_sid": "$accountSid",
        "whatsapp": {
          "name": "my_template",
          "category": "UTILITY",
          "content_type": "twilio/text",
          "status": "approved",
          "rejection_reason": "",
          "allow_category_change": true
        }
      }""")

      val result = ContentJsonRep.parseApproval(json)
      assert(
        result === Right(
          ContentApproval(
            sid = contentSid,
            accountSid = Some(accountSid),
            whatsapp = Some(
              ContentApproval.WhatsappApproval(
                name = "my_template",
                category = "UTILITY",
                contentType = "twilio/text",
                status = ContentApproval.ApprovalStatus.Approved,
                rejectionReason = None,
                allowCategoryChange = true
              )
            )
          )
        )
      )
    }

    "return None for whatsapp when field is null" in {
      val json = ujson.read(s"""{
        "sid": "$contentSid",
        "account_sid": "$accountSid",
        "whatsapp": null
      }""")

      val result = ContentJsonRep.parseApproval(json)
      assert(result.map(_.whatsapp) === Right(None))
    }

    "return None for accountSid when field is absent" in {
      val json = ujson.read(s"""{
        "sid": "$contentSid",
        "whatsapp": null
      }""")

      val result = ContentJsonRep.parseApproval(json)
      assert(result.map(_.accountSid) === Right(None))
    }
  }

}
