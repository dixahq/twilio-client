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

import com.dixa.twilio.model.TwilioStringValue
import com.dixa.twilio.model.messaging.MessageSender.Alphanumeric.AlphanumericException.AlphanumericInvalidException
import com.dixa.twilio.model.messaging.MessageSender.MessageSenderException.MessageSenderInvalidException
import com.dixa.twilio.model.phonenumber.PhoneNumberE164

import scala.annotation.nowarn

// There is also WirelessSIM, not included
sealed abstract class MessageSender extends TwilioStringValue {
  def asString: String
}

object MessageSender {

  trait MessageSenderException extends RuntimeException

  object MessageSenderException {
    case class MessageSenderInvalidException(s: String)
        extends RuntimeException(
          s"$s is not a valid phone number, whatsapp number or alphanumeric sender id"
        )
        with MessageSenderException
  }

  def fromString(s: String): Either[MessageSenderException, MessageSender] = {
    PhoneNumberE164(s)
      .map(E164)
      .orElse(WhatsappNumber(s).map(Whatsapp))
      .orElse(Alphanumeric.fromString(s).toOption)
      .toRight(MessageSenderInvalidException(s))
  }

  def fromStringUnsafe(s: String): MessageSender = {
    fromString(s).toTry.get
  }

  final case class E164(phoneNumber: PhoneNumberE164) extends MessageSender {
    override def asString: String = phoneNumber.asString
  }

  final case class Whatsapp(whatsappNumber: WhatsappNumber) extends MessageSender {
    override def asString: String = whatsappNumber.toString
  }

  final case class Alphanumeric private (override val asString: String) extends MessageSender

  object Alphanumeric {

    // override apply method as private to ensure clients cannot create invalid instances
    @nowarn(value = "cat=unused")
    private def apply(asString: String): Alphanumeric = new Alphanumeric(asString)

    trait AlphanumericException extends RuntimeException
    object AlphanumericException {
      case class AlphanumericInvalidException(s: String)
          extends RuntimeException(s"$s is not a valid alphanumeric sender id")
          with AlphanumericException
    }

    /** Alphanumeric sender ID restrictions (did not include some special symbols that Twilio claims
      * to support): 1.Alphanumeric Sender IDs may be up to 11 characters long. 2. Accepted
      * characters include both upper-case and lower-case Ascii letters, digits 0 through 9, and the
      * space character. 3. Alphanumeric Sender IDs may not be only numerals. They must include at
      * least one letter.
      *
      * @see
      *   https://www.twilio.com/docs/glossary/what-alphanumeric-sender-id
      * @see
      *   https://gatewayapi.com/help-center/sender-id/
      */
    def isValid(s: String): Boolean = {
      val alphanumericRegex = "^(?=.*[a-zA-Z])[a-zA-Z0-9 ]{1,11}$"
      if (!s.matches(alphanumericRegex) || s.trim.isEmpty)
        false
      else true
    }

    /** Create a new Alphanumeric instance from a string.
      *
      * The input String will be trimmed, as leading or trailing whitespaces don't make sense in a
      * alphanumeric sender id. Twilio does the same, if we allowed untrimmed values here, we would
      * end in situations where Twilio send back a different value in their response, than we send
      * them in the request.
      */
    def fromString(s: String): Either[AlphanumericException, Alphanumeric] = {
      val trimmed = s.trim
      if (isValid(trimmed)) Right(new Alphanumeric(trimmed))
      else Left(AlphanumericInvalidException(s))
    }

    /** Exception throwing version of [[fromString]] */
    def fromStringUnsafe(s: String): Alphanumeric = {
      fromString(s).toTry.get
    }
  }
}
