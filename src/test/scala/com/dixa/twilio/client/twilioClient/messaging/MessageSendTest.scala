package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.client.TwilioTestConstants.{testAuthToken, testSid}
import com.dixa.twilio.client.messaging.MessageSendRequestExecutor
import com.dixa.twilio.client.messaging.MessageSendRequestExecutor.{
  MessageSendException,
  MessageSendRequest
}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging._
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{ApiException, TwilioClient, TwilioTestConstants}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.net.{URL, URLEncoder}
import java.nio.charset.StandardCharsets
import scala.concurrent.Future

final class MessageSendTest extends TwilioClientTest {

  classOf[MessageSendRequestExecutor].getSimpleName when {
    "asked to send an sms" should {
      "successfully authenticate with Twilio and send an sms" in {
        val f = new Fixture
        import f._

        val messageSendTwilioSuccessResponse =
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

        val expected = Right(
          MessageResource(
            accountSid = TwilioAccount.Sid(testSid),
            body = MessageBody(messageBody),
            dateCreated = None,
            dateSent = None,
            dateUpdated = None,
            direction = MessageDirection.withName("OutboundApi"),
            from = MessageSender.E164(PhoneNumberE164(from)),
            messagingServiceSid = None,
            numMedia = 0,
            numSegments = MessageNumSegments(1),
            price = None,
            sid = MessageSid("SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            status = MessageStatus.withName("Sent"),
            to = PhoneNumberE164(to),
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
    val from = "+12015550123"
    val to   = "+4532123456"

    val messageBody        = "Hi there"
    val testStatusCallback = "http://example.com/v1/sms/status"

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: MessageSendRequestExecutor = TwilioClient.defaultImpl().messaging.messageSend

    private def encode(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8.toString)
    val encFrom                   = encode(from)
    val encTo                     = encode(to)
    val encBody                   = encode(messageBody)
    val encStatusCallback         = encode(testStatusCallback)

    val reqEntity = s"From=$encFrom&To=$encTo&Body=$encBody&StatusCallback=$encStatusCallback"

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/$testSid/Messages.json"))
      .withRequestBody(WireMock.containing(reqEntity))
      .withBasicAuth(testSid, testAuthToken)
      .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))

    val messageSendRequest = MessageSendRequest(
      accountSid = TwilioAccount.Sid(testSid),
      from = MessageSender.E164(PhoneNumberE164(from)),
      to = PhoneNumberE164(to),
      body = MessageBody(messageBody),
      statusCallback = StatusCallback(new URL(testStatusCallback))
    )
  }
}
