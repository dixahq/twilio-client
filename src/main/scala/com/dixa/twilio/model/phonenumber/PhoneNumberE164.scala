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

import com.dixa.twilio.model.messaging.MessageRecipient

import scala.util.Try

sealed trait PhoneNumberE164 extends MessageRecipient {

  def asString: String
  override final val toString = asString
}

object PhoneNumberE164 {

  def unsafe(asString: String): PhoneNumberE164 = {
    require(verifyPattern.matcher(asString).matches(), s"$toString is not in E.164 format")
    DefaultImpl(asString)
  }

  def apply(asString: String): Option[PhoneNumberE164] = Try {
    unsafe(asString)
  }.toOption

  private val verifyPattern = """^\+[1-9]\d{1,14}$""".r.pattern

  private final case class DefaultImpl(asString: String) extends PhoneNumberE164
}
