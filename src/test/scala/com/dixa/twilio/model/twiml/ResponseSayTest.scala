package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.twiml.verb.SayVerb.{LanguageCode, Voice}
import org.scalatest.wordspec.AnyWordSpec

final class ResponseSayTest extends AnyWordSpec {
  s"${classOf[Response].getSimpleName}" when {

    "Constructing responses with Say directives" should {

      "Be able to construct a response with a simple Say directive in a typesafe manner," +
        " even when text includes reserved xml chars" in {

          val textToSay = """Twilio can you pronounce these reserved XML chars: "'<>&"""
          val textToSayEscaped =
            "Twilio can you pronounce these reserved XML chars: &quot;&apos;&lt;&gt;&amp;"

          val language = LanguageCode.`en-GB`
          val voice    = Voice.`woman`
          val result: Response.Verified = Response.build { responseBuilder =>
            responseBuilder
              .addSay { sayBuilder =>
                sayBuilder
                  .withText(textToSay)
                  .withLanguage(language)
                  .withVoice(voice)
                  .withLoop(5)
                  .build()
              }
              .buildVerified()
          }

          val xmlCompact = result.xmlCompact
          val expectedXmlCompact =
            s"""<?xml version="1.0" encoding="UTF-8"?><Response><Say language="en-GB" voice="woman" loop="5">$textToSayEscaped</Say></Response>"""
          assert(xmlCompact == expectedXmlCompact)

          val xmlPretty = result.xmlPretty
          val expectedXmlPretty =
            s"""<?xml version="1.0" encoding="UTF-8"?>
               |<Response>
               |  <Say language="en-GB" voice="woman" loop="5">$textToSayEscaped</Say>
               |</Response>""".stripMargin
          assert(xmlPretty === expectedXmlPretty)
        }
    }
  }
}
