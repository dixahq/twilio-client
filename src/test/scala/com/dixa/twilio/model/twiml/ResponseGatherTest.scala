package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.{HttpMethod, PositiveInteger}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.dtmf.DtmfDigit
import com.dixa.twilio.model.twiml.verb.{GatherVerb, HangupVerb, PauseVerb, PlayVerb, SayVerb}
import org.scalatest.wordspec.AnyWordSpec

final class ResponseGatherTest extends AnyWordSpec {

  s"${classOf[Response].getSimpleName}" when {

    "constructing a response with Gather" should {

      "be able to construct a default gather without attributes or nested elements" in {
        // According to documentation, all attributes on gather have default values, so it should be allowed to create an completly empty gather verb.
        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder.addGather(_.build()).buildVerified
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Gather />
             |</Response>""".stripMargin

        println(result.xmlPretty)
        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Gather/></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }
    }

    "be able to construct a Unverified instance by supplying the gather builder with a single custom verb" in {
      val result: Response.Unverified = Response.build { responseBuilder =>
        responseBuilder
          .addGatherUnverified(_.addCustomVerb(PauseVerb.build(_.build())).buildUnverified())
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
          .addGatherUnverified(
            _.addSay(_.withText("aa").build()).addCustomVerbs(verbList).buildUnverified()
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

    "be able to report two instances as equal, if they produce the same TwiML even when one of them is Unverified and the other Verified" in {
      val verbList = List(
        PauseVerb.build(_.build()),
      )
      val unverified: Response.Unverified = Response.build { responseBuilder =>
        responseBuilder
          .addGatherUnverified(
            _.addSay(_.withText("aa").build()).addCustomVerbs(verbList).buildUnverified()
          )
          .buildUnverified()
      }
      val verified: Response.Verified = Response.build { responseBuilder =>
        responseBuilder
          .addGather(
            _.addSay(_.withText("aa").build())
              .addPause(_.build())
              .build()
          )
          .buildVerified()
      }
      assert(unverified == verified)
      assert(unverified.hashCode() == verified.hashCode())
    }

    "be able to include all attribute and to nest pause, play and say verbs" in {
      val result: Response.Verified = Response.build { responseBuilder =>
        responseBuilder.addGather { gatherBuilder =>
          gatherBuilder
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
            .build()
        }.buildVerified
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

    "Don't allow setting finishOnKey if input is set to speech" in {
      assertTypeError("""Response.build { responseBuilder =>
                        |        responseBuilder.addGather { gatherBuilder =>
                        |          gatherBuilder
                        |            .withInputSpeech()
                        |            .withFinishOnKey(Some(DtmfDigit.`*`))
                        |            .build()
                        |        }.buildVerified
                        |      }
                        |""".stripMargin)
    }

    "Don't allow setting input to speech, if finishOnKey has already ben set" in {
      assertTypeError("""Response.build { responseBuilder =>
                        |        responseBuilder.addGather { gatherBuilder =>
                        |          gatherBuilder
                        |            .withFinishOnKey(Some(DtmfDigit.`*`))
                        |            .withInputSpeech()
                        |            .build()
                        |        }.buildVerified
                        |      }
                        |""".stripMargin)
    }

    "Don't allow adding hint, if input has been set to Dtmf" in {
      assertTypeError("""Response.build { responseBuilder =>
                        |        responseBuilder.addGather { gatherBuilder =>
                        |          gatherBuilder
                        |            .withInputDtmf()
                        |            .addHint("ntufywntyfw")
                        |            .build()
                        |        }.buildVerified
                        |      }
                        |""".stripMargin)
    }

    "Don't allow setting input to Dtmf, if hints has been added" in {
      assertTypeError("""Response.build { responseBuilder =>
                        |        responseBuilder.addGather { gatherBuilder =>
                        |          gatherBuilder
                        |            .addHint("nufwynt")
                        |            .withInputDtmf()
                        |            .build()
                        |        }.buildVerified
                        |      }
                        |""".stripMargin)
    }

    "Don't allow setting method if action has not been set" in {
      assertTypeError("""Response.build { responseBuilder =>
                        |        responseBuilder.addGather { gatherBuilder =>
                        |          gatherBuilder
                        |            .withMethod(HttpMethod.Post)
                        |            .build()
                        |        }.buildVerified
                        |      }
                        |""".stripMargin)
    }

    "Don't allow numDigits to be set, if intup don't include DTMF" in {
      assertTypeError("""Response.build { responseBuilder =>
                        |        responseBuilder.addGather { gatherBuilder =>
                        |          gatherBuilder
                        |            .withInputSpeech()
                        |            .withNumDigits(93)
                        |            .build()
                        |        }.buildVerified
                        |      }
                        |""".stripMargin)
    }

    "Don't allow to set input to speech if numDigits have been set" in {
      assertTypeError("""Response.build { responseBuilder =>
                        |        responseBuilder.addGather { gatherBuilder =>
                        |          gatherBuilder
                        |            .withNumDigits(93)
                        |            .withInputSpeech()
                        |            .build()
                        |        }.buildVerified
                        |      }
                        |""".stripMargin)
    }

    "Don't allow to set profanityFilter if speech is not in DTMF" in {
      assertTypeError("""Response.build { responseBuilder =>
                        |        responseBuilder.addGather { gatherBuilder =>
                        |          gatherBuilder
                        |            .withInputDtmf()
                        |            .withProfanityFilter(true)
                        |            .build()
                        |        }.buildVerified
                        |      }
                        |""".stripMargin)
    }

    "Don't allow to set input to DTMF if profanityFilter has set" in {
      assertTypeError("""Response.build { responseBuilder =>
                        |        responseBuilder.addGather { gatherBuilder =>
                        |          gatherBuilder
                        |            .withProfanityFilter(true)
                        |            .withInputDtmf()
                        |            .build()
                        |        }.buildVerified
                        |      }
                        |""".stripMargin)
    }

    "Don't allow to set speechTimeout if input does not include speech" in {
      assertTypeError("""Response.build { responseBuilder =>
                        |        responseBuilder.addGather { gatherBuilder =>
                        |          gatherBuilder
                        |            .withInputDtmf()
                        |            .withSpeechTimeout(PositiveInteger.unsafe(88))
                        |            .build()
                        |        }.buildVerified
                        |      }
                        |""".stripMargin)
    }

    "Don't allow to set input to DTMF only, if speechTimeout has been set" in {
      assertTypeError("""Response.build { responseBuilder =>
                        |        responseBuilder.addGather { gatherBuilder =>
                        |          gatherBuilder
                        |            .withSpeechTimeout(PositiveInteger.unsafe(88))
                        |            .withInputDtmf()
                        |            .build()
                        |        }.buildVerified
                        |      }
                        |""".stripMargin)
    }

    "dealing with speachModel default" should {

      // Test for actually setting this successfully is covered by the test that sets all attriutes.

      "Don't allow to set attribute if speech is not part of the input attribute" in {
        assertTypeError("""Response.build { responseBuilder =>
                          |          responseBuilder.addGather { gatherBuilder =>
                          |            gatherBuilder
                          |              .withInputDtmf()
                          |              .withSpeechModelDefault()
                          |              .build()
                          |          }.buildVerified
                          |        }
                          |""".stripMargin)
      }

      "Don't allow to set input to DTMF only, if speechModelDefault has been called" in {
        assertTypeError("""Response.build { responseBuilder =>
                          |          responseBuilder.addGather { gatherBuilder =>
                          |            gatherBuilder
                          |              .withInputSpeech()
                          |              .withSpeechModelDefault()
                          |              .withInputDtmf()
                          |              .build()
                          |          }.buildVerified
                          |        }
                          |""".stripMargin)
      }

    }

    "Dealing with speechModel numbers_and_commands" should {

      "Generate the correct XML when used" in {
        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder.addGather { gatherBuilder =>
            gatherBuilder
              .withInputSpeech()
              .withSpeechModelNumbersAndCommands()
              .build()
          }.buildVerified
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

      "Don't allow to set attribute if speech is not part of the input attribute" in {
        assertTypeError("""Response.build { responseBuilder =>
                          |          responseBuilder.addGather { gatherBuilder =>
                          |            gatherBuilder
                          |              .withInputDtmf()
                          |              .withSpeechModelNumbersAndCommands()
                          |              .build()
                          |          }.buildVerified
                          |        }
                          |""".stripMargin)
      }

      "Don't allow to set input to DTMF only, if speechModelDefault has been called" in {
        assertTypeError("""Response.build { responseBuilder =>
                          |          responseBuilder.addGather { gatherBuilder =>
                          |            gatherBuilder
                          |              .withInputSpeech()
                          |              .withSpeechModelNumbersAndCommands()
                          |              .withInputDtmf()
                          |              .build()
                          |          }.buildVerified
                          |        }
                          |""".stripMargin)
      }
    }

    "Dealing with speechModel phone_call" should {

      "Generate the correct XML when used" in {
        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder.addGather { gatherBuilder =>
            gatherBuilder
              .withInputSpeech()
              .withSpeechModelPhoneCall(GatherVerb.LanguageCode.`en-GB`)
              .build()
          }.buildVerified
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

      "Don't allow setting this value if language as already been set" in {
        assertTypeError("""Response.build { responseBuilder =>
                          |          responseBuilder.addGather { gatherBuilder =>
                          |            gatherBuilder
                          |              .withInputSpeech()
                          |              .withLanguage(GatherVerb.LanguageCode.`ar-JO`)
                          |              .withSpeechModelPhoneCall(GatherVerb.LanguageCode.`en-GB`)
                          |              .build()
                          |          }.buildVerified
                          |        }
                          |""".stripMargin)
      }

      "Don't allow calling withLanguage() after setting this value" in {
        assertTypeError("""Response.build { responseBuilder =>
                          |          responseBuilder.addGather { gatherBuilder =>
                          |            gatherBuilder
                          |              .withInputSpeech()
                          |              .withSpeechModelPhoneCall(GatherVerb.LanguageCode.`en-GB`)
                          |              .withLanguage(GatherVerb.LanguageCode.`ar-JO`)
                          |              .build()
                          |          }.buildVerified
                          |        }
                          |""".stripMargin)
      }

      "Don't allow calling it with a language code that does not support this phone_call model" in {
        assertTypeError("""Response.build { responseBuilder =>
                          |          responseBuilder.addGather { gatherBuilder =>
                          |            gatherBuilder
                          |              .withInputSpeech()
                          |              .withSpeechModelPhoneCall(GatherVerb.LanguageCode.`ar-BH`)
                          |              .build()
                          |          }.buildVerified
                          |        }
                          |""".stripMargin)
      }

      "Don't allow to set attribute if speech is not part of the input attribute" in {
        assertTypeError("""Response.build { responseBuilder =>
                          |          responseBuilder.addGather { gatherBuilder =>
                          |            gatherBuilder
                          |              .withInputDtmf()
                          |              .withSpeechModelPhoneCall(GatherVerb.LanguageCode.`en-GB`)
                          |              .build()
                          |          }.buildVerified
                          |        }
                          |""".stripMargin)
      }

      "Don't allow to set input to DTMF only, if this value has been called" in {
        assertTypeError("""Response.build { responseBuilder =>
                          |          responseBuilder.addGather { gatherBuilder =>
                          |            gatherBuilder
                          |              .withInputSpeech()
                          |              .withSpeechModelPhoneCall(GatherVerb.LanguageCode.`en-GB`)
                          |              .withInputDtmf()
                          |              .build()
                          |          }.buildVerified
                          |        }
                          |""".stripMargin)
      }
    }

    "Dealing with speechModel experimental_conversations" should {

      "Generate the correct XML when used" in {
        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder.addGather { gatherBuilder =>
            gatherBuilder
              .withInputSpeech()
              .withSpeechModelExperimentalConversation(GatherVerb.LanguageCode.`en-GB`)
              .build()
          }.buildVerified
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

      "Don't allow setting this value if language as already been set" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder.addGather { gatherBuilder =>
            |            gatherBuilder
            |              .withInputSpeech()
            |              .withLanguage(GatherVerb.LanguageCode.`ar-JO`)
            |              .withSpeechModelExperimentalConversation(GatherVerb.LanguageCode.`en-GB`)
            |              .build()
            |          }.buildVerified
            |        }
            |""".stripMargin
        )
      }

      "Don't allow calling withLanguage() after setting this value" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder.addGather { gatherBuilder =>
            |            gatherBuilder
            |              .withInputSpeech()
            |              .withSpeechModelExperimentalConversation(GatherVerb.LanguageCode.`en-GB`)
            |              .withLanguage(GatherVerb.LanguageCode.`ar-JO`)
            |              .build()
            |          }.buildVerified
            |        }
            |""".stripMargin
        )
      }

      "Don't allow calling it with a language code that does not support this phone_call model" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder.addGather { gatherBuilder =>
            |            gatherBuilder
            |              .withInputSpeech()
            |              .withSpeechModelExperimentalConversation(GatherVerb.LanguageCode.`sr-RS`)
            |              .build()
            |          }.buildVerified
            |        }
            |""".stripMargin
        )
      }

      "Don't allow to set attribute if speech is not part of the input attribute" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder.addGather { gatherBuilder =>
            |            gatherBuilder
            |              .withInputDtmf()
            |              .withSpeechModelExperimentalConversation(GatherVerb.LanguageCode.`en-GB`)
            |              .build()
            |          }.buildVerified
            |        }
            |""".stripMargin
        )
      }

      "Don't allow to set input to DTMF only, if this value has been has been called" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder.addGather { gatherBuilder =>
            |            gatherBuilder
            |              .withInputSpeech()
            |              .withSpeechModelExperimentalConversation(GatherVerb.LanguageCode.`en-GB`)
            |              .withInputDtmf()
            |              .build()
            |          }.buildVerified
            |        }
            |""".stripMargin
        )
      }
    }

    "Dealing with speechModel experimental_utterances" should {

      "Generate the correct XML when used" in {
        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder.addGather { gatherBuilder =>
            gatherBuilder
              .withInputSpeech()
              .withSpeechModelExperimentalUtterances(GatherVerb.LanguageCode.`en-GB`)
              .build()
          }.buildVerified
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

      "Don't allow setting this value if language as already been set" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder.addGather { gatherBuilder =>
            |            gatherBuilder
            |              .withInputSpeech()
            |              .withLanguage(GatherVerb.LanguageCode.`ar-JO`)
            |              .withSpeechModelExperimentalUtterances(GatherVerb.LanguageCode.`en-GB`)
            |              .build()
            |          }.buildVerified
            |        }
            |""".stripMargin
        )
      }

      "Don't allow calling withLanguage() after setting this value" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder.addGather { gatherBuilder =>
            |            gatherBuilder
            |              .withInputSpeech()
            |              .withSpeechModelExperimentalUtterances(GatherVerb.LanguageCode.`en-GB`)
            |              .withLanguage(GatherVerb.LanguageCode.`ar-JO`)
            |              .build()
            |          }.buildVerified
            |        }
            |""".stripMargin
        )
      }

      "Don't allow calling it with a language code that does not support this phone_call model" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder.addGather { gatherBuilder =>
            |            gatherBuilder
            |              .withInputSpeech()
            |              .withSpeechModelExperimentalUtterances(GatherVerb.LanguageCode.`sr-RS`)
            |              .build()
            |          }.buildVerified
            |        }
            |""".stripMargin
        )
      }

      "Don't allow to set attribute if speech is not part of the input attribute" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder.addGather { gatherBuilder =>
            |            gatherBuilder
            |              .withInputDtmf()
            |              .withSpeechModelExperimentalUtterances(GatherVerb.LanguageCode.`en-GB`)
            |              .build()
            |          }.buildVerified
            |        }
            |""".stripMargin
        )
      }

      "Don't allow to set input to DTMF only, if this value has been has been called" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder.addGather { gatherBuilder =>
            |            gatherBuilder
            |              .withInputSpeech()
            |              .withSpeechModelExperimentalUtterances(GatherVerb.LanguageCode.`en-GB`)
            |              .withInputDtmf()
            |              .build()
            |          }.buildVerified
            |        }
            |""".stripMargin
        )
      }
    }

    "Dealing with speechModel phone_call and the enchanced attribute" should {

      "Generate the correct XML when used" in {
        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder.addGather { gatherBuilder =>
            gatherBuilder
              .withInputSpeech()
              .withSpeechModelPhoneCallPlusEnhanced(GatherVerb.LanguageCode.`en-GB`)
              .build()
          }.buildVerified
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

      "Don't allow setting this value if language as already been set" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder.addGather { gatherBuilder =>
            |            gatherBuilder
            |              .withInputSpeech()
            |              .withLanguage(GatherVerb.LanguageCode.`ar-JO`)
            |              .withSpeechModelPhoneCallPlusEnhanced(GatherVerb.LanguageCode.`en-GB`)
            |              .build()
            |          }.buildVerified
            |        }
            |""".stripMargin
        )
      }

      "Don't allow calling withLanguage() after setting this value" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder.addGather { gatherBuilder =>
            |            gatherBuilder
            |              .withInputSpeech()
            |              .withSpeechModelPhoneCallPlusEnhanced(GatherVerb.LanguageCode.`en-GB`)
            |              .withLanguage(GatherVerb.LanguageCode.`ar-JO`)
            |              .build()
            |          }.buildVerified
            |        }
            |""".stripMargin
        )
      }

      "Don't allow calling it with a language code that does not support this phone_call + enchanced model" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder.addGather { gatherBuilder =>
            |            gatherBuilder
            |              .withInputSpeech()
            |              .withSpeechModelPhoneCallPlusEnhanced(GatherVerb.LanguageCode.`sr-RS`)
            |              .build()
            |          }.buildVerified
            |        }
            |""".stripMargin
        )
      }

      "Don't allow to set attribute if speech is not part of the input attribute" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder.addGather { gatherBuilder =>
            |            gatherBuilder
            |              .withInputDtmf()
            |              .withSpeechModelPhoneCallPlusEnhanced(GatherVerb.LanguageCode.`en-GB`)
            |              .build()
            |          }.buildVerified
            |        }
            |""".stripMargin
        )
      }

      "Don't allow to set input to DTMF only, if this value has been has been called" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder.addGather { gatherBuilder =>
            |            gatherBuilder
            |              .withInputSpeech()
            |              .withSpeechModelPhoneCallPlusEnhanced(GatherVerb.LanguageCode.`en-GB`)
            |              .withInputDtmf()
            |              .build()
            |          }.buildVerified
            |        }
            |""".stripMargin
        )
      }
    }
  }
}
