package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.model.voice.Conference
import org.scalatest.wordspec.AnyWordSpec

final class ResponseDialTest extends AnyWordSpec {

  s"${classOf[Response].getSimpleName}" when {

    "constructing a response with Dial" should {

      "Be able to nest a conference within the dial with beep false and empty wait url" in {

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

      "Be able to nest a conference within the dial with beep true and empty wait url" in {

        val conferenceFriendlyName = Conference.FriendlyName("Test_conference")

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder.addDial { dialBuilder =>
            dialBuilder.withConference { conferenceBuilder =>
              conferenceBuilder
                .withBeep(Conference.Beep.True)
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
             |    <Conference beep="true" waitUrl="">$conferenceFriendlyName</Conference>
             |  </Dial>
             |</Response>""".stripMargin

        println(result.xmlPretty)
        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial><Conference beep="true" waitUrl="">$conferenceFriendlyName</Conference></Dial></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "Be able to nest a conference within the dial with beep onEnter and empty wait url" in {

        val conferenceFriendlyName = Conference.FriendlyName("Test_conference")

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder.addDial { dialBuilder =>
            dialBuilder.withConference { conferenceBuilder =>
              conferenceBuilder
                .withBeep(Conference.Beep.OnEnter)
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
             |    <Conference beep="onEnter" waitUrl="">$conferenceFriendlyName</Conference>
             |  </Dial>
             |</Response>""".stripMargin

        println(result.xmlPretty)
        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial><Conference beep="onEnter" waitUrl="">$conferenceFriendlyName</Conference></Dial></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "Be able to nest a conference within the dial with beep onExit and empty wait url" in {

        val conferenceFriendlyName = Conference.FriendlyName("Test_conference")

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder.addDial { dialBuilder =>
            dialBuilder.withConference { conferenceBuilder =>
              conferenceBuilder
                .withBeep(Conference.Beep.OnExit)
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
             |    <Conference beep="onExit" waitUrl="">$conferenceFriendlyName</Conference>
             |  </Dial>
             |</Response>""".stripMargin

        println(result.xmlPretty)
        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial><Conference beep="onExit" waitUrl="">$conferenceFriendlyName</Conference></Dial></Response>"""
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
