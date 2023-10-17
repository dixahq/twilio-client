package com.dixa.twilio.client.twilioClient.general

import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.general.UsageTriggerCreateRequestExecutor
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.general.UsageTrigger
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.time.{Instant, ZoneOffset, ZonedDateTime}
import scala.concurrent.Future

final class UsageTriggerCreateTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to create an usage trigger" should {
      "ask twilio to create it, and return the usage trigger it gets back from Twilio" in {

        val request = UsageTriggerCreateRequestExecutor.UsageTriggerCreateRequest.build(
          _.withAccountSid(CommonFixtures.accountSid1)
            .withCallbackUrl(CallbackUrl.UsageTriggerUrl("http://www.example.com"))
            .withTriggerValue(UsageTrigger.TriggerValue("1000.000000"))
            .withUsageCategory(UsageTrigger.UsageCategory.Sms)
            .withCallbackMethod(HttpMethod.Post)
            .withFriendlyName(UsageTrigger.FriendlyName.unsafe("Trigger for sms at usage of 1000"))
            .withRecurring(UsageTrigger.Recurring.Daily)
            .withTriggerBy(UsageTrigger.TriggerBy.Usage)
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/Usage/Triggers.json"
              )
            )
            .withRequestBody(
              WireMock.containing("CallbackUrl=http%3A%2F%2Fwww.example.com")
            )
            .withRequestBody(WireMock.containing("TriggerValue=1000.000000"))
            .withRequestBody(WireMock.containing(s"UsageCategory=sms"))
            .withRequestBody(WireMock.containing(s"CallbackMethod=POST"))
            .withRequestBody(WireMock.containing(s"FriendlyName=Trigger+for+sms+at+usage+of+1000"))
            .withRequestBody(WireMock.containing(s"Recurring=daily"))
            .withRequestBody(WireMock.containing(s"TriggerBy=usage"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
            )
        )

        val expected = UsageTrigger(
          CommonFixtures.accountSid1,
          HttpMethod.Post,
          CallbackUrl.UsageTriggerUrl("http://www.example.com"),
          UsageTrigger.CurrentValue("57"),
          Instant.from(ZonedDateTime.of(2012, 10, 13, 21, 32, 30, 0, ZoneOffset.UTC)),
          None,
          Instant.from(ZonedDateTime.of(2012, 10, 13, 21, 32, 30, 0, ZoneOffset.UTC)),
          UsageTrigger.FriendlyName.unsafe("Trigger for sms at usage of 1000"),
          Some(UsageTrigger.Recurring.Daily),
          UsageTrigger.Sid.unsafe("UTc142bed7b38c4f8186ef41a309814fd2"),
          UsageTrigger.TriggerBy.Usage,
          UsageTrigger.TriggerValue("1000.000000"),
          UsageTrigger.UsageCategory.Sms
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: UsageTriggerCreateRequestExecutor =
          TwilioClient.defaultImpl().general.usageTriggerCreate
        val resultFut: Future[
          Either[UsageTriggerCreateRequestExecutor.UsageTriggerCreateException, UsageTrigger]
        ] = {
          instance.run(connSettings, request)
        }
        resultFut.map { result =>
          val succResult = result.getOrElse {
            val e = result.left.getOrElse(fail("No success or either, how can that happen :D"))
            fail("expected successfully result here", e)
          }
          assert(succResult === expected)
        }
      }
    }
  }

  private def twilioResponse1 =
    s"""{
       |   "usage_record_uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Usage/Records.json?Category=sms",
       |   "date_updated": "Sat, 13 Oct 2012 21:32:30 +0000",
       |   "date_fired": null,
       |   "friendly_name": "Trigger for sms at usage of 1000",
       |   "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Usage/Triggers/UTc142bed7b38c4f8186ef41a309814fd2.json",
       |   "account_sid": "${CommonFixtures.accountSid1}",
       |   "callback_method": "POST",
       |   "trigger_by": "usage",
       |   "sid": "UTc142bed7b38c4f8186ef41a309814fd2",
       |   "current_value": "57",
       |   "date_created": "Sat, 13 Oct 2012 21:32:30 +0000",
       |   "callback_url": "http://www.example.com",
       |   "recurring": "daily",
       |   "usage_category": "sms",
       |   "trigger_value": "1000.000000"
       |}
       |""".stripMargin
}
