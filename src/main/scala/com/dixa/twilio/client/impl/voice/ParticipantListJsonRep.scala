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

package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

private[voice] final case class ParticipantListJsonRep(
    first_page_uri: String,
    end: Int,
    participants: List[ParticipantJsonRep],
    previous_page_uri: Option[String] = None,
    uri: String,
    page_size: Int,
    start: Int,
    next_page_uri: Option[String] = None,
    page: Int
)

private[voice] object ParticipantListJsonRep {

  implicit val participantListJsonRepReader: Reader[ParticipantListJsonRep] =
    macroR[ParticipantListJsonRep]
}
