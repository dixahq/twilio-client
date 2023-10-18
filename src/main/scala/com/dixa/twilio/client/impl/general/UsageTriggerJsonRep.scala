package com.dixa.twilio.client.impl.general

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.general.UsageTrigger
import com.dixa.twilio.model.iam.TwilioAccount

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
    currentValue = UsageTrigger.CurrentValue(current_value),
    dateCreated = Instant.from(Formatter.dateTime.parse(date_created)),
    dateFired = date_fired.map(string => Instant.from(Formatter.dateTime.parse(string))),
    dateUpdated = Instant.from(Formatter.dateTime.parse(date_updated)),
    friendlyName = UsageTrigger.FriendlyName(friendly_name),
    recurring = recurring.map(UsageTrigger.Recurring.fromTwilioStringUnsafe),
    sid = UsageTrigger.Sid.unsafe(sid),
    triggerBy = UsageTrigger.TriggerBy.fromTwilioStringUnsafe(trigger_by),
    triggerValue = UsageTrigger.TriggerValue(trigger_value),
    usageCategory = UsageTrigger.UsageCategory.fromTwilioStringUnsafe(usage_category)
  )
}

private[general] object UsageTriggerJsonRep {
  implicit val upickleReader: Reader[UsageTriggerJsonRep] =
    macroR[UsageTriggerJsonRep]
}
