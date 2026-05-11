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

package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.model.messaging.ChannelSender.Webhooks
import com.dixa.twilio.model.messaging.MessageSender.Whatsapp
import com.dixa.twilio.model.messaging.{ChannelSender, WhatsappPhoneNumber}

trait ChannelSenderTestSharedFixture {
  def channelSenderSid: ChannelSender.Sid = ChannelSenderTestSharedFixture.channelSenderSid
  def whatsappChannelSender: ChannelSender.WhatsappSender =
    ChannelSenderTestSharedFixture.channelSender
}

object ChannelSenderTestSharedFixture {
  val channelSenderSid: ChannelSender.Sid =
    ChannelSender.Sid.unsafe("XEcfd04c72e3397a53e24bd6c7408aff83")
  val channelSender: ChannelSender.WhatsappSender = ChannelSender.WhatsappSender(
    status = ChannelSender.Status.Online,
    profile = ChannelSender.Profile
      .WhatsappProfile(phoneNumberDisplayName = "Example WABA"),
    senderId = Whatsapp(WhatsappPhoneNumber.unsafe("whatsapp:+4552511283")),
    sid = channelSenderSid,
    webhooks = Webhooks(
      callback = None,
      fallback = None,
      statusCallback = None
    ),
    configuration = ChannelSender.Configuration(wabaId = Some("316806161514452")),
    properties = Some(
      ChannelSender.Properties.WhatsappProperties(
        messagingLimit = None,
        qualityRating = ChannelSender.QualityRating.Unknown
      )
    )
  )
}
