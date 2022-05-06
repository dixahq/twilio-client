package com.dixa.twilio.model.twiml

import org.scalatest.wordspec.AnyWordSpec

//noinspection ComparingUnrelatedTypes
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
        assert(!result.isInstanceOf[Response.Verified])
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
        assert(!result.isInstanceOf[Response.Verified])
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

  }

}
