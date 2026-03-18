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

package com.dixa.twilio.client

import com.dixa.twilio.client.TwilioConnectionSettings.TwilioEndpoint
import com.dixa.twilio.client.impl.ApiSubDomain
import com.dixa.twilio.model.{PublicEdgeLocation, Region}
import org.scalatest.wordspec.AnyWordSpec

final class TwilioConnectionSettingsTest extends AnyWordSpec {

  s"${classOf[TwilioConnectionSettings].getSimpleName}" when {

    "TwilioEndpoints are constructed" should {

      "return the reused default object, if clients try to manually create a new instance with default values" in {
        val result = TwilioEndpoint("twilio.com", 443)
        assert(result eq TwilioEndpoint.default)
        assert(result.baseHostName == "twilio.com")
        assert(result.port == 443)
      }

      "being able to return a instance with custom settings" in {
        val result = TwilioEndpoint("localhost", 4355)
        assert(result.baseHostName == "localhost")
        assert(result.port == 4355)
      }
    }

    "ask to build a hostname for a subdomain" should {

      "work with the Api sub domain" in {
        val instance = createInstance(
          "twilio.com",
          region = Region.Us1,
          edgeLocation = PublicEdgeLocation.Ashburn
        )
        val result   = instance.hostNameFor(ApiSubDomain.Api)
        val expected = "api.ashburn.us1.twilio.com"
        assert(result === expected)
      }

      "work with the Messagin sub domain" in {
        val instance = createInstance(
          "twilio.com",
          region = Region.Ireland1,
          edgeLocation = PublicEdgeLocation.Frankfurt
        )
        val result   = instance.hostNameFor(ApiSubDomain.Messaging)
        val expected = "messaging.frankfurt.ie1.twilio.com"
        assert(result === expected)
      }

      "work with the Iam sub domain" in {
        val instance = createInstance(
          "twilio.com",
          region = Region.Ireland1,
          edgeLocation = PublicEdgeLocation.Dublin
        )
        val result   = instance.hostNameFor(ApiSubDomain.Iam)
        val expected = "iam.dublin.ie1.twilio.com"
        assert(result === expected)
      }

      "Not append any subdomain, if the base host name is localhost" in {
        val instance = createInstance(
          "localhost",
          region = Region.Australia1,
          edgeLocation = PublicEdgeLocation.Tokyo
        )
        val result   = instance.hostNameFor(ApiSubDomain.Api)
        val expected = "localhost"
        assert(result === expected)
      }

      "Not append any subdomain, if the base host name is 127.0.0.1" in {
        val instance = createInstance(
          "127.0.0.1",
          region = Region.Australia1,
          edgeLocation = PublicEdgeLocation.Dublin
        )
        val result   = instance.hostNameFor(ApiSubDomain.Api)
        val expected = "127.0.0.1"
        assert(result === expected)
      }
    }
  }

  private def createInstance(
      baseHost: String,
      region: Region,
      edgeLocation: PublicEdgeLocation
  ): TwilioConnectionSettings = {
    val connSettings = TwilioTestConstants.connSettings(4353)
    connSettings.copy(
      endpoint = TwilioEndpoint(baseHostName = baseHost, port = connSettings.endpoint.port),
      region = region,
      publicEdgeLocation = edgeLocation
    )
  }
}
