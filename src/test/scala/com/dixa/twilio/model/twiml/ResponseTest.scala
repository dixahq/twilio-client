package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.model.voice.Conference
import org.scalatest.wordspec.AnyWordSpec

import scala.annotation.nowarn

final class ResponseTest extends AnyWordSpec {

  s"${classOf[Response].getSimpleName}" when {

    "constructed from a pure string" should {

      "return UnverifiedFromString instance that use supplied xml as both pretty and compact print" in {
        val expectedXmlPretty =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Say>Hello World</Say>
             |</Response>""".stripMargin
        val result: Response.UnverifiedFromString = Response.fromString(expectedXmlPretty)
        assert(result.isInstanceOf[Response.Unverified])
        assert(result.xmlPretty === expectedXmlPretty)
        assert(result.xmlCompact === expectedXmlPretty)
      }
    }

    "constructed from a builder but including a custome verb" should {

      "not allow to call buildVerified" in {
        assertTypeError(
          """final class TestCustomVerb extends TwimlElement.Verb {
            |          override def xmlCompact: String = "<CustomVerb>Hello<CustomVerb>"
            |          override def xmlPretty: String  = xmlCompact
            |}
            |
            |Response.build { responseBuilder =>
            |          responseBuilder.addCustomVerb(new TestCustomVerb).buildVerified()
            |}
            |""".stripMargin
        )
      }

      "return a instance that is both FromModel and Unverified" in {
        final class TestCustomVerb extends TwimlElement.Verb {
          override def xmlCompact: String = """<CustomVerb>Hello<CustomVerb>"""
          override def xmlPretty: String  = xmlCompact
        }
        val result: Response.UnverifiedFromModel = Response.build { responseBuilder =>
          responseBuilder.addCustomVerb(new TestCustomVerb).buildUnverified()
        }
        assert(result.isInstanceOf[Response.FromModel])
        assert(result.xmlPretty == s"""<?xml version="1.0" encoding="UTF-8"?>
                                      |<Response>
                                      |  <CustomVerb>Hello<CustomVerb>
                                      |</Response>""".stripMargin)
      }
    }

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

    "constructing a respnse with Dial" should {

      "Be able to nest a conference within the dial" in {

        val conferenceFriendlyName = Conference.FriendlyName("Test_conference")

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder.addDial { dialBuilder =>
            dialBuilder.withConference { conferenceBuilder =>
              conferenceBuilder
                .withBeep(Conference.Beep.False)
                .withWaitUrlEmpty()
                .withConferenceFriendlyName(conferenceFriendlyName)
                .build
            }.build
          }.buildVerified
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Dial>
             |    <Conference beep="false" waitUrl="">$conferenceFriendlyName</Conference>
             |  </Dial>
             |</Response>""".stripMargin

        println(result.xmlPretty)
        assert(result.xmlPretty === expectedPrettyXml)
        
        // format: off
        val expectedCompactXml = 
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial><Conference beep="false" waitUrl="">$conferenceFriendlyName</Conference></Dial></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "Be able to nest a plain phonenumber within the dial" in {

        val pn = "+4533442255"
        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder.addDial { dialBuilder =>
            dialBuilder.withPhoneNumber(PhoneNumberE164(pn)).build
          }.buildVerified
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Dial>
             |    $pn
             |  </Dial>
             |</Response>""".stripMargin

        println(result.xmlPretty)
        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial>$pn</Dial></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }
    }

  }

}
