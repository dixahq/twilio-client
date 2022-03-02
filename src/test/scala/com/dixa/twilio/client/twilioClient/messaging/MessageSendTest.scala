package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.client.TwilioTestConstants.{testAuthToken, testSid}
import com.dixa.twilio.client.messaging.MessageSendRequestExecutor
import com.dixa.twilio.client.messaging.MessageSendRequestExecutor.{MessageSendRequest, Response}
import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.model.messaging._
import com.dixa.twilio.client.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType
import org.scalatest.concurrent.Eventually.eventually

import java.net.{URL, URLEncoder}
import java.nio.charset.StandardCharsets
import scala.concurrent.Future

final class MessageSendTest extends TwilioClientTest {

  private val pnUtil = PhoneNumberUtil.getInstance()
  private val pnUS   = pnUtil.getExampleNumberForType("US", PhoneNumberType.MOBILE)
  private val pnDK   = pnUtil.getExampleNumberForType("DK", PhoneNumberType.MOBILE)
  private val from   = s"+${pnUS.getCountryCode}${pnUS.getNationalNumber}"
  private val to     = s"+${pnDK.getCountryCode}${pnDK.getNationalNumber}"

  private val messageBody        = "Hi there"
  private val testStatusCallback = "http://random.com/v1/sms/status"

  private val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
  private val instance: MessageSendRequestExecutor = TwilioClient.defaultImpl().messaging.smsSend

  private def encode(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8.toString)
  private val encFrom           = encode(from)
  private val encTo             = encode(to)
  private val encBody           = encode(messageBody)
  private val encStatusCallback = encode(testStatusCallback)

  private val messageSendTwilioSuccessResponse =
    s"""{
       |  "account_sid": "$testSid",
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

  classOf[MessageSendRequestExecutor].getSimpleName when {
    "asked to send a message" should {
      "successfully authenticate with Twilio and send an sms" in {

        val reqEntity = s"From=$encFrom&To=$encTo&Body=$encBody&StatusCallback=$encStatusCallback"

        wireMockServer.stubFor(
          WireMock
            .post(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/$testSid/Messages.json"))
            .withRequestBody(WireMock.containing(reqEntity))
            .withBasicAuth(testSid, testAuthToken)
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(messageSendTwilioSuccessResponse)
            )
        )

        val messageSendRequest = MessageSendRequest(
          accountSid = TwilioAccount.Sid(testSid),
          from = MessageSender.E164(PhoneNumberE164(from)),
          to = PhoneNumberE164(to),
          body = MessageBody(messageBody),
          statusCallback = StatusCallback(new URL(testStatusCallback))
        )

        val expected = Response(
          accountSid = TwilioAccount.Sid(testSid),
          body = MessageBody(messageBody),
          dateCreated = None,
          dateSent = None,
          dateUpdated = None,
          direction = MessageDirection.withName("OutboundApi"),
          from = MessageSender.E164(PhoneNumberE164(from)),
          messagingServiceSid = None,
          numMedia = 0,
          numSegments = MessageNumSegments("1"),
          price = None,
          priceUnit = None,
          sid = MessageSid("SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
          status = MessageStatus.withName("Sent"),
          to = PhoneNumberE164(to)
        )

        val resultFut: Future[Response] =
          instance.unsafeRun(connSettings, messageSendRequest)
        resultFut.map(result => assert(result === expected))
      }

      "fail when the request is incorrect" in {

        val badToNumber = "1234567"
        val reqEntity =
          s"From=$encFrom&To=$badToNumber&Body=$encBody&StatusCallback=$encStatusCallback"

        val badRequestBody =
          """{
            | "code": "21614",
            | "message": "'To' number is not a valid mobile number.",
            | "more_info": "More info: https://www.twilio.com/docs/api/errors/21614",
            | "status": "400"
            |}""".stripMargin

        wireMockServer.stubFor(
          WireMock
            .post(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/$testSid/Messages.json"))
            .withRequestBody(WireMock.containing(reqEntity))
            .withBasicAuth(testSid, testAuthToken)
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(badRequestBody)
            )
        )

        val messageSendRequestIncorrect = MessageSendRequest(
          accountSid = TwilioAccount.Sid(testSid),
          from = MessageSender.E164(PhoneNumberE164(from)),
          to = PhoneNumberE164.unchecked(badToNumber),
          body = MessageBody(messageBody),
          statusCallback = StatusCallback(new URL(testStatusCallback))
        )

        val resultFut: Future[Response] =
          instance.unsafeRun(connSettings, messageSendRequestIncorrect)

        import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
        eventually {
          resultFut.value.isDefined shouldBe true
          resultFut.value.get.isFailure shouldBe true
          resultFut.value.get.failed.get.getMessage shouldBe "'To' number is not a valid mobile number. More info: https://www.twilio.com/docs/api/errors/21614"
        }
      }
    }
  }
}
