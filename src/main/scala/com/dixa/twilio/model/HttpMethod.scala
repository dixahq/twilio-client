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

package com.dixa.twilio.model

import scala.collection.immutable

sealed abstract class HttpMethod(override val toString: String)
    extends EnumWithTwilioString.EnumEntry {}

object HttpMethod extends EnumWithTwilioString[HttpMethod] {
  override val values: immutable.IndexedSeq[HttpMethod] = findValues

  case object Get  extends HttpMethod("GET")
  case object Post extends HttpMethod("POST")

  // Put and Delete are deliberately not here, as they seem to be not used at all in the Twilio API.
}
