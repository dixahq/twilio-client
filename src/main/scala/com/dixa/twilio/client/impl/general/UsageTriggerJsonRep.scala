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

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.general.UsageTrigger
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.client.impl.JsonParsingUtil.emptyStringToNone
import java.time.Instant

/** Json representation of a UsageTrigger */
private[impl] case class UsageTriggerJsonRep(
    usage_record_uri: String,
    date_updated: String,
    date_fired: Option[String],
    friendly_name: String,
    uri: String,
    account_sid: String,
    callback_method: String,
    trigger_by: String,
    sid: String,
    current_value: String,
    date_created: String,
    callback_url: String,
    recurring: Option[String],
    usage_category: String,
    trigger_value: String
) {

  def toModel: UsageTrigger = UsageTrigger(
    accountSid = TwilioAccount.Sid.unsafe(account_sid),
    callBackMethod = HttpMethod.fromTwilioStringUnsafe(callback_method),
    callbackUrl = CallbackUrl.UsageTriggerUrl(callback_url),
    currentValue = UsageTrigger.CurrentValue.unsafe(current_value),
    dateCreated = Instant.from(Formatter.dateTime.parse(date_created)),
    dateFired = date_fired.map(string => Instant.from(Formatter.dateTime.parse(string))),
    dateUpdated = Instant.from(Formatter.dateTime.parse(date_updated)),
    friendlyName = UsageTrigger.FriendlyName.unsafe(friendly_name),
    recurring = emptyStringToNone(recurring).map(UsageTrigger.Recurring.fromTwilioStringUnsafe),
    sid = UsageTrigger.Sid.unsafe(sid),
    triggerBy = UsageTrigger.TriggerBy.fromTwilioStringUnsafe(trigger_by),
    triggerValue = UsageTrigger.TriggerValue.unsafe(trigger_value),
    usageCategory = UsageTrigger.UsageCategory.fromTwilioStringUnsafe(usage_category)
  )
}

private[general] object UsageTriggerJsonRep {
  implicit val upickleReader: Reader[UsageTriggerJsonRep] =
    macroR[UsageTriggerJsonRep]
}
