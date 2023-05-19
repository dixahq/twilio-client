package com.dixa.twilio.client.twilioClient.iam

import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.iam.AccountUpdateRequestExecutor.{
  AccountUpdateException,
  AccountUpdateRequest
}
import com.dixa.twilio.client.iam.{AccountUpdateRequestExecutor, TwilioClientIam}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.TwilioAccount
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo}

import scala.concurrent.Future

final class AccountUpdateTest extends TwilioClientTest {

  classOf[TwilioClientIam].getSimpleName when {
    "Asked to update an account" should {

      "update both status and friendly name and return the account that it receives back from twilio" in {
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

        val expected = Right(
          account1.copy(
            name = TwilioAccount.Name("newFriendlyName"),
            status = TwilioAccount.Status.Suspended
          )
        )

        val resultFut: Future[
          Either[AccountUpdateException, TwilioAccount]
        ] =
          instance.run(connSettings, updateRequest)
        resultFut.map { result =>
          if (result.isLeft) fail(result.swap.getOrElse(fail()))
          assert(result === expected)
        }
      }

      "return an error if account does not exists" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(
                  """{"code": 20404, "message": "The requested resource /2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15.json was not found", "more_info": "https://www.twilio.com/docs/errors/20404", "status": 404}"""
                )
            )
        )

        val expected = Left(AccountUpdateException.AccountNotFound(accountSid1))

        val resultFut: Future[
          Either[AccountUpdateException, TwilioAccount]
        ] =
          instance.run(connSettings, updateRequest)
        resultFut.map(result => assert(result === expected))
      }

      "return an error if you try to activate an closed account" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(
                  """{"code": 21479, "message": "Unable to update Status for subaccount, subaccount is closed and can not be re-opened.", "more_info": "https://www.twilio.com/docs/errors/21479", "status": 400}"""
                )
            )
        )

        val expected = Left(AccountUpdateException.ClosedAccountCannotBeReopened(accountSid1))

        val resultFut: Future[
          Either[AccountUpdateException, TwilioAccount]
        ] =
          instance.run(connSettings, updateRequest)
        resultFut.map(result => assert(result === expected))
      }

    }
  }

  // noinspection TypeAnnotation
  final class Fixture extends CommonFixtures.Account {
    val updateRequest = AccountUpdateRequest(
      accountSid1,
      Some(TwilioAccount.Name("newFriendlyName")),
      Some(TwilioAccount.Status.Suspended)
    )

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(
        WireMock.urlPathEqualTo(
          "/2010-04-01/Accounts/ACf6c9aa4f2754c258aa45a6d2637cfa15.json"
        )
      )
      .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
      .andMatching(
        postParamMatcher(
          Map(
            "Sid"          -> accountSid1.twilioString,
            "Status"       -> "suspended",
            "FriendlyName" -> "newFriendlyName"
          )
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: AccountUpdateRequestExecutor =
      TwilioClient.defaultImpl().iam.accountUpdate
  }

  private def twilioResponse1 =
    """{
      |  "status": "suspended",
      |  "date_updated": "Wed, 23 Feb 2022 17:13:40 +0000",
      |  "auth_token": "AVerySecretValueThatShouldBeXXXX",
      |  "friendly_name": "newFriendlyName",
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
