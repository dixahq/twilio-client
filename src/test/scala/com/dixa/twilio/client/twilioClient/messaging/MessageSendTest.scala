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

import com.dixa.twilio.client.TwilioTestConstants.{accountSid, authToken}
import com.dixa.twilio.client.messaging.MessageSendRequestExecutor
import com.dixa.twilio.client.messaging.MessageSendRequestExecutor.{
  MessageSendException,
  MessageSendRequest
}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.model.content.ContentTemplate
import com.dixa.twilio.client.{ApiException, TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.callback.CallbackUrl.MessageStatusCallback
import com.dixa.twilio.model.messaging._
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.net.{URL, URLEncoder}
import java.nio.charset.StandardCharsets
import scala.concurrent.Future

final class MessageSendTest extends TwilioClientTest {

  classOf[MessageSendRequestExecutor].getSimpleName when {
    "asked to send an sms" should {
      "successfully send an mms with a single media url" in {
        val f = new Fixture
        import f._

        val mediaUrl1 = new URL("https://example.com/media/abc.jpg")

        val messageSendTwilioSuccessResponse =
          s"""{
             |  "account_sid": "${accountSid}",
             |  "api_version": "2010-04-01",
             |  "body": "${messageBody}",
             |  "date_created": null,
             |  "date_sent": null,
             |  "date_updated": null,
             |  "direction": "outbound-api",
             |  "error_code": null,
             |  "error_message": null,
             |  "from": "${from}",
             |  "messaging_service_sid": null,
             |  "num_media": "1",
             |  "num_segments": "1",
             |  "price": null,
             |  "price_unit": null,
             |  "sid": "SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
             |  "status": "sent",
             |  "subresource_uris": {
             |    "media": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Media.json"
             |  },
             |  "to": "${to}",
             |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
             |}""".stripMargin

        val encMedia1 = URLEncoder.encode(mediaUrl1.toString, StandardCharsets.UTF_8.toString)

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .withRequestBody(WireMock.containing(s"MediaUrl=${encMedia1}"))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(messageSendTwilioSuccessResponse)
            )
        )

        val expected = Right(
          MessageResource(
            accountSid = accountSid,
            body = MessageBody(messageBody),
            dateCreated = None,
            dateSent = None,
            dateUpdated = None,
            direction = MessageDirection.withName("OutboundApi"),
            from = MessageSender.E164(PhoneNumberE164.unsafe(from)),
            messagingServiceSid = None,
            numMedia = 1,
            numSegments = MessageNumSegments(1),
            price = None,
            sid = Message.Sid.unsafe("SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            status = MessageStatus.withName("Sent"),
            to = MessageRecipient.E164(PhoneNumberE164.unsafe(to)),
            error = None
          )
        )

        val requestWithMedia =
          messageSendRequest.copy(mediaUrls = Seq(MediaResourceUrl(mediaUrl1.toString)))

        val resultFut: Future[
          Either[MessageSendException, MessageResource]
        ] =
          instance.run(connSettings, requestWithMedia)
        resultFut.map(result => assert(result === expected))
      }

      "successfully send an mms with multiple media urls" in {
        val f = new Fixture
        import f._

        val mediaUrl1 = new URL("https://example.com/media/abc.jpg")
        val mediaUrl2 = new URL("https://example.com/media/def.png")

        val messageSendTwilioSuccessResponse =
          s"""{
             |  "account_sid": "${accountSid}",
             |  "api_version": "2010-04-01",
             |  "body": "${messageBody}",
             |  "date_created": null,
             |  "date_sent": null,
             |  "date_updated": null,
             |  "direction": "outbound-api",
             |  "error_code": null,
             |  "error_message": null,
             |  "from": "${from}",
             |  "messaging_service_sid": null,
             |  "num_media": "2",
             |  "num_segments": "1",
             |  "price": null,
             |  "price_unit": null,
             |  "sid": "SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
             |  "status": "sent",
             |  "subresource_uris": {
             |    "media": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Media.json"
             |  },
             |  "to": "${to}",
             |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
             |}""".stripMargin

        val encMedia1 = URLEncoder.encode(mediaUrl1.toString, StandardCharsets.UTF_8.toString)
        val encMedia2 = URLEncoder.encode(mediaUrl2.toString, StandardCharsets.UTF_8.toString)

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .withRequestBody(WireMock.containing(s"MediaUrl=${encMedia1}"))
            .withRequestBody(WireMock.containing(s"MediaUrl=${encMedia2}"))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(messageSendTwilioSuccessResponse)
            )
        )

        val expected = Right(
          MessageResource(
            accountSid = accountSid,
            body = MessageBody(messageBody),
            dateCreated = None,
            dateSent = None,
            dateUpdated = None,
            direction = MessageDirection.withName("OutboundApi"),
            from = MessageSender.E164(PhoneNumberE164.unsafe(from)),
            messagingServiceSid = None,
            numMedia = 2,
            numSegments = MessageNumSegments(1),
            price = None,
            sid = Message.Sid.unsafe("SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            status = MessageStatus.withName("Sent"),
            to = MessageRecipient.E164(PhoneNumberE164.unsafe(to)),
            error = None
          )
        )

        val requestWithMedia = messageSendRequest.copy(
          mediaUrls = Seq(
            MediaResourceUrl(mediaUrl1.toString),
            MediaResourceUrl(mediaUrl2.toString)
          )
        )

        val resultFut: Future[
          Either[MessageSendException, MessageResource]
        ] =
          instance.run(connSettings, requestWithMedia)
        resultFut.map(result => assert(result === expected))
      }
      "successfully authenticate with Twilio and send an sms" in {
        val f = new Fixture
        import f._

        val messageSendTwilioSuccessResponse =
          s"""{
             |  "account_sid": "$accountSid",
             |  "api_version": "2010-04-01",
             |  "body": "$messageBody",
             |  "date_created": null,
             |  "date_sent": null,
             |  "date_updated": null,
             |  "direction": "outbound-api",
             |  "error_code": null,
             |  "error_message": null,
             |  "from": "$from",
             |  "messaging_service_sid": null,
             |  "num_media": "0",
             |  "num_segments": "1",
             |  "price": null,
             |  "price_unit": null,
             |  "sid": "SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
             |  "status": "sent",
             |  "subresource_uris": {
             |    "media": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Media.json"
             |  },
             |  "to": "$to",
             |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
             |}""".stripMargin

        wireMockServer.stubFor(
          WireMock
            .post(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/$accountSid/Messages.json"))
            .withRequestBody(WireMock.containing(reqEntity))
            .withBasicAuth(accountSid.toString, authToken.asString)
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(messageSendTwilioSuccessResponse)
            )
        )

        val expected = Right(
          MessageResource(
            accountSid = accountSid,
            body = MessageBody(messageBody),
            dateCreated = None,
            dateSent = None,
            dateUpdated = None,
            direction = MessageDirection.withName("OutboundApi"),
            from = MessageSender.E164(PhoneNumberE164.unsafe(from)),
            messagingServiceSid = None,
            numMedia = 0,
            numSegments = MessageNumSegments(1),
            price = None,
            sid = Message.Sid.unsafe("SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            status = MessageStatus.withName("Sent"),
            to = MessageRecipient.E164(PhoneNumberE164.unsafe(to)),
            error = None
          )
        )

        val resultFut: Future[
          Either[MessageSendException, MessageResource]
        ] =
          instance.run(connSettings, messageSendRequest)
        resultFut.map(result => assert(result === expected))
      }

      "return a failed Future when 'to' number is not a valid number" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponseToNumberNotValid)
            )
        )

        val resultFut: Future[
          Either[MessageSendException, MessageResource]
        ] =
          instance.run(connSettings, messageSendRequest)
        val expected = Left(new MessageSendException.ToNumberNotValid)
        resultFut.map(res => assert(res === expected))
      }

      "return a failed Future when 'from' number is not valid" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponseFromNumberNotValid)
            )
        )

        val resultFut: Future[
          Either[MessageSendException, MessageResource]
        ] =
          instance.run(connSettings, messageSendRequest)
        val expected = Left(new MessageSendException.FromNumberNotValid)
        resultFut.map(res => assert(res === expected))
      }

      "return a failed Future when 'from' number is not a twilio number and is not message-capable" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponseNotMessageCapableNumber)
            )
        )

        val resultFut: Future[
          Either[MessageSendException, MessageResource]
        ] =
          instance.run(connSettings, messageSendRequest)
        val expected = Left(new MessageSendException.NotMessageCapableNumber)
        resultFut.map(res => assert(res === expected))
      }

      "return a failed Future when char limit exceeded of 1600 chars" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponseMessageBodyCharLimitExceeded)
            )
        )

        val resultFut: Future[
          Either[MessageSendException, MessageResource]
        ] =
          instance.run(connSettings, messageSendRequest)
        val expected = Left(new MessageSendException.MessageBodyCharLimitExceeded)
        resultFut.map(res => assert(res === expected))
      }

      "return a failed Future when credentials are wrong" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(401)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponseInvalidCredentials)
            )
        )

        val resultFut: Future[
          Either[MessageSendException, MessageResource]
        ] =
          instance.run(connSettings, messageSendRequest)
        val expected = Left(MessageSendException.Api(ApiException.AuthenticationException()))
        resultFut.map(res => assert(res === expected))
      }
    }

    "asked to send a whatsapp message" should {
      "successfully send a whatsapp message to a phone number" in {
        val f = new Fixture
        import f._

        val messageSendTwilioSuccessResponse =
          s"""{
             |  "account_sid": "$accountSid",
             |  "api_version": "2010-04-01",
             |  "body": "$messageBody",
             |  "date_created": null,
             |  "date_sent": null,
             |  "date_updated": null,
             |  "direction": "outbound-api",
             |  "error_code": null,
             |  "error_message": null,
             |  "from": "${fromWhatsapp.toString}",
             |  "messaging_service_sid": null,
             |  "num_media": "0",
             |  "num_segments": "1",
             |  "price": null,
             |  "price_unit": null,
             |  "sid": "SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
             |  "status": "sent",
             |  "subresource_uris": {
             |    "media": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Media.json"
             |  },
             |  "to": "${toWhatsapp.toString}",
             |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
             |}""".stripMargin

        wireMockServer.stubFor(
          WireMock
            .post(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/$accountSid/Messages.json"))
            .withRequestBody(WireMock.containing(reqWhatsappEntityToPhoneNumber))
            .withBasicAuth(accountSid.toString, authToken.asString)
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(messageSendTwilioSuccessResponse)
            )
        )

        val expected = Right(
          MessageResource(
            accountSid = accountSid,
            body = MessageBody(messageBody),
            dateCreated = None,
            dateSent = None,
            dateUpdated = None,
            direction = MessageDirection.withName("OutboundApi"),
            from = MessageSender.Whatsapp(fromWhatsapp),
            messagingServiceSid = None,
            numMedia = 0,
            numSegments = MessageNumSegments(1),
            price = None,
            sid = Message.Sid.unsafe("SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            status = MessageStatus.withName("Sent"),
            to = MessageRecipient.WhatsappNumber(toWhatsapp),
            error = None
          )
        )

        val resultFut: Future[
          Either[MessageSendException, MessageResource]
        ] =
          instance.run(connSettings, messageSendRequestWhatsappToPhoneNumber)
        resultFut.map(result => assert(result === expected))
      }

      "successfully send a whatsapp message to an external user ID" in {
        val f = new Fixture
        import f._

        val messageSendTwilioSuccessResponse =
          s"""{
             |  "account_sid": "$accountSid",
             |  "api_version": "2010-04-01",
             |  "body": "$messageBody",
             |  "date_created": null,
             |  "date_sent": null,
             |  "date_updated": null,
             |  "direction": "outbound-api",
             |  "error_code": null,
             |  "error_message": null,
             |  "from": "${fromWhatsapp.toString}",
             |  "messaging_service_sid": null,
             |  "num_media": "0",
             |  "num_segments": "1",
             |  "price": null,
             |  "price_unit": null,
             |  "sid": "SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
             |  "status": "sent",
             |  "subresource_uris": {
             |    "media": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Media.json"
             |  },
             |  "to": "${toExternalUserIdWhatsapp.toString}",
             |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
             |}""".stripMargin

        wireMockServer.stubFor(
          WireMock
            .post(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/$accountSid/Messages.json"))
            .withRequestBody(WireMock.containing(reqWhatsappEntityToExternalUserId))
            .withBasicAuth(accountSid.toString, authToken.asString)
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(messageSendTwilioSuccessResponse)
            )
        )

        val expected = Right(
          MessageResource(
            accountSid = accountSid,
            body = MessageBody(messageBody),
            dateCreated = None,
            dateSent = None,
            dateUpdated = None,
            direction = MessageDirection.withName("OutboundApi"),
            from = MessageSender.Whatsapp(fromWhatsapp),
            messagingServiceSid = None,
            numMedia = 0,
            numSegments = MessageNumSegments(1),
            price = None,
            sid = Message.Sid.unsafe("SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            status = MessageStatus.withName("Sent"),
            to = MessageRecipient.WhatsappId(toExternalUserIdWhatsapp),
            error = None
          )
        )

        val resultFut: Future[
          Either[MessageSendException, MessageResource]
        ] =
          instance.run(connSettings, messageSendRequestWhatsappToExternalUserId)
        resultFut.map(result => assert(result === expected))
      }
    }

    "asked to send a template message" should {
      "successfully send a template message with a content sid only" in {
        val f = new Fixture
        import f._

        val messageSendTwilioSuccessResponse =
          s"""{
             |  "account_sid": "$accountSid",
             |  "api_version": "2010-04-01",
             |  "body": "$messageBody",
             |  "date_created": null,
             |  "date_sent": null,
             |  "date_updated": null,
             |  "direction": "outbound-api",
             |  "error_code": null,
             |  "error_message": null,
             |  "from": "$from",
             |  "messaging_service_sid": null,
             |  "num_media": "0",
             |  "num_segments": "1",
             |  "price": null,
             |  "price_unit": null,
             |  "sid": "SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
             |  "status": "sent",
             |  "subresource_uris": {
             |    "media": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Media.json"
             |  },
             |  "to": "$to",
             |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
             |}""".stripMargin

        val reqTemplateEntity =
          s"From=$encFrom&To=$encTo&ContentSid=$encContentSid&StatusCallback=$encStatusCallback"

        wireMockServer.stubFor(
          WireMock
            .post(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/$accountSid/Messages.json"))
            .withRequestBody(WireMock.containing(reqTemplateEntity))
            .withBasicAuth(accountSid.toString, authToken.asString)
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(messageSendTwilioSuccessResponse)
            )
        )

        val expected = Right(
          MessageResource(
            accountSid = accountSid,
            body = MessageBody(messageBody),
            dateCreated = None,
            dateSent = None,
            dateUpdated = None,
            direction = MessageDirection.withName("OutboundApi"),
            from = MessageSender.E164(PhoneNumberE164.unsafe(from)),
            messagingServiceSid = None,
            numMedia = 0,
            numSegments = MessageNumSegments(1),
            price = None,
            sid = Message.Sid.unsafe("SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            status = MessageStatus.withName("Sent"),
            to = MessageRecipient.E164(PhoneNumberE164.unsafe(to)),
            error = None
          )
        )

        val requestTemplate = MessageSendRequest(
          accountSid = accountSid,
          from = MessageSender.E164(PhoneNumberE164.unsafe(from)),
          to = MessageRecipient.E164(PhoneNumberE164.unsafe(to)),
          body = None,
          statusCallback = MessageStatusCallback(new URL(testStatusCallback)),
          contentSid = Some(contentSid)
        )

        val resultFut: Future[Either[MessageSendException, MessageResource]] =
          instance.run(connSettings, requestTemplate)
        resultFut.map(result => assert(result === expected))
      }

      "successfully send a template message with content variables" in {
        val f = new Fixture
        import f._

        val messageSendTwilioSuccessResponse =
          s"""{
             |  "account_sid": "$accountSid",
             |  "api_version": "2010-04-01",
             |  "body": "$messageBody",
             |  "date_created": null,
             |  "date_sent": null,
             |  "date_updated": null,
             |  "direction": "outbound-api",
             |  "error_code": null,
             |  "error_message": null,
             |  "from": "$from",
             |  "messaging_service_sid": null,
             |  "num_media": "0",
             |  "num_segments": "1",
             |  "price": null,
             |  "price_unit": null,
             |  "sid": "SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
             |  "status": "sent",
             |  "subresource_uris": {
             |    "media": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Media.json"
             |  },
             |  "to": "$to",
             |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
             |}""".stripMargin

        val reqTemplateEntity =
          s"From=$encFrom&To=$encTo&ContentSid=$encContentSid&ContentVariables=$encContentVariables&StatusCallback=$encStatusCallback"

        wireMockServer.stubFor(
          WireMock
            .post(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/$accountSid/Messages.json"))
            .withRequestBody(WireMock.containing(reqTemplateEntity))
            .withBasicAuth(accountSid.toString, authToken.asString)
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(messageSendTwilioSuccessResponse)
            )
        )

        val expected = Right(
          MessageResource(
            accountSid = accountSid,
            body = MessageBody(messageBody),
            dateCreated = None,
            dateSent = None,
            dateUpdated = None,
            direction = MessageDirection.withName("OutboundApi"),
            from = MessageSender.E164(PhoneNumberE164.unsafe(from)),
            messagingServiceSid = None,
            numMedia = 0,
            numSegments = MessageNumSegments(1),
            price = None,
            sid = Message.Sid.unsafe("SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            status = MessageStatus.withName("Sent"),
            to = MessageRecipient.E164(PhoneNumberE164.unsafe(to)),
            error = None
          )
        )

        val requestTemplate = MessageSendRequest(
          accountSid = accountSid,
          from = MessageSender.E164(PhoneNumberE164.unsafe(from)),
          to = MessageRecipient.E164(PhoneNumberE164.unsafe(to)),
          body = None,
          statusCallback = MessageStatusCallback(new URL(testStatusCallback)),
          contentSid = Some(contentSid),
          contentVariables = Map("1" -> "Jose")
        )

        val resultFut: Future[Either[MessageSendException, MessageResource]] =
          instance.run(connSettings, requestTemplate)
        resultFut.map(result => assert(result === expected))
      }
    }
  }

  private def twilioResponseToNumberNotValid =
    """{
      |"code": 21211,
      |"message": "The 'To' number <some_number> is not a valid phone number.",
      |"more_info": "https://www.twilio.com/docs/errors/21211",
      |"status": 400
      |}""".stripMargin

  private def twilioResponseFromNumberNotValid =
    """{
      |"code": 21212,
      |"message": "The 'From' number <some_number> is not a valid phone number, shortcode, or alphanumeric sender ID.",
      |"more_info": "https://www.twilio.com/docs/errors/21212",
      |"status": 400
      |}""".stripMargin

  private def twilioResponseNotMessageCapableNumber =
    """{
      |"code": 21606,
      |"message": "The From phone number <some_number> is not a valid, SMS-capable inbound phone number or short code for your account.",
      |"more_info": "https://www.twilio.com/docs/errors/21606",
      |"status": 400
      |}""".stripMargin

  private def twilioResponseMessageBodyCharLimitExceeded =
    """{
      |"code": 21617,
      |"message": "The concatenated message body exceeds the 1600 character limit.",
      |"more_info": "https://www.twilio.com/docs/errors/21617",
      |"status": 400
      |}""".stripMargin

  private def twilioResponseInvalidCredentials =
    """{
      |"code": 20003,
      |"detail": "Your AccountSid or AuthToken was incorrect.",
      |"message": "Authentication Error - invalid username",
      |"more_info": "https://www.twilio.com/docs/errors/20003",
      |"status": 401
      |}""".stripMargin

  // noinspection TypeAnnotation
  final class Fixture {
    val from                     = "+12015550123"
    val to                       = "+4532123456"
    val fromWhatsapp             = WhatsappPhoneNumber.unsafe(s"${WhatsappParticipant.Prefix}$from")
    val toWhatsapp               = WhatsappPhoneNumber.unsafe(s"${WhatsappParticipant.Prefix}$to")
    val toExternalUserIdWhatsapp =
      WhatsappExternalUserId.unsafe(s"${WhatsappParticipant.Prefix}LT.13491208655302741918")

    val messageBody        = "Hi there"
    val testStatusCallback = "http://example.com/v1/sms/status"
    val contentSid         = ContentTemplate.Sid.unsafe("HXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: MessageSendRequestExecutor = TwilioClient.defaultImpl().messaging.messageSend

    private def encode(s: String)   = URLEncoder.encode(s, StandardCharsets.UTF_8.toString)
    val encFrom                     = encode(from)
    val encTo                       = encode(to)
    val encWhatsappFrom             = encode(fromWhatsapp.toString)
    val encWhatsappTo               = encode(toWhatsapp.toString)
    val encToExternalUserIdWhatsapp = encode(toExternalUserIdWhatsapp.toString)
    val encBody                     = encode(messageBody)
    val encStatusCallback           = encode(testStatusCallback)
    val encContentSid               = encode(contentSid.toString)
    val encContentVariables         = encode("""{"1":"Jose"}""")

    val reqEntity = s"From=$encFrom&To=$encTo&Body=$encBody&StatusCallback=$encStatusCallback"
    val reqWhatsappEntityToPhoneNumber =
      s"From=$encWhatsappFrom&To=$encWhatsappTo&Body=$encBody&StatusCallback=$encStatusCallback"
    val reqWhatsappEntityToExternalUserId =
      s"From=$encWhatsappFrom&To=$encToExternalUserIdWhatsapp&Body=$encBody&StatusCallback=$encStatusCallback"

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/$accountSid/Messages.json"))
      .withRequestBody(WireMock.containing(reqEntity))
      .withBasicAuth(accountSid.toString, authToken.asString)
      .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))

    val messageSendRequest = MessageSendRequest(
      accountSid = accountSid,
      from = MessageSender.E164(PhoneNumberE164.unsafe(from)),
      to = MessageRecipient.E164(PhoneNumberE164.unsafe(to)),
      body = Some(MessageBody(messageBody)),
      statusCallback = MessageStatusCallback(new URL(testStatusCallback))
    )

    val messageSendRequestWhatsappToPhoneNumber = MessageSendRequest(
      accountSid = accountSid,
      from = MessageSender.Whatsapp(fromWhatsapp),
      to = MessageRecipient.WhatsappNumber(toWhatsapp),
      body = Some(MessageBody(messageBody)),
      statusCallback = MessageStatusCallback(new URL(testStatusCallback))
    )

    val messageSendRequestWhatsappToExternalUserId = MessageSendRequest(
      accountSid = accountSid,
      from = MessageSender.Whatsapp(fromWhatsapp),
      to = MessageRecipient.WhatsappId(toExternalUserIdWhatsapp),
      body = Some(MessageBody(messageBody)),
      statusCallback = MessageStatusCallback(new URL(testStatusCallback))
    )
  }
}
