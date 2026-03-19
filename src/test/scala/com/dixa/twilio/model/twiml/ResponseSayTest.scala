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

import com.dixa.twilio.model.twiml.verb.SayVerb.{LanguageCode, Voice}
import org.scalatest.wordspec.AnyWordSpec

final class ResponseSayTest extends AnyWordSpec {

  "Response" when {

    "Constructing responses with Say directives" should {

      "Be able to construct a response with a simple Say directive in a typesafe manner," +
        " even when text includes reserved xml chars" in {

          val textToSay        = """Twilio can you pronounce these reserved XML chars: "'<>&"""
          val textToSayEscaped =
            "Twilio can you pronounce these reserved XML chars: &quot;&apos;&lt;&gt;&amp;"

          val voice                     = Voice.`woman-EnGB`
          val result: Response.Verified = Response.build { responseBuilder =>
            responseBuilder
              .addSay { sayBuilder =>
                sayBuilder
                  .withText(textToSay)
                  .withVoice(voice)
                  .withLoop(5)
                  .build()
              }
              .buildVerified()
          }

          val xmlCompact         = result.xmlCompact
          val expectedXmlCompact =
            s"""<?xml version="1.0" encoding="UTF-8"?><Response><Say language="en-GB" voice="woman" loop="5">$textToSayEscaped</Say></Response>"""
          assert(xmlCompact == expectedXmlCompact)

          val xmlPretty         = result.xmlPretty
          val expectedXmlPretty =
            s"""<?xml version="1.0" encoding="UTF-8"?>
               |<Response>
               |  <Say language="en-GB" voice="woman" loop="5">$textToSayEscaped</Say>
               |</Response>""".stripMargin
          assert(xmlPretty === expectedXmlPretty)
        }

      "construct a valid response by providing only a language code when selecting a voice" in {

        val textToSay = "Some text to say"
        val language  = LanguageCode.`en-US`

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addSay { sayBuilder =>
              sayBuilder
                .withText(textToSay)
                .withBestQualityVoiceFemalePreferred(language)
                .withLoop(5)
                .build()
            }
            .buildVerified()
        }

        val xmlCompact         = result.xmlCompact
        val expectedXmlCompact =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Say language="en-US" voice="Polly.Joanna-Neural" loop="5">$textToSay</Say></Response>"""
        assert(xmlCompact == expectedXmlCompact)

        val xmlPretty         = result.xmlPretty
        val expectedXmlPretty =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Say language="en-US" voice="Polly.Joanna-Neural" loop="5">$textToSay</Say>
             |</Response>""".stripMargin
        assert(xmlPretty === expectedXmlPretty)
      }

      "construct a valid response and fallback to a male voice if male voice is the only voice that is available" in {

        val textToSay = "Some text to say"
        val language  = LanguageCode.`lt-LT`

        val result: Response.Verified = Response.build { responseBuilder =>
          responseBuilder
            .addSay { sayBuilder =>
              sayBuilder
                .withText(textToSay)
                .withBestQualityVoiceFemalePreferred(language)
                .withLoop(5)
                .build()
            }
            .buildVerified()
        }

        val xmlCompact         = result.xmlCompact
        val expectedXmlCompact =
          s"""<?xml version="1.0" encoding="UTF-8"?><Response><Say language="lt-LT" voice="Google.lt-LT-Standard-A" loop="5">$textToSay</Say></Response>"""
        assert(xmlCompact == expectedXmlCompact)

        val xmlPretty         = result.xmlPretty
        val expectedXmlPretty =
          s"""<?xml version="1.0" encoding="UTF-8"?>
             |<Response>
             |  <Say language="lt-LT" voice="Google.lt-LT-Standard-A" loop="5">$textToSay</Say>
             |</Response>""".stripMargin
        assert(xmlPretty === expectedXmlPretty)
      }
    }
  }
}
