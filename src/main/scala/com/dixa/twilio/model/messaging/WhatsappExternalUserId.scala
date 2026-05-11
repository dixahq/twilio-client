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

package com.dixa.twilio.model.messaging

import scala.util.Try

sealed trait WhatsappExternalUserId {
  def asString: String

  override final val toString = asString
}

object WhatsappExternalUserId {
  def unsafe(asString: String): WhatsappExternalUserId = {
    require(
      asString.startsWith(WhatsappParticipant.Prefix),
      s"$asString is not a valid WhatsApp number, must start with '${WhatsappParticipant.Prefix}'"
    )
    require(
      verifyPattern.matcher(asString.stripPrefix(WhatsappParticipant.Prefix)).matches(),
      s"$asString is not a valid WhatsApp external user ID"
    )
    DefaultImpl(asString)
  }

  def apply(asString: String): Option[WhatsappExternalUserId] = Try {
    unsafe(asString)
  }.toOption

  private val verifyPattern = """^[A-Z]{2}\.[A-Za-z0-9]{1,128}$""".r.pattern

  private final case class DefaultImpl(asString: String) extends WhatsappExternalUserId
}
