package com.dixa.twilio.model.twiml

import org.scalatest.wordspec.AnyWordSpec

final class ResponseMiscTest extends AnyWordSpec {
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

      "not allow clients to create a instance without using the fromString method" in {
        assertDoesNotCompile(
          """val createdFromConstructor = new Response.UnverifiedFromString("input")"""
        )
        assertDoesNotCompile(
          """val createdFromApplyMethod = Response.UnverifiedFromString("input")"""
        )
      }
    }

    "not allow clients to create a Verified instance directly" in {
      assertDoesNotCompile(
        """val createdFromConstructor = new Response.Verified(Seq.empty)"""
      )
      assertDoesNotCompile("""val createdFromApplyMethod = Response.Verified(Seq.empty)""")
    }

    "constructed from a builder but including a custome verb" should {

      "not allow clients to create a UnverifiedFromModel instance directly" in {
        assertDoesNotCompile(
          """val createdFromConstructor = new Response.UnverifiedFromModel(Seq.empty)"""
        )
        assertDoesNotCompile(
          """val createdFromApplyMethod = Response.UnverifiedFromModel(Seq.empty)"""
        )
      }

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

          override def xmlPretty: String = xmlCompact
        }
        val result: Response.UnverifiedFromModel = Response.build { responseBuilder =>
          responseBuilder.addCustomVerb(new TestCustomVerb).buildUnverified()
        }
        assert(result.isInstanceOf[Response.FromModel])
        assert(
          result.xmlPretty ==
            s"""<?xml version="1.0" encoding="UTF-8"?>
               |<Response>
               |  <CustomVerb>Hello<CustomVerb>
               |</Response>""".stripMargin
        )
      }
    }
  }
}
