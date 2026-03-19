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

package com.dixa.twilio.client.impl

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

/** Json representation of the standard error entity that Twilio will send back on none 200
  * responses.
  *
  * More details can be found here: https://www.twilio.com/docs/api/errors
  */
private[client] final case class DefaultApiErrorEntityJsonRep(
    code: Long,
    message: String,
    more_info: String,
    status: Int
) {

  override def toString: String =
    s"Code: $code, Message: $message, More info: $more_info, Status: $status"

}

private[client] object DefaultApiErrorEntityJsonRep {

  private[client] implicit val upickleRW: Reader[DefaultApiErrorEntityJsonRep] =
    macroR[DefaultApiErrorEntityJsonRep]
}
