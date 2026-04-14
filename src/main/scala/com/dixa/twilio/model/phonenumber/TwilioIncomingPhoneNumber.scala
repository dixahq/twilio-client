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

package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.Region
import com.dixa.twilio.model.iam.TwilioAccount

sealed trait TwilioIncomingPhoneNumber extends TwilioPhoneNumber {
  def sid: TwilioPhoneNumber.Sid
  def accountSid: TwilioAccount.Sid
  def friendlyName: TwilioIncomingPhoneNumber.FriendlyName
  def phoneNumber: PhoneNumberE164
  def capabilities: TwilioIncomingPhoneNumber.PhoneNumberCapabilitiesSummary
  def region: Region
}

object TwilioIncomingPhoneNumber {

  final case class FriendlyName(override val toString: String)

  /** Wrapper arround a string, for representing a textual contains filter.
    *
    * Some functionality working with incoming phone number support a filter, and this class
    * represent such a filter. A filter works like contains, so applying a filter, will return all
    * result where the phone number contains the filter.
    */
  final case class PhoneNumberFilter(override val toString: String)

  final case class PhoneNumberCapabilitiesSummary(
      voice: Boolean,
      sms: Boolean,
      mms: Boolean,
      fax: Boolean,
  )

  def apply(
      sid: TwilioPhoneNumber.Sid,
      accountSid: TwilioAccount.Sid,
      friendlyName: TwilioIncomingPhoneNumber.FriendlyName,
      phoneNumber: PhoneNumberE164,
      capabilities: PhoneNumberCapabilitiesSummary,
      region: Region
  ): TwilioIncomingPhoneNumber =
    DefaultImpl(sid, accountSid, friendlyName, phoneNumber, capabilities, region)

  private final case class DefaultImpl(
      sid: TwilioPhoneNumber.Sid,
      accountSid: TwilioAccount.Sid,
      friendlyName: TwilioIncomingPhoneNumber.FriendlyName,
      phoneNumber: PhoneNumberE164,
      capabilities: PhoneNumberCapabilitiesSummary,
      region: Region
  ) extends TwilioIncomingPhoneNumber
}
