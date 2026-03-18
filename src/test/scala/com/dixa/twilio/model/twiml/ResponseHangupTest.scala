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

package com.dixa.twilio.model.twiml

import org.scalatest.wordspec.AnyWordSpec
import com.dixa.twilio.model.callback.CallbackUrl

final class ResponseHangupTest extends AnyWordSpec {

  s"${classOf[Response].getSimpleName}" when {

    "constructing a response with Hangup" should {

      "be able to create the Response" in {
        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder.addHangup(_.build()).buildVerified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Hangup />
             |</Response>""".stripMargin
        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Hangup/></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "not allow more verbs to be added to the response after a hangup" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |        responseBuilder
            |          .addHangup(_.build())
            |          .addSay(_.withText("hefntfw").build())
            |          .buildVerified
            |      }
            |""".stripMargin
        )
      }

      "not allow adding a hangup if something else prohibits more verbs to be added" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |        responseBuilder
            |          .addRedirect(_.withCallbackUrl(CallbackUrl("http://localhost")).build())
            |          .addHangup(_.build())
            |          .buildVerified
            |      }
            |""".stripMargin
        )
      }
    }

  }
}
