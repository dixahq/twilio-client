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

package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.TwilioStringValue
import com.dixa.twilio.model.phonenumber.PhoneNumberE164

abstract class MessageRecipient private[model] extends TwilioStringValue {

  def toMessageRecipient: String = toString
}

object MessageRecipient {
  def fromString(string: String): Option[MessageRecipient] = {
    PhoneNumberE164(string)
      .orElse(WhatsappNumber(string))
  }

  def fromStringUnsafe(string: String): MessageRecipient = {
    fromString(string).getOrElse(
      throw new IllegalArgumentException(
        "Recipient couldn't be parsed neither into Whatsapp nor into E.164 phone number"
      )
    )
  }
}
