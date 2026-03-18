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

package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.phonenumber.PhoneNumberE164

import scala.util.Try

sealed trait WhatsappNumber extends MessageRecipient {
  def number: PhoneNumberE164

  private def asString: String = WhatsappNumber.Prefix + number.toString

  override final val toString = asString
}

object WhatsappNumber {
  val Prefix = "whatsapp:"

  def unsafe(asString: String): WhatsappNumber = {
    require(
      asString.startsWith(Prefix),
      s"$toString is not a valid whatsapp number, must start with '$Prefix'"
    )
    val number = PhoneNumberE164.unsafe(asString.drop(Prefix.length))
    DefaultImpl(number)
  }

  def apply(asString: String): Option[WhatsappNumber] = Try {
    unsafe(asString)
  }.toOption

  def fromPhoneNumberE164(number: PhoneNumberE164): WhatsappNumber = DefaultImpl(number)

  private final case class DefaultImpl(number: PhoneNumberE164) extends WhatsappNumber
}
