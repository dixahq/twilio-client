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

package com.dixa.twilio.client.impl

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

/** Json representation of Twilio internal error (500) responses.
  *
  * These errors may have varying structure, so all fields are optional. More details:
  * https://www.twilio.com/docs/api/errors/20500
  */
private[client] final case class TwilioInternalErrorJsonRep(
    code: Option[Long],
    message: Option[String],
    more_info: Option[String],
    status: Option[Int]
)

private[client] object TwilioInternalErrorJsonRep {

  private[client] implicit val upickleRW: Reader[TwilioInternalErrorJsonRep] =
    macroR[TwilioInternalErrorJsonRep]
}
