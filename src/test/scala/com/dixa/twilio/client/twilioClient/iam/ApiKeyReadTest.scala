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

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.{Keep, Sink, Source}
import com.dixa.twilio.client.iam.ApiKeyReadRequestExecutor
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.{ApiKey, TwilioAccount}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class ApiKeyReadTest extends TwilioClientTest {
  "ApiKeyReadRequestExecutor" should {
    "parse keys correctly, returning ApiKey without secret" in {
      val accountSid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
      val request    = ApiKeyReadRequestExecutor.KeyReadRequest.build(
        _.withAccountSid(accountSid)
          .build()
      )

      val responseBody =
        s"""{
           |  "keys": [
           |    {
           |      "sid": "SKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
           |      "friendly_name": "Test Key 1",
           |      "date_created": "Thu, 24 Aug 2023 14:00:00 +0000",
           |      "date_updated": "Thu, 24 Aug 2023 14:00:00 +0000"
           |    },
           |    {
           |      "sid": "SKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2",
           |      "friendly_name": "Test Key 2",
           |      "date_created": "Thu, 24 Aug 2023 14:00:01 +0000",
           |      "date_updated": "Thu, 24 Aug 2023 14:00:01 +0000"
           |    }
           |  ]
           |}""".stripMargin

      wireMockServer.stubFor(
        WireMock
          .get(WireMock.urlPathEqualTo("/v1/Keys"))
          .withQueryParam("AccountSid", WireMock.equalTo(accountSid.toString))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(responseBody)
          )
      )

      val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
      val instance: ApiKeyReadRequestExecutor = TwilioClient.defaultImpl().iam.apiKeyRead

      val resultSource
          : Source[Either[ApiKeyReadRequestExecutor.KeyReadException, ApiKey], NotUsed] =
        instance.source(connSettings, request)

      val resultFut: Future[Seq[Either[ApiKeyReadRequestExecutor.KeyReadException, ApiKey]]] =
        resultSource.toMat(Sink.seq)(Keep.right).run()

      resultFut.map { results =>
        assert(results.size === 2)
        val key1 = results.head.getOrElse(fail(s"Expected success for key 1, got ${results.head}"))
        assert(key1.sid.value === "SKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1")
        assert(key1.friendlyName.twilioString === "Test Key 1")
        assert(key1.secretOpt === None)

        val key2 = results(1).getOrElse(fail(s"Expected success for key 2, got ${results(1)}"))
        assert(key2.sid.value === "SKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2")
        assert(key2.friendlyName.twilioString === "Test Key 2")
        assert(key2.secretOpt === None)
      }
    }
  }
}
