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

package com.dixa.twilio.model

import enumeratum.Enum

/** Base trait for a Enum having an twilioString value.
  *
  * Multiple enums in this library are represented as String values in Twilio. This trait works as a
  * base trait for all of these enums, and unifies the name of the values and how an enum value can
  * be found from it.
  *
  * @tparam A
  *   Type of the EnumEntry. Must extend the special EnumEntry found in the companion object.
  */
trait EnumWithTwilioString[A <: EnumWithTwilioString.EnumEntry] extends Enum[A] {

  import EnumWithTwilioString._

  /** Return the value corresponding to provided twilioString. Returns None if not found */
  def fromTwilioString(twilioString: String): Option[A] =
    values.find(_.twilioString == twilioString)

  /** Return the value corresponding to provided twilioString in a Right. Returns Left if not found
    */
  def fromTwilioStringEither(twilioString: String): Either[TwilioStringNotFoundException, A] =
    fromTwilioString(twilioString).toRight(createTwilioStringNotFoundException(twilioString))

  /** Return the value corresponding to provided twilioString.
    *
    * Throws [[com.dixa.twilio.model.EnumWithTwilioString.TwilioStringNotFoundException]] if not
    * found
    */
  def fromTwilioStringUnsafe(twilioString: String): A =
    fromTwilioString(twilioString).getOrElse(
      throw createTwilioStringNotFoundException(twilioString)
    )

  /** Return the value corresponding to provided twilioString with case insensitive matching.
    *
    * Returns None if not found
    */
  def fromTwilioStringCaseInsensitive(twilioString: String): Option[A] =
    values.find(_.twilioString.equalsIgnoreCase(twilioString))

  /** Return the value corresponding to provided twilioString with case insensitive matching as a
    * Right.
    *
    * Returns Left if not found
    */
  def fromTwilioStringCaseInsensitiveEither(
      twilioString: String
  ): Either[TwilioStringNotFoundException, A] =
    fromTwilioStringCaseInsensitive(twilioString).toRight(
      createTwilioStringNotFoundException(twilioString)
    )

  /** Return the value corresponding to provided twilioString with case insensitive matching.
    *
    * Throws [[com.dixa.twilio.model.EnumWithTwilioString.TwilioStringNotFoundException]] if not
    * found
    */
  def fromTwilioStringCaseInsensitiveUnsafe(twilioString: String): A =
    fromTwilioStringCaseInsensitive(twilioString).getOrElse(
      throw createTwilioStringNotFoundException(twilioString)
    )

  private def createTwilioStringNotFoundException(twilioString: String) =
    TwilioStringNotFoundException(
      s"$twilioString is not a valid ${getClass.getSimpleName} value. Possible values are: $values"
    )

}

object EnumWithTwilioString {

  trait EnumEntry extends enumeratum.EnumEntry with TwilioStringValue

  final case class TwilioStringNotFoundException(msg: String) extends IllegalArgumentException(msg)
}
