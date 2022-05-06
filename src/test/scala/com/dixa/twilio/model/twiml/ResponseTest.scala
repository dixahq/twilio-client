package com.dixa.twilio.model.twiml

import org.scalatest.wordspec.AnyWordSpec

final class ResponseTest extends AnyWordSpec {

  s"${classOf[Response].getSimpleName}" when {

    "Constructing responses with Say directives" should {

      "Be able to construct a response with a simple Say directive in a typesafe manner" in {

        val textToSay = "Say something Twilio"
        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addSay { sayBuilder =>
              sayBuilder.withText(textToSay).build
            }
            .buildVerified()
        }

        val xmlCompact = result.xmlCompact
        val expectedXmlCompact =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Say>$textToSay</Say></Response>"""
        assert(xmlCompact == expectedXmlCompact)

        val xmlPretty = result.xmlPretty
        val expectedXmlPretty =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Say>$textToSay</Say>
             |</Response>""".stripMargin
        assert(xmlPretty === expectedXmlPretty)
      }
    }

  }

}
