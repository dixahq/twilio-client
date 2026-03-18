// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.twilioClient.iam

import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.iam.AccountFetchRequestExecutor.{
  AccountFetchException,
  AccountFetchRequest
}
import com.dixa.twilio.client.iam.{AccountFetchRequestExecutor, TwilioClientIam}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.TwilioAccount
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class FetchAccountTest extends TwilioClientTest {

  classOf[TwilioClientIam].getSimpleName when {
    "Asked to fetch an account" should {

      "Return the account that it receives from twilio" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
            )
        )

        val expected = Right(account1)

        val resultFut: Future[
          Either[AccountFetchException, TwilioAccount]
        ] =
          instance.run(connSettings, fetchRequest)
        resultFut.map(result => assert(result === expected))
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture extends CommonFixtures.Account {
    val fetchRequest = AccountFetchRequest(
      accountSid = accountSid1,
    )

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .get(
        WireMock.urlPathEqualTo(
          "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15.json"
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: AccountFetchRequestExecutor =
      TwilioClient.defaultImpl().iam.accountFetch
  }

  private def twilioResponse1 =
    """{
      |  "status": "active",
      |  "date_updated": "Wed, 23 Feb 2022 17:13:40 +0000",
      |  "auth_token": "AVerySecretValueThatShouldBeXXXX",
      |  "friendly_name": "CommonFixtures.Account.account1 friendly name",
      |  "owner_account_sid": "AC5fc6c53ce58165d0712d4a56fa29e23a",
      |  "uri": "/2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15.json",
      |  "sid": "ACf6c9aa4f2754c258aa45a6d2637cfa15",
      |  "date_created": "Mon, 26 Oct 2015 11:40:54 +0000",
      |  "type": "Full",
      |  "subresource_uris": {
      |    "addresses": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Addresses.json",
      |    "conferences": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Conferences.json",
      |    "signing_keys": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/SigningKeys.json",
      |    "transcriptions": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Transcriptions.json",
      |    "connect_apps": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/ConnectApps.json",
      |    "sip": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/SIP.json",
      |    "authorized_connect_apps": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/AuthorizedConnectApps.json",
      |    "usage": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Usage.json",
      |    "keys": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Keys.json",
      |    "applications": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Applications.json",
      |    "recordings": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Recordings.json",
      |    "short_codes": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/SMS/ShortCodes.json",
      |    "calls": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Calls.json",
      |    "notifications": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Notifications.json",
      |    "incoming_phone_numbers": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/IncomingPhoneNumbers.json",
      |    "queues": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Queues.json",
      |    "messages": "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15/Messages.json",
      |    "outgoing_caller_ids": "/2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15/OutgoingCallerIds.json",
      |    "available_phone_numbers": "/2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15/AvailablePhoneNumbers.json",
      |    "balance": "/2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15/Balance.json"
      |  }
      |}
      |""".stripMargin
}
