package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.{HttpMethod, PositiveInteger}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.dtmf.DtmfDigit
import com.dixa.twilio.model.twiml.verb.{GatherVerb, PauseVerb, PlayVerb, SayVerb}
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
            .withAction(CallbackUrl("http://localhost/gather-action"))
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
            .build()
        }.buildVerified
      }

      val expectedPrettyXml =
        s"""<?xml version="1.0" encoding="UTF-8"?>
           |<Response>
           |  <Gather action="http://localhost/gather-action" finishOnKey="*" hints="This is a hint phrase, keyword1, keyword2" input="dtmf speech" language="ar-BH" method="POST" numDigits="47" partialResultCallback="http://localhost/partial-result" profanityFilter="false" speechTimeout="24">
           |    <Say>Say text</Say>
           |    <Pause />
           |    <Play>http://localhost/soundfile.wav</Play>
           |  </Gather>
           |</Response>""".stripMargin

      println(result.xmlPretty)
      assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Gather action="http://localhost/gather-action" finishOnKey="*" hints="This is a hint phrase, keyword1, keyword2" input="dtmf speech" language="ar-BH" method="POST" numDigits="47" partialResultCallback="http://localhost/partial-result" profanityFilter="false" speechTimeout="24"><Say>Say text</Say><Pause/><Play>http://localhost/soundfile.wav</Play></Gather></Response>"""
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
  }
}
