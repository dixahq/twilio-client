package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.model.Iso4127CountryCode
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.TwilioPhoneNumber
import com.dixa.twilio.model.voice.{Call, Group, Trunk}
import com.dixa.twilio.model.voice.Call.FormattedPhoneNumber

import java.time.Instant

/** Json representation of a Call */
private[impl] case class CallJsonRep(
    account_sid: String,
    answered_by: Option[String],
    api_version: String,
    caller_name: Option[String],
    date_created: String,
    date_updated: String,
    direction: String,
    duration: Option[String],
    end_time: Option[String],
    forwarded_from: Option[String],
    from: String,
    from_formatted: String,
    group_sid: Option[String],
    parent_call_sid: Option[String],
    phone_number_sid: String,
    price: Option[String],
    price_unit: String,
    sid: String,
    start_time: Option[String],
    status: String,
    to: String,
    to_formatted: String,
    trunk_sid: Option[String],
    queue_time: String,
) {

  def toModel: Call = Call(
    sid = Call.Sid.unsafe(sid),
    dateCreated = Instant.from(Formatter.dateTime.parse(date_created)),
    dateUpdate = Instant.from(Formatter.dateTime.parse(date_updated)),
    parentCallSid = parent_call_sid.map(Call.Sid.unsafe),
    accountSid = TwilioAccount.Sid.unsafe(account_sid),
    to = Call.CallerId(to),
    toFormatted = FormattedPhoneNumber.apply(to_formatted),
    from = Call.CallerId(from),
    fromFormatted = FormattedPhoneNumber.apply(from_formatted),
    phoneNumberSid = TwilioPhoneNumber.Sid.unsafe(phone_number_sid),
    status = Call.Status.fromTwilioStringUnsafe(status),
    startTime = start_time.map(time => Instant.from(Formatter.dateTime.parse(time))),
    endTime = end_time.map(time => Instant.from(Formatter.dateTime.parse(time))),
    duration = duration.map(Call.Duration),
    price = price.map(p => Call.Price(BigDecimal(p), Iso4127CountryCode.apply(price_unit))),
    direction = Call.Direction.fromTwilioStringUnsafe(direction),
    answeredBy = answered_by.map(Call.AnsweredBy.fromTwilioStringUnsafe),
    forwardedFrom = forwarded_from.map(Call.ForwardedFrom),
    groupSid = group_sid.map(Group.Sid.unsafe),
    callerName = caller_name.map(Call.Name),
    queueTime = Call.QueueTime(queue_time),
    trunkSid = trunk_sid.map(Trunk.Sid.unsafe)
  )
}
