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

package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.impl.ApiSubDomain
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging.{Media, MediaResourceUrl, Message}

private[client] object MediaResourceUrlFactory {

  private[client] def buildMediaResourcePath(
      accountSid: TwilioAccount.Sid,
      messageSid: Message.Sid
  ): String = {
    buildMediaResourceBasePath(accountSid, messageSid) + "/Media.json"
  }

  private def buildMediaResourceBasePath(
      accountSid: TwilioAccount.Sid,
      messageSid: Message.Sid
  ): String = {
    s"/2010-04-01/Accounts/$accountSid/Messages/$messageSid"
  }

  // Constructs the final url that contains the media resource,
  // This gives the power to clients to fetch the resources, without need of
  // using Twilio's basic auth, since it's publicly available.
  private[messaging] def resourceUrl(
      accountSid: TwilioAccount.Sid,
      messageSid: Message.Sid,
      sid: Media.Sid,
      twilioConnSettings: TwilioConnectionSettings
  ): MediaResourceUrl = {
    val basePath: String = buildMediaResourceBasePath(accountSid, messageSid)
    MediaResourceUrl(
      s"${twilioConnSettings.protocol}://${twilioConnSettings.hostNameFor(ApiSubDomain.Api)}$basePath/Media/$sid"
    )
  }
}
