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

import com.dixa.twilio.model.TwilioStringValue
import com.dixa.twilio.model.phonenumber.PhoneNumberE164

sealed abstract class MessageRecipient private[model] extends TwilioStringValue {
  def asString: String
  override def toString: String = asString
}

object MessageRecipient {
  def fromString(s: String): Option[MessageRecipient] = {
    PhoneNumberE164(s)
      .map(E164)
      .orElse(WhatsappPhoneNumber(s).map(WhatsappNumber))
      .orElse(WhatsappExternalUserId(s).map(WhatsappId))
  }

  def fromStringUnsafe(string: String): MessageRecipient = {
    fromString(string).getOrElse(
      throw new IllegalArgumentException(
        "Recipient couldn't be parsed into Whatsapp number, Whatsapp external user ID or into E.164 phone number"
      )
    )
  }

  final case class E164(phoneNumber: PhoneNumberE164) extends MessageRecipient {
    override def asString: String = phoneNumber.toString
  }

  final case class WhatsappNumber(whatsappNumber: WhatsappPhoneNumber) extends MessageRecipient {
    override def asString: String = whatsappNumber.toString
  }

  final case class WhatsappId(externalUserId: WhatsappExternalUserId) extends MessageRecipient {
    override def asString: String = externalUserId.toString
  }
}
