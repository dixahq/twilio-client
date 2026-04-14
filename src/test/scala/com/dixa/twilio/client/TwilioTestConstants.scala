// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client

import com.dixa.twilio.client.TwilioConnectionSettings.TwilioEndpoint
import com.dixa.twilio.model.{PublicEdgeLocation, Region}
import com.dixa.twilio.model.iam.{ApiKey, AuthToken, TwilioAccount}
import com.dixa.twilio.model.voice.Trunk

import java.time.Instant

object TwilioTestConstants {

  val createdTime: Instant = Instant.ofEpochSecond(1661836497) // 2022-08-30T05:14:57Z
  val updatedTime: Instant = Instant.ofEpochSecond(1662834697) // 2022-09-10T18:31:37Z

  val accountSid: TwilioAccount.Sid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
  val authToken: AuthToken.UnknownType = AuthToken.UnknownType("testPassword")

  val trunkUsername1AsString         = "TwilioTestConstants.trunkUsername1"
  val trunkUsername1: Trunk.Username = Trunk.Username(trunkUsername1AsString)
  val trunkPassword1AsString         = "TwilioTestConstants.trunkPassword1"
  val trunkPassword1: Trunk.Password = Trunk.Password(trunkPassword1AsString)

  val apiKeySid: ApiKey.Sid       = ApiKey.Sid("SKXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
  val apiKeySecret: ApiKey.Secret = ApiKey.Secret("testApiKeySecret")

  val authTokenCredentials: TwilioConnectionSettings.Credentials.AuthTokenCredentials =
    TwilioConnectionSettings.Credentials.AuthTokenCredentials(accountSid, authToken)

  val apiKeyCredentials: TwilioConnectionSettings.Credentials.ApiKeyCredentials =
    TwilioConnectionSettings.Credentials.ApiKeyCredentials(apiKeySid, apiKeySecret)

  def connSettings(port: Int): TwilioConnectionSettings = TwilioConnectionSettings(
    TwilioEndpoint(baseHostName = "localhost", port = port),
    region = Region.Us1,
    publicEdgeLocation = PublicEdgeLocation.Ashburn,
    protocol = TwilioConnectionSettings.Protocol.Http,
    credentials = authTokenCredentials,
    parallelFactor = TwilioConnectionSettings.ParallelFactor.halfCpuCores,
    timeouts = TwilioConnectionSettings.Timeouts.default
  )
}
