package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.dtmf.{DtmfDigit, DtmfString}
import org.scalatest.wordspec.AnyWordSpec

final class ResponsePlayTest extends AnyWordSpec {
  s"${classOf[Response].getSimpleName}" when {

    "Constructing responses with Play directives" should {

      "Be able to construct a response with a simple Play directive in a typesafe manner," +
        " that plays a sound file from a URL" in {

          val urlAsString = "https://www.dixa.com"
          val result: Response.Verified = Response.build { responseBuilder =>
            responseBuilder
              .addPlay { playBuilder =>
                playBuilder.withSoundFileUrl(urlAsString).build
              }
              .buildVerified()
          }

          val xmlCompact = result.xmlCompact
          val expectedXmlCompact =
            s"""<?xml version="1.0" encoding="UTF-8"?><Response><Play>$urlAsString</Play></Response>"""
          assert(xmlCompact == expectedXmlCompact)

          val xmlPretty = result.xmlPretty
          val expectedXmlPretty =
            s"""<?xml version="1.0" encoding="UTF-8"?>
               |<Response>
               |  <Play>$urlAsString</Play>
               |</Response>""".stripMargin
          assert(xmlPretty === expectedXmlPretty)
        }

      "should support playing DTMF digits" in {
        val result = Response.build { responseBuilder =>
          responseBuilder
            .addPlay { playBuilder =>
              playBuilder
                .withDigits(DtmfString(DtmfDigit.`1`, DtmfDigit.w, DtmfDigit.`*`))
                .build()
            }
            .buildVerified()
        }

        val xmlCompact = result.xmlCompact
        val expectedXmlCompact =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Play digits="1w*"></Play></Response>"""
        assert(xmlCompact == expectedXmlCompact)

        val xmlPretty = result.xmlPretty
        val expectedXmlPretty =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Play digits="1w*"></Play>
             |</Response>""".stripMargin
        assert(xmlPretty === expectedXmlPretty)
      }
    }
  }
}
