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
