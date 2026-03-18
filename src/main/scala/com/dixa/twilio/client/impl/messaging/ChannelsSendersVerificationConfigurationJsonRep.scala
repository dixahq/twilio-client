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

import com.dixa.twilio.client.impl.messaging.ChannelsSendersVerificationConfigurationJsonRep.ConfigurationJsonRep
import upickle.default.{macroW, Writer}

private[messaging] final case class ChannelsSendersVerificationConfigurationJsonRep(
    configuration: ConfigurationJsonRep,
)

private[messaging] object ChannelsSendersVerificationConfigurationJsonRep {

  final case class ConfigurationJsonRep(verification_code: String)

  final case class PropertiesJsonRep(
      quality_rating: Option[String],
      messaging_limit: Option[String],
  )

  implicit val configurationJsonRepReader: Writer[ConfigurationJsonRep] =
    macroW[ConfigurationJsonRep]
  implicit val channelSenderJsonRepReader: Writer[ChannelsSendersVerificationConfigurationJsonRep] =
    macroW[ChannelsSendersVerificationConfigurationJsonRep]
}
