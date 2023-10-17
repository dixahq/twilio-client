package com.dixa.twilio.client.twilioClient.general

import akka.stream.scaladsl.Sink
import com.dixa.twilio.client.general.UsageTriggerReadRequestExecutor.UsageTriggerReadException
import com.dixa.twilio.client.general.{TwilioClientGeneral, UsageTriggerReadRequestExecutor}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{ApiException, TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.general.UsageTrigger
import com.dixa.twilio.model.iam.TwilioAccount
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalatest.matchers.should.Matchers

import java.time._
import scala.concurrent.Future

final class UsageTriggerReadTest extends TwilioClientTest with Matchers {

  classOf[TwilioClientGeneral].getSimpleName when {

    "read usage triggers" should {

      "Support reading all usage triggers from account" in {

        val f = new Fixture
        import f._

        val expected1 = usageTrigger(
          connSettings.accountSid,
          UsageTrigger.FriendlyName.unsafe("a trigger"),
          UsageTrigger.CurrentValue.unsafe("20"),
          UsageTrigger.Sid.unsafe("UTXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1")
        )

        val expected2 = usageTrigger(
          connSettings.accountSid,
          UsageTrigger.FriendlyName.unsafe("a test trigger"),
          UsageTrigger.CurrentValue.unsafe("10"),
          UsageTrigger.Sid.unsafe("UTXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2")
        )

        val usageTriggerListResp =
          s"""{
             |    "first_page_uri": "/2010-04-01/Accounts/${connSettings.accountSid}/Usage/Triggers.json?TriggerBy=count&UsageCategory=calls&Recurring=daily&Page=0&PageSize=50",
             |   "previous_page_uri": null,
             |   "usage_triggers": [
             |       {
             |         "usage_record_uri": "/2010-04-01/Accounts/${connSettings.accountSid}/Usage/Records/Today.json?Category=calls",
             |         "date_updated": "Sat, 29 Sep 2012 19:42:57 +0000",
             |         "date_fired": null,
             |         "friendly_name": "a trigger",
             |         "uri": "/2010-04-01/Accounts/${connSettings.accountSid}/Usage/Triggers/UTXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1.json",
             |         "account_sid": "${connSettings.accountSid}",
             |         "callback_method": "POST",
             |         "trigger_by": "count",
             |         "sid": "UTXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
             |         "current_value": "20",
             |         "date_created": "Sun, 23 Sep 2012 23:07:29 +0000",
             |         "callback_url": "http://www.google.com",
             |         "recurring": "",
             |         "usage_category": "calls",
             |         "trigger_value": "0.000000"
             |       },
             |       {
             |         "usage_record_uri": "/2010-04-01/Accounts/${connSettings.accountSid}/Usage/Records/Today.json?Category=calls",
             |         "date_updated": "Sat, 29 Sep 2012 19:42:57 +0000",
             |         "date_fired": null,
             |         "friendly_name": "a test trigger",
             |         "uri": "/2010-04-01/Accounts/${connSettings.accountSid}/Usage/Triggers/UTXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2.json",
             |         "account_sid": "${connSettings.accountSid}",
             |         "callback_method": "POST",
             |         "trigger_by": "count",
             |         "sid": "UTXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2",
             |         "current_value": "10",
             |         "date_created": "Sun, 23 Sep 2012 23:07:29 +0000",
             |         "callback_url": "http://www.google.com",
             |         "recurring": null,
             |         "usage_category": "calls",
             |         "trigger_value": "0.000000"
             |       }
             |   ],
             |   "uri": "/2010-04-01/Accounts/${connSettings.accountSid}/Usage/Triggers.json?TriggerBy=count&UsageCategory=calls&Recurring=daily",
             |   "page_size": 50,
             |   "next_page_uri": null,
             |   "page": 0
             |}
             |""".stripMargin

        val expectedPath = s"/2010-04-01/Accounts/${connSettings.accountSid}/Usage/Triggers.json?" +
          s"Recurring=daily&" +
          s"UsageCategory=calls&" +
          s"TriggerBy=count"

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlEqualTo(expectedPath)
            )
            .withBasicAuth(connSettings.accountSid.twilioString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(usageTriggerListResp)
            )
        )

        val req = UsageTriggerReadRequestExecutor.UsageTriggerReadRequest.builder(
          _.withAccountSid(connSettings.accountSid)
            .withTriggerBy(UsageTrigger.TriggerBy.Count)
            .withUsageCategory(UsageTrigger.UsageCategory.Calls)
            .withRecurring(UsageTrigger.Recurring.Daily)
            .build()
        )

        val resultFut: Future[
          Seq[Either[UsageTriggerReadRequestExecutor.UsageTriggerReadException, UsageTrigger]]
        ] =
          instance.source(connSettings, req).runWith(Sink.seq)
        resultFut.map { res =>
          res.map {
            case Left(e) =>
              fail(e)
            case Right(result) => result
          } shouldBe Vector(expected1, expected2)
        }
      }

      "Return a Left if credentials are wrong" in {
        val f = new Fixture
        import f._

        val expectedPath = s"/2010-04-01/Accounts/${connSettings.accountSid}/Usage/Triggers.json?" +
          s"Recurring=daily&" +
          s"UsageCategory=calls&" +
          s"TriggerBy=count"

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlEqualTo(expectedPath)
            )
            .withBasicAuth(connSettings.accountSid.twilioString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(401)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponseInvalidCredentials)
            )
        )

        val req = UsageTriggerReadRequestExecutor.UsageTriggerReadRequest.builder(
          _.withAccountSid(connSettings.accountSid)
            .withTriggerBy(UsageTrigger.TriggerBy.Count)
            .withUsageCategory(UsageTrigger.UsageCategory.Calls)
            .withRecurring(UsageTrigger.Recurring.Daily)
            .build()
        )

        val resultFut: Future[Seq[Either[UsageTriggerReadException, UsageTrigger]]] =
          instance.source(connSettings, req).runWith(Sink.seq)
        val expected =
          Left(UsageTriggerReadException.Api(ApiException.AuthenticationException()))
        resultFut.map { res =>
          res.size shouldBe 1
          res.headOption shouldBe Some(expected)
        }
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture {

    val connSettings    = TwilioTestConstants.connSettings(wireMockServer.port())
    val usageTriggerSid = UsageTrigger.Sid.unsafe("UTc2db285b0cbf4c60a2f1a8db237a5fba")

    def twilioResponseInvalidCredentials =
      """{
        |  "code": 20003,
        |  "detail": "Your AccountSid or AuthToken was incorrect.",
        |  "message": "Authentication Error - No credentials provided",
        |  "more_info": "https://www.twilio.com/docs/errors/20003",
        |  "status": 401
        |}
        |""".stripMargin

    private val createdAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2012, 9, 23), LocalTime.of(23, 7, 29)),
        ZoneOffset.UTC
      )
    )
    private val updatedAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2012, 9, 29), LocalTime.of(19, 42, 57)),
        ZoneOffset.UTC
      )
    )

    def usageTrigger(
        accountSid: TwilioAccount.Sid,
        friendlyName: UsageTrigger.FriendlyName,
        currentValue: UsageTrigger.CurrentValue,
        sid: UsageTrigger.Sid
    ) =
      UsageTrigger(
        accountSid = accountSid,
        callBackMethod = HttpMethod.Post,
        callbackUrl = CallbackUrl.UsageTriggerUrl("http://www.google.com"),
        currentValue = currentValue,
        dateCreated = createdAtInstant,
        dateFired = None,
        dateUpdated = updatedAtInstant,
        friendlyName = friendlyName,
        recurring = None,
        sid = sid,
        triggerBy = UsageTrigger.TriggerBy.Count,
        triggerValue = UsageTrigger.TriggerValue.unsafe("0.000000"),
        usageCategory = UsageTrigger.UsageCategory.Calls
      )

    val instance: UsageTriggerReadRequestExecutor =
      TwilioClient.defaultImpl().general.usageTriggerRead
  }
}
