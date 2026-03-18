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

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.{Keep, Sink, Source}
import com.dixa.twilio.client.iam.ReadAllAccountsRequestExecutor.ReadAllAccountsRequest
import com.dixa.twilio.client.iam.{ReadAllAccountsRequestExecutor, TwilioClientIam}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.{AuthToken, TwilioAccount}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.time._

final class ReadAllAccountsTest extends TwilioClientTest {

  classOf[TwilioClient].getSimpleName when {

    "ask to read all active sub accounts" should {
      "return all the active sub accounts" ignore {

        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo("/2010-04-01/Accounts.json"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(getAccountsRequest1JsonResponse)
            )
        )
        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo("/2010-04-01/Accounts.json"))
            .withQueryParam("Page", WireMock.equalTo("1"))
            .withQueryParam(
              "PageToken",
              WireMock.equalTo(
                "PTJTdCJTIyZnJpZW5kbHlOYW1lJTIyJTNBbnVsbCUyQyUyMnN0YXR1cyUyMiUzQSUyMmFjdGl2ZSUyMiU3RCwyMDIxLTA5LTI3VDA1JTNBMTQlM0EwNS0wNyUzQTAwLCU1QiUyMkFDMzE4M2E3NDFmMWJhYjQ3NjRkYWMyNDkyYzhkMWZkODklMjIlNUQ6MTo0MjMy"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(getAccountsRequest2JsonResponse)
            )
        )

        val connSettings              = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TwilioClientIam = TwilioClient.defaultImpl().iam
        val req                       = ReadAllAccountsRequest(None)
        val resultSource: Source[
          Either[ReadAllAccountsRequestExecutor.ReadAllAccountsException, TwilioAccount],
          NotUsed
        ] =
          instance.accountRead.source(connSettings, req)
        val resultFut     = resultSource.toMat(Sink.seq)(Keep.right).run()
        val expectedValue = Set(
          Right(
            TwilioAccount(
              name = TwilioAccount.Name("Dixa main account"),
              sid = TwilioAccount.Sid.unsafe("AC5fc6c53ce58165d0712d4c93ca23e741"),
              status = TwilioAccount.Status.Active,
              ownerAccountSid = TwilioAccount.Sid.unsafe("AC5fc6c53ce58165d0712d4c93ca23e741"),
              authToken = AuthToken.Primary("go4oYeeShoozahb1ohdahbee6ahtevai"),
              accountType = TwilioAccount.Type.Full,
              timeCreated = Instant.from(
                OffsetDateTime.of(
                  LocalDateTime.of(LocalDate.of(2015, 9, 16), LocalTime.of(9, 18, 16)),
                  ZoneOffset.UTC
                )
              ),
              timeUpdated = Instant.from(
                OffsetDateTime.of(
                  LocalDateTime.of(LocalDate.of(2021, 7, 20), LocalTime.of(9, 54, 32)),
                  ZoneOffset.UTC
                )
              )
            )
          ),
          Right(
            TwilioAccount(
              name = TwilioAccount.Name("b1d45851-4ea1-4d28-9513-9fd770166a3e"),
              sid = TwilioAccount.Sid.unsafe("AC3183a741f1bab4764dac2492c8d1fd89"),
              status = TwilioAccount.Status.Active,
              ownerAccountSid = TwilioAccount.Sid.unsafe("AC5fc6c53ce58165d0712d4c93ca23e741"),
              authToken = AuthToken.Primary("shoos9reiWohzew2HoP7fei3Hoo2lai7"),
              accountType = TwilioAccount.Type.Full,
              timeCreated = Instant.from(
                OffsetDateTime.of(
                  LocalDateTime.of(LocalDate.of(2021, 9, 27), LocalTime.of(12, 14, 5)),
                  ZoneOffset.UTC
                )
              ),
              timeUpdated = Instant.from(
                OffsetDateTime.of(
                  LocalDateTime.of(LocalDate.of(2021, 9, 27), LocalTime.of(12, 14, 5)),
                  ZoneOffset.UTC
                )
              )
            )
          ),
          Right(
            TwilioAccount(
              name = TwilioAccount.Name("7f67d27b-6aa8-4a37-9dd4-8992ab3170ea"),
              sid = TwilioAccount.Sid.unsafe("AC4e8db239dc8664688a791d7f9cf45740"),
              status = TwilioAccount.Status.Active,
              ownerAccountSid = TwilioAccount.Sid.unsafe("AC5fc6c53ce58165d0712d4c93ca23e741"),
              authToken = AuthToken.Primary("ith0Zaeghie0phoshiet5eeteengaina"),
              accountType = TwilioAccount.Type.Full,
              timeCreated = Instant.from(
                OffsetDateTime.of(
                  LocalDateTime.of(LocalDate.of(2021, 9, 23), LocalTime.of(8, 59, 44)),
                  ZoneOffset.UTC
                )
              ),
              timeUpdated = Instant.from(
                OffsetDateTime.of(
                  LocalDateTime.of(LocalDate.of(2021, 9, 23), LocalTime.of(8, 59, 44)),
                  ZoneOffset.UTC
                )
              )
            )
          )
        )
        resultFut.map { result =>
          result.map { either => assert(either.isRight) }
          assert(result.toSet === expectedValue)
        }
      }

      "should provide the friendly name and status query parameters to Twilio, if they are set in the request" in {

        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo("/2010-04-01/Accounts.json"))
            .withQueryParam("Status", WireMock.equalTo("suspended"))
            .withQueryParam("FriendlyName", WireMock.equalTo("FriendlyNameToQueryFor"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(getAccountsRequest1JsonResponse3)
            )
        )

        val connSettings              = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TwilioClientIam = TwilioClient.defaultImpl().iam
        val req                       = ReadAllAccountsRequest(
          Some(TwilioAccount.Status.Suspended),
          Some(TwilioAccount.Name("FriendlyNameToQueryFor"))
        )
        val resultSource: Source[
          Either[ReadAllAccountsRequestExecutor.ReadAllAccountsException, TwilioAccount],
          NotUsed
        ] =
          instance.accountRead.source(connSettings, req)
        val resultFut     = resultSource.toMat(Sink.seq)(Keep.right).run()
        val expectedValue = Set(
          Right(
            TwilioAccount(
              name = TwilioAccount.Name("FriendlyNameToQueryFor"),
              sid = TwilioAccount.Sid.unsafe("AC5fc6c53ce58165d0712d4c93ca23e741"),
              status = TwilioAccount.Status.Suspended,
              ownerAccountSid = TwilioAccount.Sid.unsafe("AC5fc6c53ce58165d0712d4c93ca23e741"),
              authToken = AuthToken.Primary("go4oYeeShoozahb1ohdahbee6ahtevai"),
              accountType = TwilioAccount.Type.Full,
              timeCreated = Instant.from(
                OffsetDateTime.of(
                  LocalDateTime.of(LocalDate.of(2015, 9, 16), LocalTime.of(9, 18, 16)),
                  ZoneOffset.UTC
                )
              ),
              timeUpdated = Instant.from(
                OffsetDateTime.of(
                  LocalDateTime.of(LocalDate.of(2021, 7, 20), LocalTime.of(9, 54, 32)),
                  ZoneOffset.UTC
                )
              )
            )
          )
        )
        resultFut.map { result =>
          result.map { either => assert(either.isRight, either) }
          assert(result.toSet === expectedValue)
        }
      }
    }

  }

  private def getAccountsRequest1JsonResponse =
    """{
      |  "first_page_uri": "/2010-04-01/Accounts.json?Status=active&PageSize=2&Page=0",
      |  "end": 1,
      |  "previous_page_uri": null,
      |  "uri": "/2010-04-01/Accounts.json?Status=active&PageSize=2&Page=0",
      |  "page_size": 2,
      |  "start": 0,
      |  "accounts": [
      |    {
      |      "status": "active",
      |      "date_updated": "Tue, 20 Jul 2021 09:54:32 +0000",
      |      "auth_token": "go4oYeeShoozahb1ohdahbee6ahtevai",
      |      "friendly_name": "Dixa main account",
      |      "owner_account_sid": "AC5fc6c53ce58165d0712d4c93ca23e741",
      |      "uri": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741.json",
      |      "sid": "AC5fc6c53ce58165d0712d4c93ca23e741",
      |      "date_created": "Wed, 16 Sep 2015 09:18:16 +0000",
      |      "type": "Full",
      |      "subresource_uris": {
      |        "addresses": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Addresses.json",
      |        "conferences": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Conferences.json",
      |        "signing_keys": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/SigningKeys.json",
      |        "transcriptions": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Transcriptions.json",
      |        "connect_apps": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/ConnectApps.json",
      |        "sip": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/SIP.json",
      |        "authorized_connect_apps": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/AuthorizedConnectApps.json",
      |        "usage": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Usage.json",
      |        "keys": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Keys.json",
      |        "applications": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Applications.json",
      |        "recordings": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Recordings.json",
      |        "short_codes": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/SMS/ShortCodes.json",
      |        "calls": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Calls.json",
      |        "notifications": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Notifications.json",
      |        "incoming_phone_numbers": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/IncomingPhoneNumbers.json",
      |        "queues": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Queues.json",
      |        "messages": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Messages.json",
      |        "outgoing_caller_ids": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/OutgoingCallerIds.json",
      |        "available_phone_numbers": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/AvailablePhoneNumbers.json",
      |        "balance": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Balance.json"
      |      }
      |    },
      |    {
      |      "status": "active",
      |      "date_updated": "Mon, 27 Sep 2021 12:14:05 +0000",
      |      "auth_token": "shoos9reiWohzew2HoP7fei3Hoo2lai7",
      |      "friendly_name": "b1d45851-4ea1-4d28-9513-9fd770166a3e",
      |      "owner_account_sid": "AC5fc6c53ce58165d0712d4c93ca23e741",
      |      "uri": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89.json",
      |      "sid": "AC3183a741f1bab4764dac2492c8d1fd89",
      |      "date_created": "Mon, 27 Sep 2021 12:14:05 +0000",
      |      "type": "Full",
      |      "subresource_uris": {
      |        "addresses": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/Addresses.json",
      |        "conferences": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/Conferences.json",
      |        "signing_keys": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/SigningKeys.json",
      |        "transcriptions": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/Transcriptions.json",
      |        "connect_apps": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/ConnectApps.json",
      |        "sip": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/SIP.json",
      |        "authorized_connect_apps": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/AuthorizedConnectApps.json",
      |        "usage": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/Usage.json",
      |        "keys": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/Keys.json",
      |        "applications": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/Applications.json",
      |        "recordings": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/Recordings.json",
      |        "short_codes": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/SMS/ShortCodes.json",
      |        "calls": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/Calls.json",
      |        "notifications": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/Notifications.json",
      |        "incoming_phone_numbers": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/IncomingPhoneNumbers.json",
      |        "queues": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/Queues.json",
      |        "messages": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/Messages.json",
      |        "outgoing_caller_ids": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/OutgoingCallerIds.json",
      |        "available_phone_numbers": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/AvailablePhoneNumbers.json",
      |        "balance": "/2010-04-01/Accounts/AC3183a741f1bab4764dac2492c8d1fd89/Balance.json"
      |      }
      |    }
      |  ],
      |  "next_page_uri": "/2010-04-01/Accounts.json?Status=active&PageSize=2&Page=1&PageToken=PTJTdCJTIyZnJpZW5kbHlOYW1lJTIyJTNBbnVsbCUyQyUyMnN0YXR1cyUyMiUzQSUyMmFjdGl2ZSUyMiU3RCwyMDIxLTA5LTI3VDA1JTNBMTQlM0EwNS0wNyUzQTAwLCU1QiUyMkFDMzE4M2E3NDFmMWJhYjQ3NjRkYWMyNDkyYzhkMWZkODklMjIlNUQ6MTo0MjMy",
      |  "page": 0
      |}
      |""".stripMargin

  private def getAccountsRequest2JsonResponse =
    """{
      |  "first_page_uri": "/2010-04-01/Accounts.json?Status=active&PageSize=2&Page=0",
      |  "end": 2,
      |  "previous_page_uri": "/2010-04-01/Accounts.json?Status=active&PageSize=2&Page=0&PageToken=PTJTdCJTIyZnJpZW5kbHlOYW1lJTIyJTNBbnVsbCUyQyUyMnN0YXR1cyUyMiUzQSUyMmFjdGl2ZSUyMiU3RCwyMDIxLTA5LTIzVDAxJTNBNTklM0E0NC0wNyUzQTAwLCU1QiUyMkFDNGU4ZGIyMzlkYzg2NjQ2ODhhNzkxZDdmOWNmNDU3NDAlMjIlNUQ6Mjo0MjMy",
      |  "uri": "/2010-04-01/Accounts.json?Status=active&PageSize=2&Page=1&PageToken=PTJTdCJTIyZnJpZW5kbHlOYW1lJTIyJTNBbnVsbCUyQyUyMnN0YXR1cyUyMiUzQSUyMmFjdGl2ZSUyMiU3RCwyMDIxLTA5LTI3VDA1JTNBMTQlM0EwNS0wNyUzQTAwLCU1QiUyMkFDMzE4M2E3NDFmMWJhYjQ3NjRkYWMyNDkyYzhkMWZkODklMjIlNUQ6MTo0MjMy",
      |  "page_size": 2,
      |  "start": 2,
      |  "accounts": [
      |    {
      |      "status": "active",
      |      "date_updated": "Thu, 23 Sep 2021 08:59:44 +0000",
      |      "auth_token": "ith0Zaeghie0phoshiet5eeteengaina",
      |      "friendly_name": "7f67d27b-6aa8-4a37-9dd4-8992ab3170ea",
      |      "owner_account_sid": "AC5fc6c53ce58165d0712d4c93ca23e741",
      |      "uri": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740.json",
      |      "sid": "AC4e8db239dc8664688a791d7f9cf45740",
      |      "date_created": "Thu, 23 Sep 2021 08:59:44 +0000",
      |      "type": "Full",
      |      "subresource_uris": {
      |        "addresses": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/Addresses.json",
      |        "conferences": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/Conferences.json",
      |        "signing_keys": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/SigningKeys.json",
      |        "transcriptions": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/Transcriptions.json",
      |        "connect_apps": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/ConnectApps.json",
      |        "sip": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/SIP.json",
      |        "authorized_connect_apps": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/AuthorizedConnectApps.json",
      |        "usage": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/Usage.json",
      |        "keys": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/Keys.json",
      |        "applications": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/Applications.json",
      |        "recordings": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/Recordings.json",
      |        "short_codes": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/SMS/ShortCodes.json",
      |        "calls": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/Calls.json",
      |        "notifications": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/Notifications.json",
      |        "incoming_phone_numbers": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/IncomingPhoneNumbers.json",
      |        "queues": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/Queues.json",
      |        "messages": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/Messages.json",
      |        "outgoing_caller_ids": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/OutgoingCallerIds.json",
      |        "available_phone_numbers": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/AvailablePhoneNumbers.json",
      |        "balance": "/2010-04-01/Accounts/AC4e8db239dc8664688a791d7f9cf45740/Balance.json"
      |      }
      |    }
      |  ],
      |  "next_page_uri": null,
      |  "page": 1
      |}
      |""".stripMargin

  private def getAccountsRequest1JsonResponse3 =
    """{
      |  "first_page_uri": "/2010-04-01/Accounts.json?Status=suspended&PageSize=1&FriendlyName=FriendlyNameToQueryFor&Page=0",
      |  "end": 1,
      |  "previous_page_uri": null,
      |  "uri": "/2010-04-01/Accounts.json?Status=active&PageSize=2&Page=0",
      |  "page_size": 1,
      |  "start": 0,
      |  "accounts": [
      |    {
      |      "status": "suspended",
      |      "date_updated": "Tue, 20 Jul 2021 09:54:32 +0000",
      |      "auth_token": "go4oYeeShoozahb1ohdahbee6ahtevai",
      |      "friendly_name": "FriendlyNameToQueryFor",
      |      "owner_account_sid": "AC5fc6c53ce58165d0712d4c93ca23e741",
      |      "uri": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741.json",
      |      "sid": "AC5fc6c53ce58165d0712d4c93ca23e741",
      |      "date_created": "Wed, 16 Sep 2015 09:18:16 +0000",
      |      "type": "Full",
      |      "subresource_uris": {
      |        "addresses": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Addresses.json",
      |        "conferences": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Conferences.json",
      |        "signing_keys": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/SigningKeys.json",
      |        "transcriptions": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Transcriptions.json",
      |        "connect_apps": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/ConnectApps.json",
      |        "sip": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/SIP.json",
      |        "authorized_connect_apps": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/AuthorizedConnectApps.json",
      |        "usage": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Usage.json",
      |        "keys": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Keys.json",
      |        "applications": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Applications.json",
      |        "recordings": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Recordings.json",
      |        "short_codes": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/SMS/ShortCodes.json",
      |        "calls": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Calls.json",
      |        "notifications": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Notifications.json",
      |        "incoming_phone_numbers": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/IncomingPhoneNumbers.json",
      |        "queues": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Queues.json",
      |        "messages": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Messages.json",
      |        "outgoing_caller_ids": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/OutgoingCallerIds.json",
      |        "available_phone_numbers": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/AvailablePhoneNumbers.json",
      |        "balance": "/2010-04-01/Accounts/AC5fc6c53ce58165d0712d4c93ca23e741/Balance.json"
      |      }
      |    }
      |  ],
      |  "next_page_uri": null,
      |  "page": 0
      |}
      |""".stripMargin
}
