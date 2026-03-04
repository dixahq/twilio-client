package com.dixa.twilio.client.twilioClient.iam

import com.dixa.twilio.client.iam.KeyCreateRequestExecutor
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.{ApiKey, TwilioAccount}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class KeyCreateTest extends TwilioClientTest {
  "KeyCreateRequestExecutor" should {
    "parse flags correctly, skipping unknown values and handling empty/null/missing cases" in {
      val accountSid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
      val request    = KeyCreateRequestExecutor.KeyCreateRequest.build(
        _.withAccountSid(accountSid)
          .withFriendlyName(ApiKey.FriendlyName("Test Key"))
          .build()
      )

      val responseBody =
        s"""{
           |  "sid": "SKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
           |  "secret": "your_secret_here",
           |  "friendly_name": "Test Key",
           |  "flags": ["restricted", "completely_unknown_value_from_twilio", "rest_api"]
           |}""".stripMargin

      wireMockServer.stubFor(
        WireMock
          .post(WireMock.urlPathEqualTo("/v1/Keys"))
          .willReturn(
            aResponse()
              .withStatus(201)
              .withHeader("Content-Type", "application/json")
              .withBody(responseBody)
          )
      )

      val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
      val instance: KeyCreateRequestExecutor = TwilioClient.defaultImpl().iam.keyCreate

      val resultFut: Future[Either[KeyCreateRequestExecutor.KeyCreateException, ApiKey]] =
        instance.run(connSettings, request)

      resultFut.map { result =>
        val apiKey = result.getOrElse(fail(s"Expected success, got $result"))
        assert(apiKey.sid.value === "SKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
        assert(apiKey.friendlyName.twilioString === "Test Key")
        // secret is redacted in toString, but we check if it's there
        assert(apiKey.secret.value === "your_secret_here")
        assert(apiKey.flags === Set(ApiKey.Flag.Restricted, ApiKey.Flag.RestApi))
        assert(!apiKey.flags.exists(_.twilioString == "completely_unknown_value_from_twilio"))
      }
    }

    "handle missing flags by returning an empty set" in {
      val accountSid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
      val request    = KeyCreateRequestExecutor.KeyCreateRequest.build(
        _.withAccountSid(accountSid)
          .build()
      )

      val responseBody =
        s"""{
           |  "sid": "SKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
           |  "secret": "your_secret_here",
           |  "friendly_name": "Test Key"
           |}""".stripMargin

      wireMockServer.stubFor(
        WireMock
          .post(WireMock.urlPathEqualTo("/v1/Keys"))
          .willReturn(
            aResponse()
              .withStatus(201)
              .withHeader("Content-Type", "application/json")
              .withBody(responseBody)
          )
      )

      val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
      val instance: KeyCreateRequestExecutor = TwilioClient.defaultImpl().iam.keyCreate

      val resultFut: Future[Either[KeyCreateRequestExecutor.KeyCreateException, ApiKey]] =
        instance.run(connSettings, request)

      resultFut.map { result =>
        val apiKey = result.getOrElse(fail(s"Expected success, got $result"))
        assert(apiKey.flags === Set.empty)
      }
    }

    "handle null flags by returning an empty set" in {
      val accountSid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
      val request    = KeyCreateRequestExecutor.KeyCreateRequest.build(
        _.withAccountSid(accountSid)
          .build()
      )

      val responseBody =
        s"""{
           |  "sid": "SKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
           |  "secret": "your_secret_here",
           |  "friendly_name": "Test Key",
           |  "flags": null
           |}""".stripMargin

      wireMockServer.stubFor(
        WireMock
          .post(WireMock.urlPathEqualTo("/v1/Keys"))
          .willReturn(
            aResponse()
              .withStatus(201)
              .withHeader("Content-Type", "application/json")
              .withBody(responseBody)
          )
      )

      val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
      val instance: KeyCreateRequestExecutor = TwilioClient.defaultImpl().iam.keyCreate

      val resultFut: Future[Either[KeyCreateRequestExecutor.KeyCreateException, ApiKey]] =
        instance.run(connSettings, request)

      resultFut.map { result =>
        val apiKey = result.getOrElse(fail(s"Expected success, got $result"))
        assert(apiKey.flags === Set.empty)
      }
    }
  }
}
