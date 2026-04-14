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

import com.dixa.twilio.client.TwilioTestConstants
import com.dixa.twilio.client.iam.AccessTokenFactory
import com.dixa.twilio.model.Region
import com.dixa.twilio.model.general.Application
import com.dixa.twilio.model.iam.TwilioGrant.VoiceGrant
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._

final class AccessTokenCreateTest extends AnyWordSpec {

  classOf[AccessTokenFactory.type].getSimpleName when {

    "asked to generate an access token" should {

      "return a valid JWT string" in {
        val result = AccessTokenFactory.generate(
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

        val token = result.toTry.get
        assert(token.token.nonEmpty)
        val parts = token.token.split('.')
        assert(parts.length === 3)
      }

      "return an error if ttl is zero" in {
        val result = AccessTokenFactory.generate(
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
        val result = AccessTokenFactory.generate(
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
