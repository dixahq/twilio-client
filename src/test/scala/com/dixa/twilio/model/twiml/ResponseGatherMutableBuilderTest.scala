package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.dtmf.DtmfDigit
import com.dixa.twilio.model.twiml.verb._
import com.dixa.twilio.model.{HttpMethod, PositiveInteger}
import org.scalatest.wordspec.AnyWordSpec

final class ResponseGatherMutableBuilderTest extends AnyWordSpec {

  s"${classOf[Response].getSimpleName}" when {

    "constructing a response with Gather using the mutable builder that does not verify anything compile time" should {

      "be able to construct a default gather without attributes or nested elements" in {
        // According to documentation, all attributes on gather have default values, so it should be allowed to create an completely empty gather verb.
        val result: Response.Unverified = Response.build { responseBuilder =>
          responseBuilder
            .addCustomVerb(GatherVerb.BuilderMutable.empty().buildUnverified())
            .buildUnverified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Gather />
             |</Response>""".stripMargin

        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Gather/></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "be able to construct a Unverified instance by supplying the gather builder with a single custom verb" in {
        val result: Response.Unverified = Response.build { responseBuilder =>
          responseBuilder
            .addCustomVerb(
              GatherVerb.BuilderMutable
                .empty()
                .addCustomVerb(PauseVerb.build(_.build()))
                .buildUnverified()
            )
            .buildUnverified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Gather>
             |    <Pause />
             |  </Gather>
             |</Response>""".stripMargin

        println(result.xmlPretty)
        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Gather><Pause/></Gather></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "be able to construct a Unverified instance by supplying the gather builder with a Seq of custom verbs" in {
        val verbList = List(
          PauseVerb.build(_.build()),
          HangupVerb.build(_.build())
        )
        val result: Response.Unverified = Response.build { responseBuilder =>
          responseBuilder
            .addCustomVerb(
              GatherVerb.BuilderMutable
                .empty()
                .addSay(_.withText("aa").build())
                .addCustomVerbs(verbList)
                .buildUnverified()
            )
            .buildUnverified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Gather>
             |    <Say>aa</Say>
             |    <Pause />
             |    <Hangup />
             |  </Gather>
             |</Response>""".stripMargin

        println(result.xmlPretty)
        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Gather><Say>aa</Say><Pause/><Hangup/></Gather></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "be able to include all attribute and to nest pause, play and say verbs" in {
        val result: Response.UnverifiedFromModel = Response.build { responseBuilder =>
          responseBuilder
            .addCustomVerb {
              GatherVerb.BuilderMutable
                .empty()
                .addSay { sayBuilder: SayVerb.BuilderStartState =>
                  sayBuilder.withText("Say text").build()
                }
                .addPause { pauseBuilder: PauseVerb.BuilderStartState => pauseBuilder.build() }
                .addPlay { playBuilder: PlayVerb.BuilderStartState =>
                  playBuilder.withSoundFileUrl("http://localhost/soundfile.wav").build()
                }
                // include & to make sure xml gets escaped properly
                .withAction(CallbackUrl("""http://localhost/gather-action?q1=v1&q2=v2"""))
                .withInputDtmfSpeech()
                .withFinishOnKey(Some(DtmfDigit.`*`))
                .addHint("This is a hint phrase")
                .addHint("keyword1")
                .addHint("keyword2")
                .withLanguage(GatherVerb.LanguageCode.`ar-BH`)
                .withMethod(HttpMethod.Post)
                .withNumDigits(47)
                .withPartialResultCallback(CallbackUrl("http://localhost/partial-result"))
                .withProfanityFilter(false)
                .withSpeechTimeout(PositiveInteger.unsafe(24))
                .withTimeout(PositiveInteger.unsafe(34))
                .withSpeechModelDefault()
                .withActionOnEmptyResult(true)
                .buildUnverified()
            }
            .buildUnverified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Gather action="http://localhost/gather-action?q1=v1&amp;q2=v2" finishOnKey="*" hints="This is a hint phrase, keyword1, keyword2" input="dtmf speech" language="ar-BH" method="POST" numDigits="47" partialResultCallback="http://localhost/partial-result" profanityFilter="false" speechTimeout="24" timeout="34" speechModel="default" actionOnEmptyResult="true">
             |    <Say>Say text</Say>
             |    <Pause />
             |    <Play>http://localhost/soundfile.wav</Play>
             |  </Gather>
             |</Response>""".stripMargin

        println(result.xmlPretty)
        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Gather action="http://localhost/gather-action?q1=v1&amp;q2=v2" finishOnKey="*" hints="This is a hint phrase, keyword1, keyword2" input="dtmf speech" language="ar-BH" method="POST" numDigits="47" partialResultCallback="http://localhost/partial-result" profanityFilter="false" speechTimeout="24" timeout="34" speechModel="default" actionOnEmptyResult="true"><Say>Say text</Say><Pause/><Play>http://localhost/soundfile.wav</Play></Gather></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "Generate the correct XML when used with speechModel numbers_and_commands" in {
        val result: Response.UnverifiedFromModel = Response.build { responseBuilder =>
          responseBuilder
            .addCustomVerb {
              GatherVerb.BuilderMutable
                .empty()
                .withInputSpeech()
                .withSpeechModelNumbersAndCommands()
                .buildUnverified()
            }
            .buildUnverified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Gather input="speech" speechModel="numbers_and_commands" />
             |</Response>""".stripMargin

        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Gather input="speech" speechModel="numbers_and_commands"/></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "Generate the correct XML when used with speechModel phone_call" in {
        val result: Response.UnverifiedFromModel = Response.build { responseBuilder =>
          responseBuilder
            .addCustomVerb {
              GatherVerb.BuilderMutable
                .empty()
                .withInputSpeech()
                .withSpeechModelPhoneCall(GatherVerb.LanguageCode.`en-GB`)
                .buildUnverified()
            }
            .buildUnverified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Gather input="speech" language="en-GB" speechModel="phone_call" />
             |</Response>""".stripMargin

        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Gather input="speech" language="en-GB" speechModel="phone_call"/></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "Generate the correct XML when used with speechModel experimental_conversations" in {
        val result: Response.UnverifiedFromModel = Response.build { responseBuilder =>
          responseBuilder
            .addCustomVerb {
              GatherVerb.BuilderMutable
                .empty()
                .withInputSpeech()
                .withSpeechModelExperimentalConversation(GatherVerb.LanguageCode.`en-GB`)
                .buildUnverified()
            }
            .buildUnverified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Gather input="speech" language="en-GB" speechModel="experimental_conversations" />
             |</Response>""".stripMargin

        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Gather input="speech" language="en-GB" speechModel="experimental_conversations"/></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "Generate the correct XML when used with speechModel experimental_utterances" in {
        val result: Response.UnverifiedFromModel = Response.build { responseBuilder =>
          responseBuilder
            .addCustomVerb {
              GatherVerb.BuilderMutable
                .empty()
                .withInputSpeech()
                .withSpeechModelExperimentalUtterances(GatherVerb.LanguageCode.`en-GB`)
                .buildUnverified()
            }
            .buildUnverified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Gather input="speech" language="en-GB" speechModel="experimental_utterances" />
             |</Response>""".stripMargin

        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Gather input="speech" language="en-GB" speechModel="experimental_utterances"/></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "Generate the correct XML when used with speechModel phone_call and the enchanced attribute" in {
        val result: Response.UnverifiedFromModel = Response.build { responseBuilder =>
          responseBuilder
            .addCustomVerb {
              GatherVerb.BuilderMutable
                .empty()
                .withInputSpeech()
                .withSpeechModelPhoneCallPlusEnhanced(GatherVerb.LanguageCode.`en-GB`)
                .buildUnverified()
            }
            .buildUnverified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Gather input="speech" language="en-GB" speechModel="phone_call" enhanced="true" />
             |</Response>""".stripMargin

        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Gather input="speech" language="en-GB" speechModel="phone_call" enhanced="true"/></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

    }
  }
}
