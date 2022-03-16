package com.dixa.twilio.client.twilioClient.messaging

import akka.http.scaladsl.model.DateTime
import akka.stream.scaladsl.Sink
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.messaging.TwilioClientMessaging.MessageResourcesReadRequestFilter
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging.MessageSid
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalatest.matchers.should.Matchers

import java.time._

final class MessageResourceReadTest extends TwilioClientTest with Matchers {

  classOf[TwilioClientMessaging].getSimpleName when {

    "messageResourceRead" should {

      "no message resources should turn into an empty list" in {

        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(
                  mediaResourceListResp(
                    connSettings.accountSid,
                    None,
                    None,
                    None,
                    List.empty
                  )
                )
            )
        )

        val instance = TwilioClient.defaultImpl().messaging
        val result =
          instance.messageResourceRead(connSettings, req).runWith(Sink.seq)
        result.map { result =>
          result.isEmpty shouldBe true
        }
      }
    }
  }

  final class Fixture {
    def messageResourceReferenceResp(
        accountSid: TwilioAccount.Sid,
        messageSid: MessageSid
    ): String = {
      s"""{
         |      "account_sid": "$accountSid",
         |      "api_version": "2010-04-01",
         |      "body": "testing",
         |      "date_created": "Fri, 24 May 2019 17:44:46 +0000",
         |      "date_sent": "Fri, 24 May 2019 17:44:50 +0000",
         |      "date_updated": "Fri, 24 May 2019 17:44:50 +0000",
         |      "direction": "outbound-api",
         |      "error_code": null,
         |      "error_message": null,
         |      "from": "+12019235161",
         |      "messaging_service_sid": null,
         |      "num_media": "0",
         |      "num_segments": "1",
         |      "price": "-0.00750",
         |      "price_unit": "USD",
         |      "sid": "$messageSid",
         |      "status": "sent",
         |      "subresource_uris": {
         |        "media": "/2010-04-01/Accounts/$accountSid/Messages/$messageSid/Media.json",
         |        "feedback": "/2010-04-01/Accounts/$accountSid/Messages/$messageSid/Feedback.json"
         |      },
         |      "to": "+18182008801",
         |      "uri": "/2010-04-01/Accounts/$accountSid/Messages/$messageSid.json"
         |    }""".stripMargin
    }

    def mediaResourceListResp(
        accountSid: TwilioAccount.Sid,
        to: Option[PhoneNumberE164],
        from: Option[PhoneNumberE164],
        dateSent: Option[DateTime],
        sids: List[MessageSid]
    ) =
      s"""{
         |  "end": 1,
         |  "first_page_uri": "/2010-04-01/Accounts/$accountSid/Messages.json?To=%2B$to&From=%2B$from&DateSent%3E=${dateSent
          .map { _.toIsoDateString }}&PageSize=2&Page=0",
         |  "next_page_uri": null,
         |  "page": 0,
         |  "page_size": 1000,
         |  "previous_page_uri": null,
         |  "messages": [
         |    ${sids.map(messageResourceReferenceResp(accountSid, _)).mkString(", ")}
         |  ],
         |  "start": 0,
         |  "uri": "/2010-04-01/Accounts/$accountSid/Messages.json?To=%2B$to&From=%2B$from&DateSent%3E=${dateSent
          .map { _.toIsoDateString }}&PageSize=2&Page=0"
         |}
         |""".stripMargin

    val accountSid   = TwilioAccount.Sid("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())

    val wireMockBuilderExpectedTwilioRequest =
      WireMock
        .get(
          WireMock.urlPathEqualTo(
            s"/2010-04-01/Accounts/$accountSid/Messages.json"
          )
        )
        .withBasicAuth(connSettings.accountSid.toString, "testPassword")

    val messageSid1 = MessageSid("SMded05904ccb347238880ca9264e8fe1c")

    val toPhoneNumber   = PhoneNumberE164("+18182008801")
    val fromPhoneNumber = PhoneNumberE164("+12019235161")

    val dateSent = DateTime(year = 2020, month = 4, day = 1)

    val req = TwilioClientMessaging.MessageResourceReadRequest(
      accountSid = accountSid,
      filter = MessageResourcesReadRequestFilter(
        to = Some(toPhoneNumber),
        from = Some(fromPhoneNumber),
        dateSent = Some(dateSent),
      )
    )
    val createdAt = "Tue, 01 Feb 2022 13:44:20 +0000"
    val updatedAt = "Wed, 02 Feb 2022 15:42:20 +0000"

    val createdAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2022, 2, 1), LocalTime.of(13, 44, 20)),
        ZoneOffset.UTC
      )
    )
    val updatedAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2022, 2, 2), LocalTime.of(15, 42, 20)),
        ZoneOffset.UTC
      )
    )
  }
}
