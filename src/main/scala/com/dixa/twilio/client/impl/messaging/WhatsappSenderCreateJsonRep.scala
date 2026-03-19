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

package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroW, Writer}

private[messaging] final case class WhatsappSenderCreateJsonRep(
    sender_id: String,
    profile: WhatsappSenderCreateJsonRep.ProfileJsonRep,
    webhook: WhatsappSenderCreateJsonRep.WebhooksJsonRep,
    configuration: WhatsappSenderCreateJsonRep.ConfigurationJsonRep,
)

private[messaging] object WhatsappSenderCreateJsonRep {

  final case class WebhooksJsonRep(
      fallback_method: Option[String] = None,
      fallback_url: Option[String] = None,
      status_callback_url: Option[String] = None,
      status_callback_method: Option[String] = None,
      callback_method: Option[String] = None,
      callback_url: Option[String] = None
  )
  final case class ProfileJsonRep(about: Option[String] = None, name: String)

  final case class ConfigurationJsonRep(
      waba_id: Option[String] = None,
      verification_method: Option[String] = None
  )

  implicit val webhooksJsonRepWriter: Writer[WebhooksJsonRep]           = macroW[WebhooksJsonRep]
  implicit val profileJsonRepWriter: Writer[ProfileJsonRep]             = macroW[ProfileJsonRep]
  implicit val configurationJsonRepWriter: Writer[ConfigurationJsonRep] =
    macroW[ConfigurationJsonRep]
  implicit val whatsappSenderCreateJsonRepWriter: Writer[WhatsappSenderCreateJsonRep] =
    macroW[WhatsappSenderCreateJsonRep]
}
