package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.dtmf.DtmfDigit
import com.dixa.twilio.model.twiml.verb.{PauseVerb, PlayVerb, SayVerb}
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
            .withFinishOnKey(Some(DtmfDigit.`*`))
            .build()
        }.buildVerified
      }

      val expectedPrettyXml =
        s"""<?xml version="1.0" encoding="UTF-8"?>
           |<Response>
           |  <Gather action="http://localhost/gather-action" finishOnKey="*">
           |    <Say>Say text</Say>
           |    <Pause />
           |    <Play>http://localhost/soundfile.wav</Play>
           |  </Gather>
           |</Response>""".stripMargin

      println(result.xmlPretty)
      assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Gather action="http://localhost/gather-action" finishOnKey="*"><Say>Say text</Say><Pause/><Play>http://localhost/soundfile.wav</Play></Gather></Response>"""
        // format: on
      assert(result.xmlCompact == expectedCompactXml)
    }
  }
}
