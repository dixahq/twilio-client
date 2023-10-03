package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.model.voice.{Call, Conference}
import com.dixa.twilio.model.{HttpMethod, Region}
import org.scalatest.wordspec.AnyWordSpec

final class ResponseDialTest extends AnyWordSpec {

  s"${classOf[Response].getSimpleName}" when {

    "constructing a response with Dial" should {

      "Be able to nest a conference within the dial with beep false, muted true, startConferenceOnEnter false, endConferenceOnExit true, custom participantLabel, jitterBufferSize medium, empty wait url, waithMethod Get, maxParticipants 233, record record-from-start, region jp1, trim do-not-trim, coach CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1, statusCallbackEvent start + end, statusCallback = localhost, statusCallbackMethod POST, recordingStatusCallback localhost, recordingStatusCallbackMethod GET and recordingStatusCallbackEvent Completed + absent" in {

        val conferenceFriendlyName = Conference.FriendlyName("Test_conference")

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addDial { dialBuilder =>
              dialBuilder
                .withConference { conferenceBuilder =>
                  conferenceBuilder
                    .withConferenceFriendlyName(conferenceFriendlyName)
                    .withMuted(true)
                    .withBeep(Conference.Beep.False)
                    .withStartConferenceOnEnter(false)
                    .withEndConferenceOnExit(true)
                    .withParticipantLabel(Conference.Participant.Label("customLabel"))
                    .withJitterBufferSize(Conference.Participant.JitterBufferSize.Medium)
                    .withWaitUrlEmpty()
                    .withMaxParticipants(Conference.MaxParticipants.unsafe(233))
                    .withRecordFromStart()
                    .withRegion(Region.Jp1)
                    .withTrim(Conference.Trim.DoNotTrim)
                    .withCoach(Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1"))
                    .withStatusCallback(CallbackUrl("http://localhost/status"))
                    .withStatusCallbackEvent(
                      Set(Conference.StatusCallbackEvent.Start, Conference.StatusCallbackEvent.End)
                    )
                    .withStatusCallbackMethod(HttpMethod.Post)
                    .withRecordingStatusCallback(CallbackUrl("http://localhost/recording"))
                    .withRecordingStatusCallbackMethod(HttpMethod.Get)
                    .withRecordingStatusCallbackEvent(
                      Set(
                        Conference.RecordingStatusCallbackEvent.Absent,
                        Conference.RecordingStatusCallbackEvent.Completed
                      )
                    )
                    .build()
                }
                .build()
            }
            .buildVerified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Dial>
             |    <Conference muted="true" beep="false" startConferenceOnEnter="false" endConferenceOnExit="true" participantLabel="customLabel" jitterBufferSize="medium" waitUrl="" maxParticipants="233" record="record-from-start" region="jp1" trim="do-not-trim" coach="CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1" statusCallbackEvent="start end" statusCallback="http://localhost/status" statusCallbackMethod="POST" recordingStatusCallback="http://localhost/recording" recordingStatusCallbackMethod="GET" recordingStatusCallbackEvent="completed absent">$conferenceFriendlyName</Conference>
             |  </Dial>
             |</Response>""".stripMargin

        assert(result.xmlPretty === expectedPrettyXml)
        
        // format: off
        val expectedCompactXml = 
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial><Conference muted="true" beep="false" startConferenceOnEnter="false" endConferenceOnExit="true" participantLabel="customLabel" jitterBufferSize="medium" waitUrl="" maxParticipants="233" record="record-from-start" region="jp1" trim="do-not-trim" coach="CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1" statusCallbackEvent="start end" statusCallback="http://localhost/status" statusCallbackMethod="POST" recordingStatusCallback="http://localhost/recording" recordingStatusCallbackMethod="GET" recordingStatusCallbackEvent="completed absent">$conferenceFriendlyName</Conference></Dial></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "Be able to nest a conference within the dial with None empty wait url" in {

        val conferenceFriendlyName = Conference.FriendlyName("Test_conference")

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addDial { dialBuilder =>
              dialBuilder
                .withConference { conferenceBuilder =>
                  conferenceBuilder
                    .withConferenceFriendlyName(conferenceFriendlyName)
                    .withWaitUrl(CallbackUrl("http://localhost/test/wait/url"))
                    .withWaitMethod(HttpMethod.Get)
                    .build()
                }
                .build()
            }
            .buildVerified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Dial>
             |    <Conference waitUrl="http://localhost/test/wait/url" waitMethod="GET">$conferenceFriendlyName</Conference>
             |  </Dial>
             |</Response>""".stripMargin

        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial><Conference waitUrl="http://localhost/test/wait/url" waitMethod="GET">$conferenceFriendlyName</Conference></Dial></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "Be able to nest a conference within the dial with beep true and empty wait url" in {

        val conferenceFriendlyName = Conference.FriendlyName("Test_conference")

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addDial { dialBuilder =>
              dialBuilder
                .withConference { conferenceBuilder =>
                  conferenceBuilder
                    .withBeep(Conference.Beep.True)
                    .withWaitUrlEmpty()
                    .withConferenceFriendlyName(conferenceFriendlyName)
                    .build()
                }
                .build()
            }
            .buildVerified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Dial>
             |    <Conference beep="true" waitUrl="">$conferenceFriendlyName</Conference>
             |  </Dial>
             |</Response>""".stripMargin

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
          responseBuilder
            .addDial { dialBuilder =>
              dialBuilder
                .withConference { conferenceBuilder =>
                  conferenceBuilder
                    .withBeep(Conference.Beep.OnEnter)
                    .withWaitUrlEmpty()
                    .withConferenceFriendlyName(conferenceFriendlyName)
                    .build()
                }
                .build()
            }
            .buildVerified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Dial>
             |    <Conference beep="onEnter" waitUrl="">$conferenceFriendlyName</Conference>
             |  </Dial>
             |</Response>""".stripMargin

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
          responseBuilder
            .addDial { dialBuilder =>
              dialBuilder
                .withConference { conferenceBuilder =>
                  conferenceBuilder
                    .withBeep(Conference.Beep.OnExit)
                    .withWaitUrlEmpty()
                    .withConferenceFriendlyName(conferenceFriendlyName)
                    .build()
                }
                .build()
            }
            .buildVerified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Dial>
             |    <Conference beep="onExit" waitUrl="">$conferenceFriendlyName</Conference>
             |  </Dial>
             |</Response>""".stripMargin

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
          responseBuilder
            .addDial { dialBuilder =>
              dialBuilder.withPhoneNumber(PhoneNumberE164.unsafe(pn)).build()
            }
            .buildVerified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Dial>$pn</Dial>
             |</Response>""".stripMargin

        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial>$pn</Dial></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }
    }

    "Be able to nest a conference within the dial with conference name including reserved XML chars" in {

      val conferenceFriendlyName = Conference.FriendlyName("Test_\"'<>&_conference")
      val conferenceEscapedName  = "Test_&quot;&apos;&lt;&gt;&amp;_conference"

      val result: Response.Verified = Response.build { responseBuilder =>
        responseBuilder
          .addDial { dialBuilder =>
            dialBuilder
              .withConference { conferenceBuilder =>
                conferenceBuilder
                  .withConferenceFriendlyName(conferenceFriendlyName)
                  .build()
              }
              .build()
          }
          .buildVerified()
      }

      val expectedPrettyXml =
        s"""<?xml version="1.0" encoding="UTF-8"?>
           |<Response>
           |  <Dial>
           |    <Conference>$conferenceEscapedName</Conference>
           |  </Dial>
           |</Response>""".stripMargin

      assert(result.xmlPretty === expectedPrettyXml)

      // format: off
      val expectedCompactXml =
        s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial><Conference>$conferenceEscapedName</Conference></Dial></Response>"""
      // format: on
      assert(result.xmlCompact == expectedCompactXml)
    }

    "Don't allow calling build, without setting conferenceName" in {
      assertTypeError("""Response.build { responseBuilder =>
                        |        responseBuilder
                        |          .addDial { dialBuilder =>
                        |            dialBuilder
                        |              .withConference { conferenceBuilder =>
                        |                conferenceBuilder
                        |                  .withWaitUrlEmpty()
                        |                  .withWaitMethod(HttpMethod.Get)
                        |                  .build()
                        |              }
                        |              .build()
                        |          }
                        |          .buildVerified()
                        |      }""".stripMargin)
    }

    "Don't allow calling withWaitMethod if withWaitUrl has not been called" in {
      assertTypeError(
        """Response.build { responseBuilder =>
          |          responseBuilder
          |            .addDial { dialBuilder =>
          |              dialBuilder
          |                .withConference { conferenceBuilder =>
          |                  conferenceBuilder
          |                    .withConferenceFriendlyName(Conference.FriendlyName("Test_conference"))
          |                    .withWaitMethod(HttpMethod.Get)
          |                    .build()
          |                }
          |                .build()
          |            }
          |            .buildVerified()
          |        }""".stripMargin
      )
    }

    "Don't allow calling withWaitMethod if withWaitUrl has not been called (even if withWaitUrlEmpty has)" in {
      assertTypeError(
        """Response.build { responseBuilder =>
          |          responseBuilder
          |            .addDial { dialBuilder =>
          |              dialBuilder
          |                .withConference { conferenceBuilder =>
          |                  conferenceBuilder
          |                    .withConferenceFriendlyName(Conference.FriendlyName("Test_conference"))
          |                    .withWaitUrlEmpty()
          |                    .withWaitMethod(HttpMethod.Get)
          |                    .build()
          |                }
          |                .build()
          |            }
          |            .buildVerified()
          |        }""".stripMargin
      )
    }

    "Don't allow calling withWaitUrl if withWaitUrlEmpty has ben called" in {
      assertTypeError(
        """Response.build { responseBuilder =>
          |          responseBuilder
          |            .addDial { dialBuilder =>
          |              dialBuilder
          |                .withConference { conferenceBuilder =>
          |                  conferenceBuilder
          |                    .withConferenceFriendlyName(Conference.FriendlyName("Test_conference"))
          |                    .withWaitUrlEmpty()
          |                    .withWaitUrl(CallbackUrl("http://localhost/test"))
          |                    .withWaitMethod(HttpMethod.Get)
          |                    .build()
          |                }
          |                .build()
          |            }
          |            .buildVerified()
          |        }""".stripMargin
      )
    }

    "Don't allow calling withWaitUrlEmpty if withWaitUrl has ben called" in {
      assertTypeError(
        """Response.build { responseBuilder =>
          |          responseBuilder
          |            .addDial { dialBuilder =>
          |              dialBuilder
          |                .withConference { conferenceBuilder =>
          |                  conferenceBuilder
          |                    .withConferenceFriendlyName(Conference.FriendlyName("Test_conference"))
          |                    .withWaitUrl(CallbackUrl("http://localhost/test"))
          |                    .withWaitUrlEmpty()
          |                    .build()
          |                }
          |                .build()
          |            }
          |            .buildVerified()
          |        }""".stripMargin
      )
    }

    "Don't allow calling withTrim, if withRecordingFromStart has not been called first" in {
      assertTypeError(
        """Response.build { responseBuilder =>
          |        responseBuilder
          |          .addDial { dialBuilder =>
          |            dialBuilder
          |              .withConference { conferenceBuilder =>
          |                conferenceBuilder
          |                  .withConferenceFriendlyName(Conference.FriendlyName("Test_conference"))
          |                  .withTrim(Conference.Trim.DoNotTrim)
          |                  .withWaitUrlEmpty()
          |                  .build()
          |              }
          |              .build()
          |          }
          |          .buildVerified()
          |      }""".stripMargin
      )
    }

    "Don't allow withStatusCallbackEvent to be called, if not withStatusCalback has been called first" in {
      assertTypeError(
        """Response.build { responseBuilder =>
          |        responseBuilder
          |          .addDial { dialBuilder =>
          |            dialBuilder
          |              .withConference { conferenceBuilder =>
          |                conferenceBuilder
          |                  .withConferenceFriendlyName(Conference.FriendlyName("Test_conference"))
          |                  .withStatusCallbackEvent(Set(Conference.StatusCallbackEvent.Join))
          |                  .build()
          |              }
          |              .build()
          |          }
          |          .buildVerified()
          |      }""".stripMargin
      )
    }

    "Don't allow withStatusCallbackMethod to be called, if withStatusCallback has not been called first" in {
      assertTypeError(
        """Response.build { responseBuilder =>
          |        responseBuilder
          |          .addDial { dialBuilder =>
          |            dialBuilder
          |              .withConference { conferenceBuilder =>
          |                conferenceBuilder
          |                  .withConferenceFriendlyName(Conference.FriendlyName("Test_conference"))
          |                  .withStatusCallbackMethod(HttpMethod.Get)
          |                  .build()
          |              }
          |              .build()
          |          }
          |          .buildVerified()
          |      }""".stripMargin
      )
    }

    "Don't allow withRecordingStatusCallback to be called, if withRecordFromStart has not been called first" in {
      assertTypeError(
        """Response.build { responseBuilder =>
          |        responseBuilder
          |          .addDial { dialBuilder =>
          |            dialBuilder
          |              .withConference { conferenceBuilder =>
          |                conferenceBuilder
          |                  .withConferenceFriendlyName(Conference.FriendlyName("Test_conference"))
          |                  .withRecordingStatusCallback()
          |                  .build()
          |              }
          |              .build()
          |          }
          |          .buildVerified()
          |      }""".stripMargin
      )
    }

    "Don't allow withRecordingStatusMethod to be called, if withRecordingStatusCallback has not been called first" in {
      assertTypeError(
        """Response.build { responseBuilder =>
          |        responseBuilder
          |          .addDial { dialBuilder =>
          |            dialBuilder
          |              .withConference { conferenceBuilder =>
          |                conferenceBuilder
          |                  .withConferenceFriendlyName(Conference.FriendlyName("Test_conference"))
          |                  .withRecordingStatusCallbackMethod(HttpMethod.Get)
          |                  .build()
          |              }
          |              .build()
          |          }
          |          .buildVerified()
          |      }""".stripMargin
      )
    }

    "Don't allow withRecordingStatusEvent to be called, if withRecordingStatusCallback has not been called first" in {
      assertTypeError(
        """Response.build { responseBuilder =>
          |        responseBuilder
          |          .addDial { dialBuilder =>
          |            dialBuilder
          |              .withConference { conferenceBuilder =>
          |                conferenceBuilder
          |                  .withConferenceFriendlyName(Conference.FriendlyName("Test_conference"))
          |                  .withRecordingStatusCallbackEvent(Set(Conference.RecordingStatusCallbackEvent.Completed))
          |                  .build()
          |              }
          |              .build()
          |          }
          |          .buildVerified()
          |      }""".stripMargin
      )
    }

  }
}
