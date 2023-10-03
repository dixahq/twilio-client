package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.PositiveInteger
import org.scalatest.wordspec.AnyWordSpec

final class ResponsePauseTest extends AnyWordSpec {
  s"${classOf[Response].getSimpleName}" when {

    "Constructing responses with Pause directives" should {

      "Be able to construct a response with a simple Pause directive in a typesafe manner using default timeout" in {

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addPause { pauseBuilder =>
              pauseBuilder.build()
            }
            .buildVerified()
        }

        val xmlCompact = result.xmlCompact
        val expectedXmlCompact =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Pause/></Response>"""
        assert(xmlCompact == expectedXmlCompact)

        val xmlPretty = result.xmlPretty
        val expectedXmlPretty =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Pause />
             |</Response>""".stripMargin
        assert(xmlPretty === expectedXmlPretty)
      }

      "Be able to construct a response with a simple Pause directive in a typesafe manner using custom length from integer" in {

        val length = PositiveInteger.unsafe(74)
        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addPause { pauseBuilder =>
              pauseBuilder.withLengthInSeconds(length).build()
            }
            .buildVerified()
        }

        val xmlCompact = result.xmlCompact
        val expectedXmlCompact =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Pause length="74"/></Response>"""
        assert(xmlCompact == expectedXmlCompact)

        val xmlPretty = result.xmlPretty
        val expectedXmlPretty =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Pause length="74" />
             |</Response>""".stripMargin
        assert(xmlPretty === expectedXmlPretty)
      }

      "Be able to construct a response with a simple Pause directive in a typesafe manner using custom length from Duration" in {

        val duration = java.time.Duration.ofSeconds(2345)
        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addPause { pauseBuilder =>
              pauseBuilder.withLength(duration).build()
            }
            .buildVerified()
        }

        val xmlCompact = result.xmlCompact
        val expectedXmlCompact =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Pause length="2345"/></Response>"""
        assert(xmlCompact == expectedXmlCompact)

        val xmlPretty = result.xmlPretty
        val expectedXmlPretty =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Pause length="2345" />
             |</Response>""".stripMargin
        assert(xmlPretty === expectedXmlPretty)
      }
    }
  }
}
