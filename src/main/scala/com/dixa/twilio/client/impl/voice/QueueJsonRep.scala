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

package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Queue

import java.time.{Duration, Instant}
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

/** Json representation of a Call */
private[voice] case class QueueJsonRep(
    account_sid: String,
    average_wait_time: Int,
    current_size: Int,
    date_created: String,
    date_updated: String,
    friendly_name: String,
    max_size: Int,
    sid: String
) {

  def toModel: Queue = Queue(
    Queue.Sid.unsafe(sid),
    Queue.FriendlyName(friendly_name),
    TwilioAccount.Sid.unsafe(account_sid),
    Queue.CurrentSize(current_size),
    Queue.MaxSize(max_size),
    Duration.ofSeconds(average_wait_time),
    Instant.from(Formatter.dateTime.parse(date_created)),
    Instant.from(Formatter.dateTime.parse(date_updated))
  )
}

private[voice] object QueueJsonRep {

  implicit val queueJsonRepReader: Reader[QueueJsonRep] = macroR[QueueJsonRep]
}
