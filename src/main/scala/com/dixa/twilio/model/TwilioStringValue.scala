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

package com.dixa.twilio.model

/** Abstraction over entities, that has a value that is useable as String values in the twilio.
  *
  * This can be all from enum entries with static values, to classes that wrap entity values like
  * names etc.
  *
  * Note that the single field of this trait; `twilioString`, default to using the value of
  * `toString`, as this is in most cases mix into classes just wrapping a `String`, and just
  * overriding `toString` for doing so. In these cases using `toString` as the value for
  * `twilioString` is what you want. However, in more advanced classes, you probably would need to
  * override `twilioString` manually.
  */
trait TwilioStringValue {
  def twilioString: String = toString

  def startsWith(prefix: String): Boolean = twilioString.startsWith(prefix)

  def contains(s: String): Boolean = twilioString.contains(s)
}
