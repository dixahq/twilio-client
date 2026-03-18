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

import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import org.scalatest.wordspec.AnyWordSpec

final class ResponseRedirectTest extends AnyWordSpec {

  s"${classOf[Response].getSimpleName}" when {

    "constructing a response with Redirect" should {

      "be able to create the redirect with defuault method attribute" in {
        val callbackUrl = CallbackUrl("relative/url")

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addRedirect { redirectBuilder =>
              redirectBuilder.withCallbackUrl(callbackUrl).build()
            }
            .buildVerified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Redirect>relative/url</Redirect>
             |</Response>""".stripMargin
        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Redirect>relative/url</Redirect></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }
    }

    "be able to create the redirect with explicit method attribute" in {
      val callbackUrl = CallbackUrl("relative/url")

      val result: Response.Verified = Response.build { responseBuilder =>
        responseBuilder
          .addRedirect { redirectBuilder =>
            redirectBuilder.withCallbackUrl(callbackUrl).withMethod(HttpMethod.Get).build()
          }
          .buildVerified()
      }

      val expectedPrettyXml =
        s"""<?xml version="1.0" encoding="UTF-8"?>
           |<Response>
           |  <Redirect method="GET">relative/url</Redirect>
           |</Response>""".stripMargin
      assert(result.xmlPretty === expectedPrettyXml)

      // format: off
      val expectedCompactXml =
        s"""<?xml version="1.0" encoding="UTF-8"?><Response><Redirect method="GET">relative/url</Redirect></Response>"""
      // format: on
      assert(result.xmlCompact == expectedCompactXml)
    }

    "be able to handle redirects with query parameters (including xml reserved & char" in {
      val callbackUrl           = CallbackUrl("relative/url$key1=value1&key2=value2")
      val callbackUrlXmlEscaped = "relative/url$key1=value1&amp;key2=value2"

      val result: Response.Verified = Response.build { responseBuilder =>
        responseBuilder
          .addRedirect { redirectBuilder =>
            redirectBuilder.withCallbackUrl(callbackUrl).build()
          }
          .buildVerified()
      }

      val expectedPrettyXml =
        s"""<?xml version="1.0" encoding="UTF-8"?>
           |<Response>
           |  <Redirect>$callbackUrlXmlEscaped</Redirect>
           |</Response>""".stripMargin
      assert(result.xmlPretty === expectedPrettyXml)

      // format: off
      val expectedCompactXml =
        s"""<?xml version="1.0" encoding="UTF-8"?><Response><Redirect>$callbackUrlXmlEscaped</Redirect></Response>"""
      // format: on
      assert(result.xmlCompact == expectedCompactXml)
    }

    "not allow more verbs to be added to the response after a redirect" in {
      assertTypeError(
        """val callbackUrl = CallbackUrl("relative/url")
          |
          |val result: Response.Verified = Response.build { responseBuilder =>
          |  responseBuilder.addRedirect { redirectBuilder =>
          |    redirectBuilder.withCallbackUrl(callbackUrl).withMethod(HttpMethod.Get).build()
          |  }.addSay(_.withText("Should not be allowed to add this").build())
          |    .buildVerified()
          |}
          |""".stripMargin
      )
    }
  }
}
