package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.model.twiml.verb.{HangupVerb, PauseVerb, SayVerb}
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

      "not allow clients to call copy method on returned instance, as that could bypass constraints" in {
        assertTypeError(
          """val expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Say>Hello World</Say></Response>"
            |val i: Response.UnverifiedFromString = Response.fromString(expectedXml)
            |i.copy("")
            |""".stripMargin
        )
      }
    }

    "not allow clients to create a Verified instance directly" in {
      assertDoesNotCompile(
        """val createdFromConstructor = new Response.Verified(Seq.empty)"""
      )
      assertDoesNotCompile("""val createdFromApplyMethod = Response.Verified(Seq.empty)""")
    }

    "not allow clients to have copy methods on the returned Verified instance, as that would " +
      "be a way to produce invalid instances" in {
        assertTypeError("""val i = Response.build(_.addSay(_.withText("a").build()).buildVerified())
                          |i.copy(verbs = Seq.empty)
                          |""".stripMargin)
      }

    "constructed from a builder but including pre build Verb elements" should {

      "not allow clients to create a UnverifiedFromModel instance directly" in {
        assertDoesNotCompile(
          """val createdFromConstructor = new Response.UnverifiedFromModel(Seq.empty)"""
        )
        assertDoesNotCompile(
          """val createdFromApplyMethod = Response.UnverifiedFromModel(Seq.empty)"""
        )
      }

      "not be allowed to call copy on returned instance, as that could by pass the constraints" in {
        assertTypeError("""final class TestCustomVerb extends TwimlElement.Verb {
                          |  override def xmlCompact: String = "<CustomVerb>Hello<CustomVerb>"
                          |  override def xmlPretty: String  = xmlCompact
                          |}
                          |
                          |val i = Response.build { responseBuilder =>
                          |  responseBuilder.addCustomVerb(new TestCustomVerb).buildUnverified()
                          |}
                          |i.copy(verbs = Seq.empty)
                          |""".stripMargin)
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

      "support creating Response from a single completly custom verb" in {
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

      "support appending multiple verbs from an Seq to Response" in {
        val sayVerb: TwimlElement.Verb   = SayVerb.build(_.withText("aa").build())
        val pauseVerb: TwimlElement.Verb = PauseVerb.build(_.build())
        val hangupVerb                   = HangupVerb.build(_.build())
        val verbSeq                      = List(sayVerb, pauseVerb, hangupVerb)
        val result: Response.UnverifiedFromModel = Response.build { responseBuilder =>
          responseBuilder
            .addDial(_.withPhoneNumber(PhoneNumberE164("+4522334455").get).build())
            .addCustomVerbs(verbSeq)
            .buildUnverified()
        }
        assert(result.isInstanceOf[Response.FromModel])
        assert(
          result.xmlCompact ==
            s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial>+4522334455</Dial>${sayVerb.xmlCompact}${pauseVerb.xmlCompact}${hangupVerb.xmlCompact}</Response>""".stripMargin
        )
      }
    }
  }
}
