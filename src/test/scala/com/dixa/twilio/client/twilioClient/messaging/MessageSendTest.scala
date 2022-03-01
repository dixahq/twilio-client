package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.client.messaging.SmsSendRequestExecutor
import com.dixa.twilio.client.messaging.SmsSendRequestExecutor.{Response, SmsSendRequest}
import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.model.messaging._
import com.dixa.twilio.client.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType

import java.net.URL
import scala.concurrent.Future

final class MessageSendTest extends TwilioClientTest {
  private val testSid       = "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
  private val testAuthToken = "testAuthToken"

  private val pnUtil = PhoneNumberUtil.getInstance()
  private val pnUS1  = pnUtil.getExampleNumberForType("US", PhoneNumberType.MOBILE)
  private val pnUS2  = pnUtil.getExampleNumberForType("US", PhoneNumberType.MOBILE)

  private val from = s"+${pnUS1.getCountryCode}${pnUS1.getNationalNumber}"
  private val to   = s"+${pnUS2.getCountryCode}${pnUS2.getNationalNumber}"

  private val messageBody = "Hi there"

  private val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
  private val instance: SmsSendRequestExecutor = TwilioClient.defaultImpl().messaging.smsSend

//  private val date = Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.)
//    Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(date))

  private val messageSendTwilioSuccessResponse =
    s"""{
       |  "account_sid": "$testSid",
       |  "api_version": "2010-04-01",
       |  "body": "$messageBody",
       |  "date_created": "Thu, 30 Jul 2015 20:12:31 +0000",
       |  "date_sent": "Thu, 30 Jul 2015 20:12:33 +0000",
       |  "date_updated": "Thu, 30 Jul 2015 20:12:33 +0000",
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

  classOf[SmsSendRequestExecutor].getSimpleName when {
    "asked to send a message with the org's twilio account" should {
      "send a message successfully" in {

        wireMockServer.stubFor(
          WireMock
            .post(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/$testSid/Messages.json"))
            .withRequestBody(WireMock.containing(s"From=$from"))
            .withRequestBody(WireMock.containing(s"To=$to"))
            .withRequestBody(WireMock.containing(s"Body=$messageBody"))
            .withRequestBody(WireMock.containing(s"StatusCallback=http://random.com/v1/sms/status"))
            .withBasicAuth(testSid, testAuthToken)
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(messageSendTwilioSuccessResponse)
            )
        )

        val expected = Response(
          accountSid = TwilioAccount.Sid(testSid),
          body = MessageBody(messageBody),
          dateCreated = null,
          dateSent = null,
          dateUpdated = null,
          direction = MessageDirection.withName("OutboundApi"),
          from = MessageSender.E164(PhoneNumberE164(from)),
          messagingServiceSid = null,
          numMedia = 0,
          numSegments = MessageNumSegments("1"),
          price = null,
          priceUnit = null,
          sid = MessageSid("SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
          status = MessageStatus.withName("Sent"),
          to = PhoneNumberE164(to)
        )

        val messageSendRequest = SmsSendRequest(
          accountSid = TwilioAccount.Sid(testSid),
          from = MessageSender.E164(PhoneNumberE164(from)),
          to = PhoneNumberE164(to),
          body = MessageBody(messageBody),
          statusCallback = StatusCallback(new URL("http://random.com/v1/sms/status"))
        )

        val resultFut: Future[Response] =
          instance.unsafeRun(connSettings, messageSendRequest)
        resultFut.map(result => assert(result === expected))
      }

//      "throw if 'from' number does not support SMS" in {
//
//      }
//
//      "throw if 'to' number does not support SMS" in {
//
//      }
//
//      "throw if it fails to retrieve Twilio account" in {
//
//      }
    }
  }
}
