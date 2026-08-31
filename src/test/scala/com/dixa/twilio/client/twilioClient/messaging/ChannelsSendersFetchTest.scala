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

package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.client.messaging.{
  ChannelsSendersCommonExceptions,
  ChannelsSendersFetchRequestExecutor
}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.messaging.ChannelSender
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class ChannelsSendersFetchTest
    extends TwilioClientTest
    with ChannelsSendersTestSharedFixture {

  "TwilioClientMessaging" when {
    "Asked to fetch an channel sender" should {

      "Return the whatsapp channel sender that it receives from twilio" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(channelWhatsappSenderTwilioResponse1)
            )
        )

        val expected = Right(whatsappChannelSender)

        val resultFut: Future[
          Either[ChannelsSendersCommonExceptions, ChannelSender]
        ] =
          instance.run(connSettings, fetchRequest)
        resultFut.map { result => assert(result === expected) }
      }

      "Return the offline reasons when twilio reports the sender as offline" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(channelWhatsappSenderOfflineTwilioResponse)
            )
        )

        val expected = Right(
          whatsappChannelSender.copy(
            status = ChannelSender.Status.Offline,
            configuration = ChannelSender.Configuration(
              wabaId = Some("316806161514452"),
              verificationMethod = Some(ChannelSender.VerificationMethod.SMS)
            ),
            offlineReasons = List(
              ChannelSender.OfflineReason(
                code = Some("410"),
                message = Some(
                  "Something went wrong. Please create a support ticket - Root Cause from " +
                    "provider: Phone Number In Use - This phone number is already registered to " +
                    "a WhatsApp account."
                ),
                moreInfo = Some("https://www.twilio.com/docs/errors/410")
              )
            )
          )
        )

        val resultFut: Future[
          Either[ChannelsSendersCommonExceptions, ChannelSender]
        ] =
          instance.run(connSettings, fetchRequest)
        resultFut.map { result => assert(result === expected) }
      }

      "Return exception if channel sender id isn't supported" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(channelUnknownSenderTwilioResponse1)
            )
        )

        val expected = ChannelsSendersCommonExceptions.ParseFailure(
          "Channel Sender ID: @twitterhandle of unknown type is not supported"
        )

        val resultFut: Future[
          Either[ChannelsSendersCommonExceptions, ChannelSender]
        ] =
          instance.run(connSettings, fetchRequest)
        resultFut.map {
          case Left(ex) => assert(ex == expected)
          case Right(_) => fail("should return left exception")
        }
      }

      "Return exception if channel sender id is e164 formatted phone number" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(channelTelephonySenderTwilioResponse1)
            )
        )

        val expected = ChannelsSendersCommonExceptions.ParseFailure(
          "PhoneNumber Channel Sender with ID: +4552511283 not supported"
        )

        val resultFut: Future[
          Either[ChannelsSendersCommonExceptions, ChannelSender]
        ] =
          instance.run(connSettings, fetchRequest)
        resultFut.map {
          case Left(ex) => assert(ex == expected)
          case Right(_) => fail("should return left exception")
        }
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture {
    val fetchRequest =
      ChannelsSendersFetchRequestExecutor.ChannelSenderFetchRequest(channelSenderSid =
        channelSenderSid
      )

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .get(
        WireMock.urlPathEqualTo(
          "/v2/Channels/Senders/XEcfd04c72e3397a53e24bd6c7408aff83"
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: ChannelsSendersFetchRequestExecutor =
      TwilioClient.defaultImpl().messaging.channelsSendersFetch
  }

  private def channelWhatsappSenderTwilioResponse1 =
    """{
      |    "status": "ONLINE",
      |    "profile": {
      |        "name": "Example WABA"
      |    },
      |    "url": "https://messaging.twilio.com/v2/Channels/Senders/XEfb45b27913a995543c9ccf5be843ee4",
      |    "sender_id": "whatsapp:+4552511283",
      |    "webhook": { },
      |    "sid": "XEcfd04c72e3397a53e24bd6c7408aff83",
      |    "configuration": {
      |        "waba_id": "316806161514452"
      |    },
      |    "properties":{ }
      |}
      |""".stripMargin

  /** Shape taken from a real production response for a sender stuck offline, with the profile and
    * ids swapped for the shared fixture's. Note that `code` arrives as a string, not a number.
    */
  private def channelWhatsappSenderOfflineTwilioResponse =
    """{
      |    "status": "OFFLINE",
      |    "profile": {
      |        "name": "Example WABA"
      |    },
      |    "offline_reasons": [
      |        {
      |            "code": "410",
      |            "message": "Something went wrong. Please create a support ticket - Root Cause from provider: Phone Number In Use - This phone number is already registered to a WhatsApp account.",
      |            "more_info": "https://www.twilio.com/docs/errors/410"
      |        }
      |    ],
      |    "url": "https://messaging.twilio.com/v2/Channels/Senders/XEfb45b27913a995543c9ccf5be843ee4",
      |    "sender_id": "whatsapp:+4552511283",
      |    "webhook": { },
      |    "sid": "XEcfd04c72e3397a53e24bd6c7408aff83",
      |    "configuration": {
      |        "waba_id": "316806161514452",
      |        "verification_method": "sms"
      |    },
      |    "properties":{ }
      |}
      |""".stripMargin

  private def channelUnknownSenderTwilioResponse1 =
    """{
      |    "status": "ONLINE",
      |    "profile": {
      |        "name": "Example WABA"
      |    },
      |    "url": "https://messaging.twilio.com/v2/Channels/Senders/XEfb45b27913a995543c9ccf5be843ee4",
      |    "sender_id": "@twitterhandle",
      |    "webhook": { },
      |    "sid": "XEcfd04c72e3397a53e24bd6c7408aff83",
      |    "configuration": {
      |        "waba_id": "316806161514452"
      |    },
      |    "properties":{ }
      |}
      |""".stripMargin

  private def channelTelephonySenderTwilioResponse1 =
    """{
      |    "status": "ONLINE",
      |    "profile": {
      |        "name": "Example WABA"
      |    },
      |    "url": "https://messaging.twilio.com/v2/Channels/Senders/XEfb45b27913a995543c9ccf5be843ee4",
      |    "sender_id": "+4552511283",
      |    "webhook": { },
      |    "sid": "XEcfd04c72e3397a53e24bd6c7408aff83",
      |    "configuration": {
      |        "waba_id": "316806161514452"
      |    },
      |    "properties":{ }
      |}
      |""".stripMargin
}
