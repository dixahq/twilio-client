package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.voice.Conference
import org.scalatest.wordspec.AnyWordSpec

final class ResponseRedirectTest extends AnyWordSpec {

  s"${classOf[Response].getSimpleName}" when {

    "constructing a response with Redirect" should {

      "Be able to create the redirect with defuault method attribute" in {
        val callbackUrl = CallbackUrl("relative/url")

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder.addRedirect { redirectBuilder =>
            redirectBuilder.withCallbackUrl(callbackUrl).build
          }.buildVerified
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

    "Be able to create the redirect with explicit method attribute" in {
      val callbackUrl = CallbackUrl("relative/url")

      val result: Response.Verified = Response.build { responseBuilder =>
        responseBuilder.addRedirect { redirectBuilder =>
          redirectBuilder.withCallbackUrl(callbackUrl).withMethod(HttpMethod.Get).build
        }.buildVerified
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
  }
}
