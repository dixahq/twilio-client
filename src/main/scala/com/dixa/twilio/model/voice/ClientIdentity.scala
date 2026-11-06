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

/** The identity of a Twilio client, such as a Voice SDK app.
  *
  * The identity is the one in the access token that the client registered with. Twilio dials the
  * client with the Client noun, and shows the client as `client:<identity>` as a caller ID, in the
  * From of a webhook, and in the To and From of a call. Twilio allows up to 256 chars.
  *
  * @see
  *   https://www.twilio.com/docs/voice/twiml/client
  */
final case class ClientIdentity private (override val toString: String)
    extends ConstrainedString
    with TwilioStringValue

object ClientIdentity extends ConstrainedString.ConstrainedStringCompanionObject[ClientIdentity] {
  override protected def constructInstance(wrapped: String): ClientIdentity =
    new ClientIdentity(wrapped)

  override protected val maxLength: Option[Int] = Some(256)
}
