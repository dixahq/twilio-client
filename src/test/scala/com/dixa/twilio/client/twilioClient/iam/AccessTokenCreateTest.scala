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

package com.dixa.twilio.client.twilioClient.iam

import java.util.Base64
import com.dixa.twilio.client.TwilioTestConstants
import com.dixa.twilio.client.iam.AccessTokenFactory
import com.dixa.twilio.model.Region
import com.dixa.twilio.model.general.Application
import com.dixa.twilio.model.iam.TwilioGrant.VoiceGrant
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._

final class AccessTokenCreateTest extends AnyWordSpec {

  private val decoder = Base64.getUrlDecoder

  private def decodeJson(base64url: String): ujson.Value =
    ujson.read(new String(decoder.decode(base64url), "UTF-8"))

  classOf[AccessTokenFactory].getSimpleName when {

    "asked to generate an access token" should {

      "produce a JWT with a valid header" in {
        val token = AccessTokenFactory.defaultImpl
          .generate(
            credentials = TwilioTestConstants.apiKeyCredentials,
            accountSid = TwilioTestConstants.accountSid,
            region = Region.Us1,
            identity = "user123",
            grants = Nil,
            ttl = 1.hour
          )
          .toTry
          .get

        val header = decodeJson(token.token.split('.')(0))
        assert(header("typ").str === "JWT")
        assert(header("alg").str === "HS256")
        assert(header("cty").str === "twilio-fpa;v=1")
        assert(header("twr").str === Region.Us1.twilioString)
      }

      "produce a JWT payload with the expected standard claims" in {
        val token = AccessTokenFactory.defaultImpl
          .generate(
            credentials = TwilioTestConstants.apiKeyCredentials,
            accountSid = TwilioTestConstants.accountSid,
            region = Region.Us1,
            identity = "user123",
            grants = Nil,
            ttl = 1.hour
          )
          .toTry
          .get

        val payload = decodeJson(token.token.split('.')(1))
        assert(payload("iss").str === TwilioTestConstants.apiKeySid.toString)
        assert(payload("sub").str === TwilioTestConstants.accountSid.toString)
        assert((payload("exp").num - payload("iat").num) === 3600.0)
      }

      "produce a JWT payload with the identity and grants" in {
        val token = AccessTokenFactory.defaultImpl
          .generate(
            credentials = TwilioTestConstants.apiKeyCredentials,
            accountSid = TwilioTestConstants.accountSid,
            region = Region.Us1,
            identity = "user123",
            grants = Seq(
              VoiceGrant(
                incomingAllow = true,
                outgoingAppSid = Some(Application.Sid.unsafe("APaaaabbbbccccdddd1111222233334444"))
              )
            ),
            ttl = 1.hour
          )
          .toTry
          .get

        val grants = decodeJson(token.token.split('.')(1))("grants")
        assert(grants("identity").str === "user123")
        assert(grants("voice")("incoming")("allow").bool === true)
        assert(
          grants("voice")("outgoing")(
            "application_sid"
          ).str === "APaaaabbbbccccdddd1111222233334444"
        )
      }

      "produce valid JSON in the grants object when no grants are provided" in {
        val token = AccessTokenFactory.defaultImpl
          .generate(
            credentials = TwilioTestConstants.apiKeyCredentials,
            accountSid = TwilioTestConstants.accountSid,
            region = Region.Us1,
            identity = "user123",
            grants = Nil,
            ttl = 1.hour
          )
          .toTry
          .get

        val grants = decodeJson(token.token.split('.')(1))("grants")
        assert(grants("identity").str === "user123")
        assert(grants.obj.size === 1)
      }

      "correctly escape special characters in the identity field" in {
        val token = AccessTokenFactory.defaultImpl
          .generate(
            credentials = TwilioTestConstants.apiKeyCredentials,
            accountSid = TwilioTestConstants.accountSid,
            region = Region.Us1,
            identity = """user"with\special""",
            grants = Nil,
            ttl = 1.hour
          )
          .toTry
          .get

        val grants = decodeJson(token.token.split('.')(1))("grants")
        assert(grants("identity").str === """user"with\special""")
      }

      "return an error if ttl is zero" in {
        val result = AccessTokenFactory.defaultImpl.generate(
          credentials = TwilioTestConstants.apiKeyCredentials,
          accountSid = TwilioTestConstants.accountSid,
          region = Region.Us1,
          identity = "user123",
          grants = Nil,
          ttl = 0.seconds
        )

        result match {
          case Left(AccessTokenFactory.Error.InvalidTtl(0)) => succeed
          case other => fail(s"Expected InvalidTtl(0), got $other")
        }
      }

      "return an error if ttl exceeds 24 hours" in {
        val result = AccessTokenFactory.defaultImpl.generate(
          credentials = TwilioTestConstants.apiKeyCredentials,
          accountSid = TwilioTestConstants.accountSid,
          region = Region.Us1,
          identity = "user123",
          grants = Nil,
          ttl = 25.hours
        )

        result match {
          case Left(AccessTokenFactory.Error.InvalidTtl(_)) => succeed
          case other => fail(s"Expected InvalidTtl, got $other")
        }
      }
    }
  }
}
