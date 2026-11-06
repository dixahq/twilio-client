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

import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.model.twiml.noun.{ClientNoun, NumberNoun, SipNoun}
import com.dixa.twilio.model.twiml.verb.DialVerb
import com.dixa.twilio.model.voice.{Call, Conference}
import com.dixa.twilio.model.{HttpMethod, Region}
import org.scalatest.wordspec.AnyWordSpec

final class ResponseDialTest extends AnyWordSpec {

  private val callerIdPhoneNumber =
    DialVerb.CallerId.PhoneNumber(PhoneNumberE164.unsafe("+18005551212"))

  private def numberNoun(phoneNumber: String): NumberNoun =
    NumberNoun.build(_.withPhoneNumber(PhoneNumberE164.unsafe(phoneNumber)).build())

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
                    .withRegion(Region.Australia1)
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
             |    <Conference muted="true" beep="false" startConferenceOnEnter="false" endConferenceOnExit="true" participantLabel="customLabel" jitterBufferSize="medium" waitUrl="" maxParticipants="233" record="record-from-start" region="au1" trim="do-not-trim" coach="CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1" statusCallbackEvent="start end" statusCallback="http://localhost/status" statusCallbackMethod="POST" recordingStatusCallback="http://localhost/recording" recordingStatusCallbackMethod="GET" recordingStatusCallbackEvent="completed absent">$conferenceFriendlyName</Conference>
             |  </Dial>
             |</Response>""".stripMargin

        assert(result.xmlPretty === expectedPrettyXml)
        
        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial><Conference muted="true" beep="false" startConferenceOnEnter="false" endConferenceOnExit="true" participantLabel="customLabel" jitterBufferSize="medium" waitUrl="" maxParticipants="233" record="record-from-start" region="au1" trim="do-not-trim" coach="CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1" statusCallbackEvent="start end" statusCallback="http://localhost/status" statusCallbackMethod="POST" recordingStatusCallback="http://localhost/recording" recordingStatusCallbackMethod="GET" recordingStatusCallbackEvent="completed absent">$conferenceFriendlyName</Conference></Dial></Response>"""
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

        val pn                        = "+4533442255"
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

      "Be able to dial a Number noun without any attribute" in {

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addDial { dialBuilder =>
              dialBuilder
                .withDestinations(
                  DialVerb.Destinations.unsafe(
                    List(
                      NumberNoun.build { numberBuilder =>
                        numberBuilder
                          .withPhoneNumber(PhoneNumberE164.unsafe("+15559999999"))
                          .build()
                      }
                    )
                  )
                )
                .build()
            }
            .buildVerified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Dial>
             |    <Number>+15559999999</Number>
             |  </Dial>
             |</Response>""".stripMargin

        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial><Number>+15559999999</Number></Dial></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "Be able to dial several numbers one after the other, with callerId and timeout" in {

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addDial { dialBuilder =>
              dialBuilder
                .withCallerId(callerIdPhoneNumber)
                .withTimeout(DialVerb.Timeout.unsafe(30))
                .withSequential(true)
                .withDestinations(
                  DialVerb.Destinations.unsafe(
                    List(
                      numberNoun("+15551111111"),
                      numberNoun("+15552222222"),
                      numberNoun("+15553333333")
                    )
                  )
                )
                .build()
            }
            .buildVerified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Dial callerId="+18005551212" timeout="30" sequential="true">
             |    <Number>+15551111111</Number>
             |    <Number>+15552222222</Number>
             |    <Number>+15553333333</Number>
             |  </Dial>
             |</Response>""".stripMargin

        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial callerId="+18005551212" timeout="30" sequential="true"><Number>+15551111111</Number><Number>+15552222222</Number><Number>+15553333333</Number></Dial></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "Be able to set the action that Twilio requests when the Dial ends" in {

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addDial { dialBuilder =>
              dialBuilder
                .withCallerId(callerIdPhoneNumber)
                .withAction(
                  CallbackUrl("/v1/twilio/forward/12345/status?parent=voicemail-node-123")
                )
                .withTimeout(DialVerb.Timeout.unsafe(30))
                .withSequential(true)
                .withDestinations(
                  DialVerb.Destinations.unsafe(
                    List(numberNoun("+15551111111"), numberNoun("+15552222222"))
                  )
                )
                .build()
            }
            .buildVerified()
        }

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial callerId="+18005551212" action="/v1/twilio/forward/12345/status?parent=voicemail-node-123" timeout="30" sequential="true"><Number>+15551111111</Number><Number>+15552222222</Number></Dial></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "Be able to dial a Number, a Sip and a Client noun at the same time" in {

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addDial { dialBuilder =>
              dialBuilder
                .withCallerId(callerIdPhoneNumber)
                .withTimeout(DialVerb.Timeout.unsafe(30))
                .withDestinations(
                  DialVerb.Destinations.unsafe(
                    List(
                      numberNoun("+15551111111"),
                      SipNoun.build { sipBuilder =>
                        sipBuilder.withUri(SipNoun.Uri.unsafe("sip:agent@pbx.company.com")).build()
                      },
                      ClientNoun.build { clientBuilder =>
                        clientBuilder
                          .withIdentity(ClientNoun.Identity.unsafe("agent-browser-123"))
                          .build()
                      }
                    )
                  )
                )
                .build()
            }
            .buildVerified()
        }

        val expectedPrettyXml =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Dial callerId="+18005551212" timeout="30">
             |    <Number>+15551111111</Number>
             |    <Sip>sip:agent@pbx.company.com</Sip>
             |    <Client>agent-browser-123</Client>
             |  </Dial>
             |</Response>""".stripMargin

        assert(result.xmlPretty === expectedPrettyXml)

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial callerId="+18005551212" timeout="30"><Number>+15551111111</Number><Sip>sip:agent@pbx.company.com</Sip><Client>agent-browser-123</Client></Dial></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "Be able to dial a Sip noun with credentials, escaping reserved XML chars in them" in {

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addDial { dialBuilder =>
              dialBuilder
                .withDestinations(
                  DialVerb.Destinations.unsafe(
                    List(
                      SipNoun.build { sipBuilder =>
                        sipBuilder
                          .withUri(SipNoun.Uri.unsafe("sip:+15559999999@kamailio.example.com"))
                          .withCredentials(
                            SipNoun.Username("trunk-user"),
                            SipNoun.Password("pa\"'<>&ss")
                          )
                          .build()
                      }
                    )
                  )
                )
                .build()
            }
            .buildVerified()
        }

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial><Sip username="trunk-user" password="pa&quot;&apos;&lt;&gt;&amp;ss">sip:+15559999999@kamailio.example.com</Sip></Dial></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "Be able to record the dialed call and get a recording status callback" in {

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addDial { dialBuilder =>
              dialBuilder
                .withCallerId(callerIdPhoneNumber)
                .withRecord(DialVerb.Record.RecordFromAnswer)
                .withRecordingStatusCallback(
                  CallbackUrl("/v1/twilio/incoming/12345/recording?recordingType=callrecording")
                )
                .withRecordingStatusCallbackMethod(HttpMethod.Post)
                .withTimeout(DialVerb.Timeout.unsafe(30))
                .withDestinations(DialVerb.Destinations.unsafe(List(numberNoun("+15559999999"))))
                .build()
            }
            .buildVerified()
        }

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial callerId="+18005551212" record="record-from-answer" recordingStatusCallbackMethod="POST" recordingStatusCallback="/v1/twilio/incoming/12345/recording?recordingType=callrecording" timeout="30"><Number>+15559999999</Number></Dial></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "Be able to use a client identifier as callerId" in {

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addDial { dialBuilder =>
              dialBuilder
                .withCallerId(DialVerb.CallerId.Client(ClientNoun.Identity.unsafe("joey")))
                .withDestinations(
                  DialVerb.Destinations.unsafe(
                    List(
                      ClientNoun.build(_.withIdentity(ClientNoun.Identity.unsafe("jenny")).build())
                    )
                  )
                )
                .build()
            }
            .buildVerified()
        }

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial callerId="client:joey"><Client>jenny</Client></Dial></Response>"""
        // format: on
        assert(result.xmlCompact == expectedCompactXml)
      }

      "Be able to use an alphanumeric callerId for a Sip noun" in {

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addDial { dialBuilder =>
              dialBuilder
                .withCallerId(DialVerb.CallerId.Sip.unsafe("Dixa_Support-1.0+"))
                .withDestinations(
                  DialVerb.Destinations.unsafe(
                    List(
                      SipNoun.build(_.withUri(SipNoun.Uri.unsafe("sip:jack@example.com")).build())
                    )
                  )
                )
                .build()
            }
            .buildVerified()
        }

        // format: off
        val expectedCompactXml =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Dial callerId="Dixa_Support-1.0+"><Sip>sip:jack@example.com</Sip></Dial></Response>"""
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

    "Don't allow withRecordingStatusCallback on a Dial, if withRecord has not been called first" in {
      assertTypeError(
        """DialVerb.build(
          |  _.withRecordingStatusCallback(CallbackUrl("/recording"))
          |    .withDestinations(DialVerb.Destinations.unsafe(List(numberNoun("+15559999999"))))
          |    .build()
          |)""".stripMargin
      )
    }

    "Don't allow withRecordingStatusCallbackMethod on a Dial, if withRecordingStatusCallback has not been called first" in {
      assertTypeError(
        """DialVerb.build(
          |  _.withRecord(DialVerb.Record.RecordFromAnswer)
          |    .withRecordingStatusCallbackMethod(HttpMethod.Post)
          |    .withDestinations(DialVerb.Destinations.unsafe(List(numberNoun("+15559999999"))))
          |    .build()
          |)""".stripMargin
      )
    }

    "Don't allow a Conference noun among the destinations" in {
      assertTypeError(
        """DialVerb.Destinations.unsafe(
          |  List(
          |    com.dixa.twilio.model.twiml.noun.ConferenceNoun
          |      .build(_.withConferenceFriendlyName(Conference.FriendlyName("c")).build())
          |  )
          |)""".stripMargin
      )
    }

    "Don't allow withDestinations after withConference" in {
      assertTypeError(
        """DialVerb.build(
          |  _.withConference(_.withConferenceFriendlyName(Conference.FriendlyName("c")).build())
          |    .withDestinations(DialVerb.Destinations.unsafe(List(numberNoun("+15559999999"))))
          |    .build()
          |)""".stripMargin
      )
    }

    "Don't allow withConference after withDestinations" in {
      assertTypeError(
        """DialVerb.build(
          |  _.withDestinations(DialVerb.Destinations.unsafe(List(numberNoun("+15559999999"))))
          |    .withConference(_.withConferenceFriendlyName(Conference.FriendlyName("c")).build())
          |    .build()
          |)""".stripMargin
      )
    }

    "Don't allow building a Number noun without a phone number" in {
      assertTypeError("""NumberNoun.build(_.build())""")
    }

    "Don't allow building a Sip noun without a URI" in {
      assertTypeError(
        """SipNoun.build(_.withCredentials(SipNoun.Username("u"), SipNoun.Password("p")).build())"""
      )
    }

    "Don't allow building a Client noun without an identity" in {
      assertTypeError("""ClientNoun.build(_.build())""")
    }

  }
}
