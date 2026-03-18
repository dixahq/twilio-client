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

/** Encapsulate an account sid and a auth token in one type.
  *
  * This is not representing a Twilio entity as such, but is a combination often used in system to
  * represent credentials for accessing Twilio.
  *
  * @tparam A
  *   Type of AuthToken to encapsulate. If you don't care what type it is, then set this value to
  *   the base type of `AuthToken`
  */
final case class AccountSidAndAuthToken[+A <: AuthToken](
    accountSid: TwilioAccount.Sid,
    authToken: A
)
