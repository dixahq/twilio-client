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

import com.dixa.twilio.client.ApiException.BadRequestException
import com.dixa.twilio.client.messaging.ChannelsSendersCreateRequestExecutor
import com.dixa.twilio.client.messaging.ChannelsSendersCreateRequestExecutor.ChannelsSendersException
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.HttpMethod.Post
import com.dixa.twilio.model.messaging.ChannelSender.{Webhook, Webhooks}
import com.dixa.twilio.model.messaging.MessageSender.{E164, Whatsapp}
import com.dixa.twilio.model.messaging.{ChannelSender, WhatsappNumber}
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson}

import scala.concurrent.Future

final class ChannelSendersCreateTest extends TwilioClientTest with ChannelSendersTestSharedFixture {

  "TwilioClientMessaging" when {
    "Asked to create a channel sender" should {

      "Call Twilio to create a Whatsapp sender" should {

        "handle 200 response" in {
          val f = new Fixture
          import f._

          wireMockServer.stubFor(
            wireMockBuilderExpectedTwilioRequest
              .withRequestBody(equalToJson(createChannelWhatsappSenderTwilioRequest))
              .willReturn(
                aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(createChannelWhatsappSenderTwilioResponse)
              )
          )

          val expected = Right(
            whatsappChannelSender.copy(
              properties = None,
              webhooks = Webhooks(
                callback = Some(ChannelSender.Webhook(Post, "https://webhook.messages")),
                fallback = None,
                statusCallback = None
              )
            )
          )

          val resultFut: Future[
            Either[ChannelsSendersException, ChannelSender]
          ] =
            instance.run(connSettings, createRequest)
          resultFut.map { result => assert(result === expected) }
        }

        "handle 202 response" in {
          val f = new Fixture
          import f._

          wireMockServer.stubFor(
            wireMockBuilderExpectedTwilioRequest
              .withRequestBody(equalToJson(createChannelWhatsappSenderTwilioRequest))
              .willReturn(
                aResponse()
                  .withStatus(202)
                  .withHeader("Content-Type", "application/json")
                  .withBody(createChannelWhatsappSenderTwilioResponse)
              )
          )

          val expected = Right(
            whatsappChannelSender.copy(
              properties = None,
              webhooks = Webhooks(
                callback = Some(ChannelSender.Webhook(Post, "https://webhook.messages")),
                fallback = None,
                statusCallback = None
              )
            )
          )

          val resultFut: Future[
            Either[ChannelsSendersException, ChannelSender]
          ] =
            instance.run(connSettings, createRequest)
          resultFut.map { result => assert(result === expected) }
        }
      }

      "Call Twilio to create a Whatsapp sender with phonenumber sender id and get exception" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .withRequestBody(equalToJson(createChannelWhatsappSenderTwilioRequest1))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createChannelWhatsappSenderTwilioResponse)
            )
        )

        val expected = Left(ChannelsSendersException.ChannelSenderNotSupported("+4552511283"))

        val resultFut: Future[
          Either[ChannelsSendersException, ChannelSender]
        ] =
          instance.run(connSettings, createRequest1)
        resultFut.map { result => assert(result === expected) }
      }

      "Call Twilio to create a Whatsapp sender with a waba id" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .withRequestBody(equalToJson(createChannelWhatsappSenderTwilioRequest2))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createChannelWhatsappSenderTwilioResponse)
            )
        )

        val req = ChannelsSendersCreateRequestExecutor.ChannelsSendersCreateRequest.build(
          _.withSenderId(Whatsapp(WhatsappNumber.unsafe("whatsapp:+4552511283")))
            .withConfiguration(
              ChannelSender.Configuration(
                wabaId = Some("316806161514452"),
                verificationMethod = Some(ChannelSender.VerificationMethod.SMS)
              )
            )
            .withWebhooks(Webhooks(None, None, None))
            .withProfile(
              ChannelSender.Profile.WhatsappProfile(phoneNumberDisplayName = "Example WABA")
            )
            .build()
        )

        val expected = Right(
          whatsappChannelSender.copy(
            properties = None,
            webhooks = Webhooks(
              callback = Some(ChannelSender.Webhook(Post, "https://webhook.messages")),
              fallback = None,
              statusCallback = None
            )
          )
        )

        val resultFut: Future[
          Either[ChannelsSendersException, ChannelSender]
        ] =
          instance.run(connSettings, req)
        resultFut.map { result => assert(result === expected) }
      }

      "fail when receiving a bad request with error code from Twilio" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .withRequestBody(equalToJson(createChannelBrokenWabaIdRequest))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(createChannelBrokenWabaIdResponse)
            )
        )
        val createBadRequest =
          ChannelsSendersCreateRequestExecutor.ChannelsSendersCreateRequest.build(
            _.withSenderId(Whatsapp(WhatsappNumber.unsafe("whatsapp:+4552511283")))
              .withConfiguration(
                ChannelSender.Configuration(wabaId = Some("316806161514452BROKENID"))
              )
              .withWebhooks(
                Webhooks(
                  None,
                  None,
                  callback = Some(
                    Webhook(
                      Post,
                      "https://example.com/v1/995304bc-1bd4-44d1-a3ba-4372239d269e/message"
                    )
                  )
                )
              )
              .withProfile(
                ChannelSender.Profile.WhatsappProfile(phoneNumberDisplayName = "Example WABA")
              )
              .build()
          )

        val expected =
          Left(ChannelsSendersException.Api(BadRequestException(createChannelBrokenWabaIdResponse)))

        val resultFut: Future[
          Either[ChannelsSendersException, ChannelSender]
        ] =
          instance.run(connSettings, createBadRequest)
        resultFut.map { result => assert(result === expected) }
      }

      "fail when receiving a 500 internal server error from Twilio" should {
        "map to TwilioInternalError with parsed error details" in {
          val f = new Fixture
          import f._

          wireMockServer.stubFor(
            wireMockBuilderExpectedTwilioRequest
              .withRequestBody(equalToJson(createChannelWhatsappSenderTwilioRequest))
              .willReturn(
                aResponse()
                  .withStatus(500)
                  .withHeader("Content-Type", "application/json")
                  .withBody(twilioInternalServerErrorResponse)
              )
          )

          val expected = Left(
            ChannelsSendersException.TwilioInternalError(
              errorCode = Some(20500L),
              errorMessage = Some("An internal server error has occurred"),
              moreInfo = Some("https://www.twilio.com/docs/errors/20500"),
              rawResponse = twilioInternalServerErrorResponse
            )
          )

          val resultFut: Future[
            Either[ChannelsSendersException, ChannelSender]
          ] =
            instance.run(connSettings, createRequest)
          resultFut.map { result => assert(result === expected) }
        }

        "map to TwilioInternalError with empty fields when response cannot be parsed" in {
          val f = new Fixture
          import f._

          val unparsableResponse = "Internal Server Error"

          wireMockServer.stubFor(
            wireMockBuilderExpectedTwilioRequest
              .withRequestBody(equalToJson(createChannelWhatsappSenderTwilioRequest))
              .willReturn(
                aResponse()
                  .withStatus(500)
                  .withHeader("Content-Type", "text/plain")
                  .withBody(unparsableResponse)
              )
          )

          val expected = Left(
            ChannelsSendersException.TwilioInternalError(
              errorCode = None,
              errorMessage = None,
              moreInfo = None,
              rawResponse = unparsableResponse
            )
          )

          val resultFut: Future[
            Either[ChannelsSendersException, ChannelSender]
          ] =
            instance.run(connSettings, createRequest)
          resultFut.map { result => assert(result === expected) }
        }
      }

      "fail when receiving a conflict response with error code from Twilio" should {
        "map to exception when receiving code 63100 'sender_id provided already exists'" in {
          val f = new Fixture
          import f._

          wireMockServer.stubFor(
            wireMockBuilderExpectedTwilioRequest
              .withRequestBody(equalToJson(createChannelWhatsappSenderTwilioRequest))
              .willReturn(
                aResponse()
                  .withStatus(409)
                  .withHeader("Content-Type", "application/json")
                  .withBody(createSenderIdAlreadyExistsResponse)
              )
          )

          val expected = Left(
            ChannelsSendersException.SenderIdAlreadyExists(
              "whatsapp:+4552511283",
              "sender_id provided already exists",
              "https://www.twilio.com/docs/errors/63100"
            )
          )

          val resultFut: Future[
            Either[ChannelsSendersException, ChannelSender]
          ] =
            instance.run(connSettings, createRequest)
          resultFut.map { result => assert(result === expected) }
        }

        "map to exception when receiving code 63103 'Could not extend credit line to the waba_id provided'" in {
          val f = new Fixture
          import f._

          wireMockServer.stubFor(
            wireMockBuilderExpectedTwilioRequest
              .withRequestBody(equalToJson(createChannelWhatsappSenderTwilioRequest2))
              .willReturn(
                aResponse()
                  .withStatus(409)
                  .withHeader("Content-Type", "application/json")
                  .withBody(couldNotExtendCreditLineResponse)
              )
          )

          val req = ChannelsSendersCreateRequestExecutor.ChannelsSendersCreateRequest.build(
            _.withSenderId(Whatsapp(WhatsappNumber.unsafe("whatsapp:+4552511283")))
              .withConfiguration(
                ChannelSender.Configuration(
                  wabaId = Some("316806161514452"),
                  verificationMethod = Some(ChannelSender.VerificationMethod.SMS)
                )
              )
              .withWebhooks(Webhooks(None, None, None))
              .withProfile(
                ChannelSender.Profile.WhatsappProfile(phoneNumberDisplayName = "Example WABA")
              )
              .build()
          )

          val expected = Left(
            ChannelsSendersException.CouldNotExtendCreditLine(
              Some("316806161514452"),
              "Could not extend credit line to the waba_id provided",
              "https://www.twilio.com/docs/errors/63103"
            )
          )

          val resultFut: Future[
            Either[ChannelsSendersException, ChannelSender]
          ] =
            instance.run(connSettings, req)
          resultFut.map { result => assert(result === expected) }
        }
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture {
    val createRequest = ChannelsSendersCreateRequestExecutor.ChannelsSendersCreateRequest.build(
      _.withSenderId(Whatsapp(WhatsappNumber.unsafe("whatsapp:+4552511283")))
        .withConfiguration(
          ChannelSender.Configuration(verificationMethod =
            Some(ChannelSender.VerificationMethod.SMS)
          )
        )
        .withWebhooks(Webhooks(None, None, None))
        .withProfile(ChannelSender.Profile.WhatsappProfile(phoneNumberDisplayName = "Example WABA"))
        .build()
    )

    val createRequest1 = ChannelsSendersCreateRequestExecutor.ChannelsSendersCreateRequest.build(
      _.withSenderId(E164(PhoneNumberE164.unsafe("+4552511283")))
        .withConfiguration(
          ChannelSender.Configuration(verificationMethod =
            Some(ChannelSender.VerificationMethod.SMS)
          )
        )
        .withWebhooks(Webhooks(None, None, None))
        .withProfile(ChannelSender.Profile.WhatsappProfile(phoneNumberDisplayName = "Example WABA"))
        .build()
    )

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(
        WireMock.urlPathEqualTo(
          "/v2/Channels/Senders"
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: ChannelsSendersCreateRequestExecutor =
      TwilioClient.defaultImpl().messaging.channelsSendersCreate
  }

  private def createChannelWhatsappSenderTwilioRequest =
    """{
      |    "sender_id": "whatsapp:+4552511283",
      |    "profile": {
      |        "name": "Example WABA"
      |    },
      |    "webhook": { },
      |    "configuration": {
      |        "verification_method": "sms"
      |    }
      |}
      |
      |""".stripMargin

  private def createChannelWhatsappSenderTwilioResponse =
    s"""{
       |    "status": "ONLINE",
       |    "profile": {
       |        "name": "Example WABA"
       |    },
       |    "offline_reasons": null,
       |    "sender_id": "whatsapp:+4552511283",
       |    "webhook": {
       |        "callback_method": "POST",
       |        "callback_url": "https://webhook.messages"
       |    },
       |    "url": "https://messaging.twilio.com/v2/Channels/Senders/XEcfd04c72e3397a53e24bd6c7408aff83",
       |    "sid": "XEcfd04c72e3397a53e24bd6c7408aff83",
       |    "configuration": {
       |        "waba_id": "316806161514452"
       |    },
       |    "properties": null
       |}
       |""".stripMargin

  private def createChannelWhatsappSenderTwilioRequest1 =
    """{
      |    "sender_id": "+4552511283",
      |    "profile": {
      |        "name": "Example WABA"
      |    },
      |    "webhook": { },
      |    "configuration": {
      |        "verification_method": "sms"
      |    }
      |}
      |
      |""".stripMargin

  private def createChannelWhatsappSenderTwilioRequest2 =
    """{
      |    "sender_id": "whatsapp:+4552511283",
      |    "profile": {
      |        "name": "Example WABA"
      |    },
      |    "webhook": { },
      |    "configuration": {
      |        "waba_id": "316806161514452",
      |        "verification_method": "sms"
      |    }
      |}
      |
      |""".stripMargin

  private def createChannelBrokenWabaIdRequest =
    """{
      |    "sender_id": "whatsapp:+4552511283",
      |    "profile": {
      |        "name": "Example WABA"
      |    },
      |    "configuration": {
      |        "waba_id": "316806161514452BROKENID"
      |    },
      |      "webhook": {
      |      "callback_url": "https://example.com/v1/995304bc-1bd4-44d1-a3ba-4372239d269e/message",
      |      "callback_method": "POST"
      |  }
      |}
      |""".stripMargin

  private def createSenderIdAlreadyExistsResponse =
    """{
      |"code":63100,
      |"message":"sender_id provided already exists",
      |"more_info":"https://www.twilio.com/docs/errors/63100",
      |"status":409}""".stripMargin

  private def createChannelBrokenWabaIdResponse =
    """{
      |"code": 63101,
      |"message": "waba_id provided is not valid or unable to be used",
      |"more_info": "https://www.twilio.com/docs/errors/63101",
      |"status": 400
      |}""".stripMargin

  private def couldNotExtendCreditLineResponse =
    """{
      |"code":63103,
      |"message":"Could not extend credit line to the waba_id provided",
      |"more_info":"https://www.twilio.com/docs/errors/63103",
      |"status":409}""".stripMargin

  private def twilioInternalServerErrorResponse =
    """{
      |"code":20500,
      |"message":"An internal server error has occurred",
      |"more_info":"https://www.twilio.com/docs/errors/20500",
      |"status":500}""".stripMargin

}
