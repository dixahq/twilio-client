package com.dixa.twilio.client

import com.dixa.twilio.client.implDetails.ApiSubDomain
import com.dixa.twilio.client.model.iam.TwilioAccount
import org.scalatest.wordspec.AnyWordSpec

final class TwilioConnectionSettingsTest extends AnyWordSpec {

  s"${classOf[TwilioConnectionSettings].getSimpleName}" when {

    "factory for production settings is called" should {

      "build settings with production settings, and use default values for parallel " +
        "factor and timeouts" in {
          val sid    = TwilioAccount.Sid("TestSid")
          val token  = TwilioAccount.AuthToken("TestToken")
          val result = TwilioConnectionSettings.forProduction(sid, token)
          val expected = TwilioConnectionSettings(
            "twilio.com",
            443,
            TwilioConnectionSettings.Protocol.Https,
            sid,
            token,
            TwilioConnectionSettings.ParallelFactor.halfCpuCores,
            TwilioConnectionSettings.Timeouts.default
          )
          assert(result === expected)
        }
    }

    "ask to build a hostnaem for a subdomain" should {

      "work with the Api sub domain" in {
        val instance = createInstance("twilio.com")
        val result   = instance.hostNameFor(ApiSubDomain.Api)
        val expected = "api.twilio.com"
        assert(result === expected)
      }

      "work with the Messagin sub domain" in {
        val instance = createInstance("twilio.com")
        val result   = instance.hostNameFor(ApiSubDomain.Messaging)
        val expected = "messaging.twilio.com"
        assert(result === expected)
      }

      "Not append any subdomain, if the base host name is localhost" in {
        val instance = createInstance("localhost")
        val result   = instance.hostNameFor(ApiSubDomain.Api)
        val expected = "localhost"
        assert(result === expected)
      }

      "Not append any subdomain, if the base host name is 127.0.0.1" in {
        val instance = createInstance("127.0.0.1")
        val result   = instance.hostNameFor(ApiSubDomain.Api)
        val expected = "127.0.0.1"
        assert(result === expected)
      }
    }
  }

  private def createInstance(baseHost: String) =
    TwilioTestConstants.connSettings(4353).copy(baseHostName = baseHost)
}
