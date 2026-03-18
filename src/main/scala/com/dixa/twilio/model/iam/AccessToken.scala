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

package com.dixa.twilio.model.iam

import com.dixa.twilio.model.TwilioStringValue

/** A short-lived JSON Web Token (JWT) used to authenticate Twilio client-side SDKs. It encodes the
  * target Twilio account, region, user identity, and a set of grants that define which Twilio
  * products and actions the token holder is permitted to use. Tokens have a maximum lifetime of 24
  * hours and must be signed with a Twilio API Key. More info:
  * https://www.twilio.com/docs/iam/access-tokens
  */
final case class AccessToken(token: String) extends TwilioStringValue {
  override def twilioString: String = token
}
