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

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.model.Iso4127CountryCode
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.TwilioPhoneNumber
import com.dixa.twilio.model.voice.Call.FormattedPhoneNumber
import com.dixa.twilio.model.voice.{Call, Group, Trunk}
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

import java.time.{Duration, Instant}
import scala.util.Try

/** Json representation of a Call */
private[impl] case class CallJsonRep(
    account_sid: String,
    answered_by: Option[String] = None,
    api_version: String,
    caller_name: Option[String] = None,
    date_created: String,
    date_updated: String,
    direction: String,
    duration: Option[String] = None,
    end_time: Option[String] = None,
    forwarded_from: Option[String] = None,
    from: String,
    from_formatted: String,
    group_sid: Option[String],
    parent_call_sid: Option[String] = None,
    phone_number_sid: Option[String] = None,
    price: Option[String] = None,
    price_unit: String,
    sid: String,
    start_time: Option[String] = None,
    status: String,
    to: String,
    to_formatted: String,
    trunk_sid: Option[String] = None,
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
    phoneNumberSid = phone_number_sid.flatMap(s => TwilioPhoneNumber.Sid(s).toOption),
    status = Call.Status.fromTwilioStringUnsafe(status),
    startTime = start_time.map(time => Instant.from(Formatter.dateTime.parse(time))),
    endTime = end_time.map(time => Instant.from(Formatter.dateTime.parse(time))),
    duration = optionStringToOptionLong(duration).map(Duration.ofSeconds),
    price = price.map(p => Call.Price(BigDecimal(p), Iso4127CountryCode.apply(price_unit))),
    direction = Call.Direction.fromTwilioStringUnsafe(direction),
    answeredBy = answered_by.map(Call.AnsweredBy.fromTwilioStringUnsafe),
    forwardedFrom = forwarded_from.map(Call.ForwardedFrom),
    groupSid = group_sid.flatMap(s => Group.Sid(s).toOption),
    callerName = caller_name.map(Call.Name),
    queueTime = Try(queue_time.toLong).map(Duration.ofMillis).getOrElse(Duration.ZERO),
    trunkSid = trunk_sid.flatMap(s => Trunk.Sid(s).toOption)
  )

  private def optionStringToOptionLong(x: Option[String]): Option[Long] =
    x.flatMap(asString => Try(asString.toLong).toOption)
}

private[voice] object CallJsonRep {
  implicit val upickleReader: Reader[CallJsonRep] =
    macroR[CallJsonRep]
}
