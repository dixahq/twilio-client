package com.dixa.twilio.client

import com.dixa.twilio.client.TwilioConnectionSettings.TwilioEndpoint
import com.dixa.twilio.client.impl.ApiSubDomain
import com.dixa.twilio.client.model.iam.TwilioAccount
import org.scalatest.wordspec.AnyWordSpec

final class TwilioConnectionSettingsTest extends AnyWordSpec {

  s"${classOf[TwilioConnectionSettings].getSimpleName}" when {

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

  private def createInstance(baseHost: String): TwilioConnectionSettings = {
    val connSettings = TwilioTestConstants.connSettings(4353)
    connSettings.copy(endpoint =
      TwilioEndpoint(baseHostName = baseHost, port = connSettings.endpoint.port)
    )
  }
}
