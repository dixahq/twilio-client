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

package com.dixa.twilio.model.voice

import com.dixa.twilio.model.{ConstrainedString, TwilioStringValue}

/** The caller ID of an outgoing call to a SIP endpoint, as a text that does not need to be a phone
  * number.
  *
  * Twilio writes the text as the username of the From header that the SIP endpoint receives. Twilio
  * accepts letters, digits and the chars `+`, `-`, `_` and `.`, and no spaces. Twilio uses it for
  * the callerId of a Dial verb that calls a Sip noun, and for the From of a call created over REST
  * to a SIP address.
  *
  * @see
  *   https://www.twilio.com/docs/voice/twiml/dial
  * @see
  *   https://www.twilio.com/docs/voice/api/sip-making-calls
  */
final case class AlphanumericCallerId private (override val toString: String)
    extends ConstrainedString
    with TwilioStringValue

object AlphanumericCallerId
    extends ConstrainedString.ConstrainedStringCompanionObject[AlphanumericCallerId] {
  override protected def constructInstance(wrapped: String): AlphanumericCallerId =
    new AlphanumericCallerId(wrapped)

  override protected val validChars: Set[Char] =
    (('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9') ++ "+-_.").toSet
}
