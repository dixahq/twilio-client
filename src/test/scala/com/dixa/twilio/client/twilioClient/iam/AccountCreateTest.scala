package com.dixa.twilio.client.twilioClient.iam

import com.dixa.twilio.client.iam.AccountCreateRequestExecutor
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.{AuthToken, TwilioAccount}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.time.{Instant, ZoneOffset, ZonedDateTime}
import scala.concurrent.Future

final class AccountCreateTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to create an Account" should {
      "ask twilio to create it, and return the usage trigger it gets back from Twilio" in {

        val request = AccountCreateRequestExecutor.AccountCreateRequest.build(
          _.withFriendlyName(TwilioAccount.Name("Unit test created account"))
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts.json"
              )
            )
            .withRequestBody(WireMock.containing(s"FriendlyName=Unit+test+created+account"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
            )
        )

        val expected = TwilioAccount(
          TwilioAccount.Name("Unit test created account"),
          TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXNEW"),
          TwilioAccount.Status.Active,
          TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
          AuthToken.Primary("auth_token"),
          TwilioAccount.Type.Full,
          Instant.from(ZonedDateTime.of(2015, 7, 30, 20, 0, 0, 0, ZoneOffset.UTC)),
          Instant.from(ZonedDateTime.of(2015, 7, 30, 20, 0, 0, 0, ZoneOffset.UTC))
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: AccountCreateRequestExecutor =
          TwilioClient.defaultImpl().iam.accountCreate
        val resultFut: Future[
          Either[AccountCreateRequestExecutor.AccountCreateException, TwilioAccount]
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
       |  "auth_token": "auth_token",
       |  "date_created": "Thu, 30 Jul 2015 20:00:00 +0000",
       |  "date_updated": "Thu, 30 Jul 2015 20:00:00 +0000",
       |  "friendly_name": "Unit test created account",
       |  "owner_account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |  "sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXNEW",
       |  "status": "active",
       |  "subresource_uris": {
       |    "available_phone_numbers": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/AvailablePhoneNumbers.json",
       |    "calls": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls.json",
       |    "conferences": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Conferences.json",
       |    "incoming_phone_numbers": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IncomingPhoneNumbers.json",
       |    "notifications": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Notifications.json",
       |    "outgoing_caller_ids": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/OutgoingCallerIds.json",
       |    "recordings": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings.json",
       |    "transcriptions": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Transcriptions.json",
       |    "addresses": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Addresses.json",
       |    "signing_keys": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/SigningKeys.json",
       |    "connect_apps": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/ConnectApps.json",
       |    "sip": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/SIP.json",
       |    "authorized_connect_apps": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/AuthorizedConnectApps.json",
       |    "usage": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Usage.json",
       |    "keys": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Keys.json",
       |    "applications": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Applications.json",
       |    "short_codes": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/SMS/ShortCodes.json",
       |    "queues": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Queues.json",
       |    "messages": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages.json",
       |    "balance": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Balance.json"
       |  },
       |  "type": "Full",
       |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
       |}
       |""".stripMargin
}
