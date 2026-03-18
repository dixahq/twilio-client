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

package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.twiml.verb.RejectVerb
import org.scalatest.wordspec.AnyWordSpec

final class ResponseRejectTest extends AnyWordSpec {

  s"${classOf[Response].getSimpleName}" when {

    "constructing a response with Reject" should {

      "be able to create the Reject without a reason" in {
        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder.addReject(_.build()).buildVerified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Reject />
             |</Response>""".stripMargin
        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Reject/></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "be able to create the Reject with reason rejected" in {
        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addReject(_.withReason(RejectVerb.RejectReason.Rejected).build())
            .buildVerified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Reject reason="rejected" />
             |</Response>""".stripMargin
        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Reject reason="rejected"/></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "be able to create the Reject with reason busy" in {
        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addReject(_.withReason(RejectVerb.RejectReason.Busy).build())
            .buildVerified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Reject reason="busy" />
             |</Response>""".stripMargin
        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Reject reason="busy"/></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "not allow more verbs to be added to the response after a reject" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |        responseBuilder
            |          .addReject(_.build())
            |          .addSay(_.withText("hefntfw").build())
            |          .buildVerified
            |      }
            |""".stripMargin
        )
      }

      "not allow adding a reject if something else prohibits more verbs to be added" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |        responseBuilder
            |          .addRedirect(_.withCallbackUrl(CallbackUrl("http://localhost")).build())
            |          .addReject(_.build())
            |          .buildVerified
            |      }
            |""".stripMargin
        )
      }
    }
  }
}
