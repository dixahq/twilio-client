// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.twilioClient.iam

import com.dixa.twilio.client.iam.{AccessTokenCreateRequestExecutor, TwilioClientIam}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.general.Application
import com.dixa.twilio.model.iam.TwilioGrant.VoiceGrant

import scala.concurrent.duration._

final class AccessTokenCreateTest extends TwilioClientTest {

  classOf[TwilioClientIam].getSimpleName when {
    "Asked to create an access token" should {

      "Return a valid token string" in {
        val instance     = TwilioClient.defaultImpl().iam.accessTokenCreate
        val connSettings = TwilioTestConstants.connSettings(8080)

        val request = AccessTokenCreateRequestExecutor.AccessTokenCreateRequest.Builder.empty
          .withIdentity("user123")
          .addGrant(
            VoiceGrant(
              incomingAllow = true,
              outgoingAppSid = Some(Application.Sid.unsafe("APaaaabbbbccccdddd1111222233334444"))
            )
          )
          .build()

        instance.run(connSettings, request).map { resultEither =>
          val result = resultEither.toTry.get
          assert(result.token.nonEmpty)
          val parts = result.token.split('.')
          assert(parts.length === 3)
          // Header, Payload, Signature are all base64url encoded
        }
      }

      "Return an error if ttl is invalid" in {
        val instance     = TwilioClient.defaultImpl().iam.accessTokenCreate
        val connSettings = TwilioTestConstants.connSettings(8080)

        val request = AccessTokenCreateRequestExecutor.AccessTokenCreateRequest.Builder.empty
          .withIdentity("user123")
          .withTtl(0.seconds)
          .build()

        instance.run(connSettings, request).map {
          case Left(AccessTokenCreateRequestExecutor.AccessTokenCreateException.InvalidTtl(0)) =>
            // Success
            succeed
          case other =>
            fail(s"Expected InvalidTtl exception, got $other")
        }
      }
    }
  }
}
