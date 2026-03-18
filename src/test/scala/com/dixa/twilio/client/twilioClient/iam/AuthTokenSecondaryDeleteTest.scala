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

import org.apache.pekko.Done
import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.iam.AuthTokenSecondaryDeleteRequestExecutor.{
  AuthTokenSecondaryDeleteException,
  AuthTokenSecondaryDeleteRequest
}
import com.dixa.twilio.client.iam.{AuthTokenSecondaryDeleteRequestExecutor, TwilioClientIam}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class AuthTokenSecondaryDeleteTest extends TwilioClientTest {

  classOf[TwilioClientIam].getSimpleName when {
    "Asked to delete a new secondary auth token" should {

      "Return Done if it succeeds" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(204) // this api returns 204 on success.
                .withHeader("Content-Type", "application/json")
            )
        )

        val resultFut: Future[Either[AuthTokenSecondaryDeleteException, Done]] =
          instance.run(connSettings, createRequest)
        resultFut.map { resultEither =>
          val result = resultEither.toTry.get
          assert(result === Done)
        }
      }

      "Return a not existing error in case of 404" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(
                  """{"code": 20404, "message": "The requested resource /AuthTokens/Secondary was not found", "more_info": "https://www.twilio.com/docs/errors/20404", "status": 404}"""
                )
            )
        )

        val expected =
          Left(AuthTokenSecondaryDeleteException.SecondaryAuthTokenNotFoundOnAccountException())

        val resultFut: Future[Either[AuthTokenSecondaryDeleteException, Done]] =
          instance.run(connSettings, createRequest)
        resultFut.map { resultEither => assert(resultEither === expected) }
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture extends CommonFixtures.AccountSid {
    val createRequest = AuthTokenSecondaryDeleteRequest()

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .delete(
        WireMock.urlPathEqualTo(
          "/v1/AuthTokens/Secondary"
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: AuthTokenSecondaryDeleteRequestExecutor =
      TwilioClient.defaultImpl().iam.authTokenSecondaryDelete
  }
}
