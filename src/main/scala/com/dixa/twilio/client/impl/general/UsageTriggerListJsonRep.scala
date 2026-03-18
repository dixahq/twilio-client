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

package com.dixa.twilio.client.impl.general

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

private[general] final case class UsageTriggerListJsonRep(
    first_page_uri: String,
    usage_triggers: List[UsageTriggerJsonRep],
    previous_page_uri: Option[String] = None,
    uri: String,
    page_size: Int,
    next_page_uri: Option[String] = None,
    page: Int
)

private[general] object UsageTriggerListJsonRep {

  implicit val usageTriggerListJsonRepReader: Reader[UsageTriggerListJsonRep] =
    macroR[UsageTriggerListJsonRep]
}
