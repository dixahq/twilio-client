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

import com.dixa.twilio.model.dtmf.{DtmfDigit, DtmfString}
import org.scalatest.wordspec.AnyWordSpec

final class ResponsePlayTest extends AnyWordSpec {
  s"${classOf[Response].getSimpleName}" when {

    "Constructing responses with Play directives" should {

      "Be able to construct a response with a simple Play directive in a typesafe manner," +
        " that plays a sound file from a URL" in {

          val urlAsString               = "https://example.com"
          val result: Response.Verified = Response.build { responseBuilder =>
            responseBuilder
              .addPlay { playBuilder =>
                playBuilder.withSoundFileUrl(urlAsString).build()
              }
              .buildVerified()
          }

          val xmlCompact         = result.xmlCompact
          val expectedXmlCompact =
            s"""<?xml version="1.0" encoding="UTF-8"?><Response><Play>$urlAsString</Play></Response>"""
          assert(xmlCompact == expectedXmlCompact)

          val xmlPretty         = result.xmlPretty
          val expectedXmlPretty =
            s"""<?xml version="1.0" encoding="UTF-8"?>
               |<Response>
               |  <Play>$urlAsString</Play>
               |</Response>""".stripMargin
          assert(xmlPretty === expectedXmlPretty)
        }

      "should support playing DTMF digits" in {
        val result = Response.build { responseBuilder =>
          responseBuilder
            .addPlay { playBuilder =>
              playBuilder
                .withDigits(DtmfString(DtmfDigit.`1`, DtmfString.w, DtmfDigit.`*`))
                .build()
            }
            .buildVerified()
        }

        val xmlCompact         = result.xmlCompact
        val expectedXmlCompact =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Play digits="1w*"></Play></Response>"""
        assert(xmlCompact == expectedXmlCompact)

        val xmlPretty         = result.xmlPretty
        val expectedXmlPretty =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Play digits="1w*"></Play>
             |</Response>""".stripMargin
        assert(xmlPretty === expectedXmlPretty)
      }

      "should allow adding both digits and a Url and a Loop attribute" in {
        val result = Response.build { responseBuilder =>
          responseBuilder
            .addPlay { playBuilder =>
              playBuilder
                .withSoundFileUrl("https://example.com")
                .withDigits(DtmfString(DtmfDigit.`2`, DtmfDigit.`3`, DtmfDigit.`4`))
                .withLoop(88)
                .build()
            }
            .buildVerified()
        }

        val xmlCompact         = result.xmlCompact
        val expectedXmlCompact =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Play digits="234" loop="88">https://example.com</Play></Response>"""
        assert(xmlCompact == expectedXmlCompact)

        val xmlPretty         = result.xmlPretty
        val expectedXmlPretty =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Play digits="234" loop="88">https://example.com</Play>
             |</Response>""".stripMargin
        assert(xmlPretty === expectedXmlPretty)
      }

      "should not allow building empty instance" in {
        // format: off
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder
            |            .addPlay { playBuilder =>
            |              playBuilder.build()
            |            }
            |            .buildVerified()
            |        }
            |""".stripMargin)
        // format: on
      }

      "should not allow building an instance only with loop" in {
        // format: off
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder
            |            .addPlay { playBuilder =>
            |              playBuilder
            |                .withLoop(88)
            |                .build()
            |            }
            |            .buildVerified()
            |        }
            |""".stripMargin)
        // format: on
      }

      "should not allow adding multiple urls" in {
        // format: off
        assertTypeError(
        """Response.build { responseBuilder =>
           |          responseBuilder
           |            .addPlay { playBuilder =>
           |              playBuilder
           |                .withSoundFileUrl("https://example.com/")
           |                .withSoundFileUrl("https://example.com/about/")
           |                .build
           |            }
           |            .buildVerified()
           |        }
           |""".stripMargin)
        // format: on
      }

      "should not allow adding muliple digtits" in {
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder
            |            .addPlay { playBuilder =>
            |              playBuilder
            |                .withDigits(DtmfString(DtmfDigit.`1`, DtmfDigit.w, DtmfDigit.`*`))
            |                .withDigits(DtmfString(DtmfDigit.`2`, DtmfDigit.`3`, DtmfDigit.`4`))
            |                .build()
            |            }
            |            .buildVerified()
            |        }
            |""".stripMargin
        )
      }

      "should not allow multipleLoop" in {
        // format: off
        assertTypeError(
          """Response.build { responseBuilder =>
            |          responseBuilder
            |            .addPlay { playBuilder =>
            |              playBuilder
            |                .withSoundFileUrl("https://example.com")
            |                .withLoop(88)
            |                .withLoop(99)
            |                .build()
            |            }
            |            .buildVerified()
            |        }
            |""".stripMargin)
        // format: on
      }
    }
  }
}
