package com.dixa.twilio.client.twilioClient.iam

import com.dixa.twilio.client.iam.ApiKeyCreateRequestExecutor
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.{ApiKey, ApiKeyPolicy, TwilioAccount}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class ApiKeyCreateTest extends TwilioClientTest {

  private val friendlyName = ApiKey.FriendlyName("Test Key")

  "ApiKeyCreateRequestExecutor" should {
    "parse flags correctly, skipping unknown values and handling empty/null/missing cases" in {
      val accountSid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
      val request    = ApiKeyCreateRequestExecutor.KeyCreateRequest.build(
        _.withAccountSid(accountSid)
          .withFriendlyName(friendlyName)
          .withTypeStandard()
          .build()
      )

      val responseBody =
        s"""{
           |  "sid": "SKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
           |  "secret": "your_secret_here",
           |  "friendly_name": "Test Key",
           |  "date_created": "Thu, 24 Aug 2023 14:00:00 +0000",
           |  "date_updated": "Thu, 24 Aug 2023 14:00:00 +0000",
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
      val instance: ApiKeyCreateRequestExecutor = TwilioClient.defaultImpl().iam.apiKeyCreate

      val resultFut: Future[Either[ApiKeyCreateRequestExecutor.KeyCreateException, ApiKey]] =
        instance.run(connSettings, request)

      resultFut.map { result =>
        val apiKey = result.getOrElse(fail(s"Expected success, got $result"))
        assert(apiKey.sid.value === "SKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
        assert(apiKey.friendlyName.twilioString === "Test Key")
        // secret is redacted in toString, but we check if it's there
        apiKey match {
          case withSecret: ApiKey.HasSecret =>
            assert(withSecret.secret.value === "your_secret_here")
          case _ => fail("Expected ApiKey to have secret")
        }

        apiKey match {
          case withFlags: ApiKey.HasFlags =>
            assert(withFlags.flags === Set(ApiKey.Flag.Restricted, ApiKey.Flag.RestApi))
            assert(
              !withFlags.flags.exists(_.twilioString == "completely_unknown_value_from_twilio")
            )
          case _ => fail("Expected ApiKey to have flags")
        }
      }
    }

    "parse policy_allow correctly" in {
      val accountSid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
      val request    = ApiKeyCreateRequestExecutor.KeyCreateRequest.build(
        _.withAccountSid(accountSid)
          .withTypeRestricted()
          .withPolicy(Set(ApiKeyPolicy.ConferencesRead))
          .build()
      )

      val responseBody =
        s"""{
           |  "sid": "SKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
           |  "secret": "your_secret_here",
           |  "friendly_name": "Test Key",
           |  "date_created": "Thu, 24 Aug 2023 14:00:00 +0000",
           |  "date_updated": "Thu, 24 Aug 2023 14:00:00 +0000",
           |  "policy_allow": ["/twilio/voice/conferences/read", "unknown_policy"]
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
      val instance: ApiKeyCreateRequestExecutor = TwilioClient.defaultImpl().iam.apiKeyCreate

      val resultFut: Future[Either[ApiKeyCreateRequestExecutor.KeyCreateException, ApiKey]] =
        instance.run(connSettings, request)

      resultFut.map { result =>
        val apiKey = result.getOrElse(fail(s"Expected success, got $result"))
        apiKey match {
          case withPolicy: ApiKey.HasPolicyAllow =>
            assert(withPolicy.policyAllow === Set(ApiKeyPolicy.ConferencesRead))
          case _ => fail("Expected ApiKey to have policyAllow")
        }
      }
    }

    "handle missing flags by returning an empty set" in {
      val accountSid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
      val request    = ApiKeyCreateRequestExecutor.KeyCreateRequest.build(
        _.withAccountSid(accountSid)
          .withTypeStandard()
          .build()
      )

      val responseBody =
        s"""{
           |  "sid": "SKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
           |  "secret": "your_secret_here",
           |  "friendly_name": "Test Key",
           |  "date_created": "Thu, 24 Aug 2023 14:00:00 +0000",
           |  "date_updated": "Thu, 24 Aug 2023 14:00:00 +0000"
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
      val instance: ApiKeyCreateRequestExecutor = TwilioClient.defaultImpl().iam.apiKeyCreate

      val resultFut: Future[Either[ApiKeyCreateRequestExecutor.KeyCreateException, ApiKey]] =
        instance.run(connSettings, request)

      resultFut.map { result =>
        val apiKey = result.getOrElse(fail(s"Expected success, got $result"))
        assert(apiKey.flagsOpt === None)
      }
    }

    "handle null flags by returning an empty set" in {
      val accountSid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
      val request    = ApiKeyCreateRequestExecutor.KeyCreateRequest.build(
        _.withAccountSid(accountSid)
          .withTypeStandard()
          .build()
      )

      val responseBody =
        s"""{
           |  "sid": "SKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
           |  "secret": "your_secret_here",
           |  "friendly_name": "Test Key",
           |  "date_created": "Thu, 24 Aug 2023 14:00:00 +0000",
           |  "date_updated": "Thu, 24 Aug 2023 14:00:00 +0000",
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
      val instance: ApiKeyCreateRequestExecutor = TwilioClient.defaultImpl().iam.apiKeyCreate

      val resultFut: Future[Either[ApiKeyCreateRequestExecutor.KeyCreateException, ApiKey]] =
        instance.run(connSettings, request)

      resultFut.map { result =>
        val apiKey = result.getOrElse(fail(s"Expected success, got $result"))
        assert(apiKey.flagsOpt === None)
      }
    }

    "send Policy as JSON when using withRestrictedType and withPolicy" in {
      val accountSid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
      val request    = ApiKeyCreateRequestExecutor.KeyCreateRequest.build(
        _.withAccountSid(accountSid)
          .withTypeRestricted()
          .withPolicy(Set(ApiKeyPolicy.ConferencesRead))
          .build()
      )

      val responseBody =
        s"""{
           |  "sid": "SKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
           |  "secret": "your_secret_here",
           |  "friendly_name": "Restricted Key",
           |  "date_created": "Thu, 24 Aug 2023 14:00:00 +0000",
           |  "date_updated": "Thu, 24 Aug 2023 14:00:00 +0000",
           |  "policy_allow": ["/twilio/voice/conferences/read"]
           |}""".stripMargin

      wireMockServer.stubFor(
        WireMock
          .post(WireMock.urlPathEqualTo("/v1/Keys"))
          .withRequestBody(WireMock.containing("KeyType=restricted"))
          .withRequestBody(
            WireMock.containing(
              "Policy=%7B%22allow%22%3A%5B%22%2Ftwilio%2Fvoice%2Fconferences%2Fread%22%5D%7D"
            )
          )
          .willReturn(
            aResponse()
              .withStatus(201)
              .withHeader("Content-Type", "application/json")
              .withBody(responseBody)
          )
      )

      val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
      val instance: ApiKeyCreateRequestExecutor = TwilioClient.defaultImpl().iam.apiKeyCreate

      val resultFut: Future[Either[ApiKeyCreateRequestExecutor.KeyCreateException, ApiKey]] =
        instance.run(connSettings, request)

      resultFut.map { result =>
        val apiKey = result.getOrElse(fail(s"Expected success, got $result"))
        apiKey match {
          case withPolicy: ApiKey.HasPolicyAllow =>
            assert(withPolicy.policyAllow === Set(ApiKeyPolicy.ConferencesRead))
          case _ => fail("Expected ApiKey to have policyAllow")
        }
      }
    }

    "dont allow to build the request, if no key type is specified" in {
      assertDoesNotCompile("""ApiKeyCreateRequestExecutor.KeyCreateRequest.build(
                             |        _.withAccountSid(TwilioTestConstants.accountSid)
                             |          .withFriendlyName(friendlyName)
                             |          .build()
                             |      )
                             |""".stripMargin)
      succeed
    }

    "dont allow a policy to be set, if the key type is standard" in {
      assertDoesNotCompile("""ApiKeyCreateRequestExecutor.KeyCreateRequest.build(
                             |        _.withAccountSid(TwilioTestConstants.accountSid)
                             |          .withFriendlyName(friendlyName)
                             |          .withTypeStandard()
                             |          .withPolicy(Set(ApiKeyPolicy.IamApiKeysList))
                             |          .build()
                             |      )
                             |""".stripMargin)
      succeed
    }

    "dont allow to build  request if type is restricted but no policy is set" in {
      assertDoesNotCompile("""ApiKeyCreateRequestExecutor.KeyCreateRequest.build(
                             |        _.withAccountSid(TwilioTestConstants.accountSid)
                             |          .withFriendlyName(friendlyName)
                             |          .withTypeRestricted()
                             |          .build()
                             |      )
                             |""".stripMargin)
      succeed
    }

    "Dont mind in what order you specify the policy and the restricted type" in {
      ApiKeyCreateRequestExecutor.KeyCreateRequest.build(
        _.withAccountSid(TwilioTestConstants.accountSid)
          .withFriendlyName(friendlyName)
          .withPolicy(Set(ApiKeyPolicy.IamApiKeysList))
          .withTypeRestricted()
          .build()
      )
      ApiKeyCreateRequestExecutor.KeyCreateRequest.build(
        _.withAccountSid(TwilioTestConstants.accountSid)
          .withFriendlyName(friendlyName)
          .withTypeRestricted()
          .withPolicy(Set(ApiKeyPolicy.IamApiKeysList))
          .build()
      )
      succeed
    }
  }
}
