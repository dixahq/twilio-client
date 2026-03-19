// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.model.twiml.verb.{HangupVerb, PauseVerb, SayVerb}
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.immutable

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

      "should equal a verified response, if it was created from the verified instances xmlCompact" in {
        val verified   = Response.build(_.addSay(_.withText("Hello world").build()).buildVerified())
        val unverified = Response.fromString(verified.xmlCompact)
        assert(unverified == verified)
      }
    }

    "Allow creating of a UnverifiedFromModel instance from a Seq of Verbs" in {
      val input = immutable.Seq(
        SayVerb.build(_.withText("Hello").build()),
        HangupVerb.build(_.build())
      )
      val result: Response.UnverifiedFromModel = Response.fromVerbs(input)
      assert(result.verbs == input)
    }

    "toString should include type and xml, so that all that it indicates the difference, but still is easy comparable" should {

      "when created as a a UnverifiedFromModel instance" in {
        val input = immutable.Seq(
          SayVerb.build(_.withText("Hello").build()),
          HangupVerb.build(_.build())
        )
        val response: Response.UnverifiedFromModel = Response.fromVerbs(input)
        assert(response.toString == s"Response.UnverifiedFromModel(${response.xmlCompact})")
      }

      "when created from string" in {
        val xml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Say>Hello World</Say>
             |</Response>""".stripMargin
        val response: Response.UnverifiedFromString = Response.fromString(xml)
        assert(response.toString == s"Response.UnverifiedFromString(${response.xmlCompact})")
      }

      "when created from model" in {
        val response: Response.Verified = Response.build(_.addReject(_.build()).buildVerified())
        assert(response.toString == s"Response.Verified(${response.xmlCompact})")
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

      "support creating Response from a single completely custom verb" in {
        final class TestCustomVerb extends TwimlElement.Verb {
          override protected def tagName: String                                = "CustomVerb"
          override protected def tagAttributes: immutable.Seq[(String, String)] = Nil
          override protected def tagSubElements: immutable.Seq[TwimlElement]    = Nil
          override protected def tagValue: Option[String]                       = Some("Hello")
        }
        val result: Response.UnverifiedFromModel = Response.build { responseBuilder =>
          responseBuilder.addCustomVerb(new TestCustomVerb).buildUnverified()
        }
        assert(result.isInstanceOf[Response.FromModel])
        assert(
          result.xmlPretty ==
            s"""<?xml version="1.0" encoding="UTF-8"?>
               |<Response>
               |  <CustomVerb>Hello</CustomVerb>
               |</Response>""".stripMargin
        )
      }

      "support appending multiple verbs from an Seq to Response" in {
        val sayVerb: TwimlElement.Verb           = SayVerb.build(_.withText("aa").build())
        val pauseVerb: TwimlElement.Verb         = PauseVerb.build(_.build())
        val hangupVerb                           = HangupVerb.build(_.build())
        val verbSeq                              = List(sayVerb, pauseVerb, hangupVerb)
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
