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

package com.dixa.twilio.client.callback

import com.dixa.twilio.client.impl.callback.RequestValidatorImpl
import com.dixa.twilio.model.iam.AuthToken

trait RequestValidator {

  import RequestValidator._

  /** Validates that the request payload truly comes from twilio, by encrypting the payload (url +
    * params) with hmac-sha1 algorithm using the twilio account auth token for signing key. Finally
    * compares the result with the `X-Twilio-Signature` header.
    *
    * @see
    *   https://www.twilio.com/docs/usage/security#validating-requests
    */
  def validate(
      requestUrl: String,
      authToken: AuthToken,
      params: Map[String, String],
      xTwilioSignature: XTwilioSignature
  ): ValidationRequestStatus

}

object RequestValidator {

  def defaultImpl(): RequestValidator = new RequestValidatorImpl()

  final case class XTwilioSignature(override val toString: String)

  sealed trait ValidationRequestStatus
  object ValidationStatus {
    case object Valid   extends ValidationRequestStatus
    case object Invalid extends ValidationRequestStatus
  }

}
