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
        assert(apiKey.flagsOpt === None)
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
        assert(apiKey.flagsOpt === None)
      }
    }

    "correctly implement equals, hashCode and toString" in {
      val sid                     = ApiKey.Sid("SK123")
      val secret                  = ApiKey.Secret("secret")
      val name                    = ApiKey.FriendlyName("name")
      val flags: Set[ApiKey.Flag] = Set(ApiKey.Flag.Restricted)

      val key1 = ApiKey(sid, name).withSecret(secret)
      val key2 = ApiKey(sid, name).withSecret(secret)
      val key3 = ApiKey(sid, name).withSecret(secret).withFlags(flags)
      val key4 = ApiKey(sid, name).withSecret(secret).withFlags(flags)

      assert(key1 === key2)
      assert(key1.hashCode() === key2.hashCode())
      assert(
        key1.toString === s"ApiKey(sid=$sid, secretOpt=Some($secret), friendlyName=$name, flagsOpt=None)"
      )

      assert(key3 === key4)
      assert(key3.hashCode() === key4.hashCode())
      assert(
        key3.toString === s"ApiKey(sid=$sid, secretOpt=Some($secret), friendlyName=$name, flagsOpt=Some($flags))"
      )

      assert(key1 !== key3)
      assert(key1.hashCode() !== key3.hashCode())

      val keyNoSecret = ApiKey(sid, name)
      assert(keyNoSecret.secretOpt === None)
      assert(
        keyNoSecret.toString === s"ApiKey(sid=$sid, secretOpt=None, friendlyName=$name, flagsOpt=None)"
      )
    }
  }
}
