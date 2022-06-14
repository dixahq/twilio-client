package com.dixa.twilio.model

import enumeratum.Enum
import org.scalactic.TypeCheckedTripleEquals._

/** Base trait for a Enum having an twilioString value.
  *
  * Multiple enums in this library, is represented as String values in Twilio. This trait works as a
  * base trait for all of these enums, unifies the name of the value and how a enum value can be
  * found from it.
  *
  * @tparam A
  *   Type of the EnumEntry. Must extends the special EnumEntry found in the companion object.
  */
trait EnumWithTwilioString[A <: EnumWithTwilioString.EnumEntry] extends Enum[A] {

  import EnumWithTwilioString._

  /** Return the value corresponding to provided twilioString. Returns None if not found */
  def fromTwilioString(twilioString: String): Option[A] =
    values.find(_.twilioString === twilioString)

  /** Return the value corresponding to provided twilioString in a Right. Returns Left if not found
    */
  def fromTwilioStringEither(twilioString: String): Either[TwilioStringNotFoundException, A] =
    fromTwilioString(twilioString).toRight(createTwilioStringNotFoundException(twilioString))

  /** Return the value corresponding to provided twilioString.
    *
    * @throws EnumWithTwilioString.TwilioStringNotFoundException
    *   if not found
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
    * @throws EnumWithTwilioString.TwilioStringNotFoundException
    *   if not found
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

  trait EnumEntry extends enumeratum.EnumEntry {
    def twilioString: String
  }

  final case class TwilioStringNotFoundException(msg: String) extends IllegalArgumentException(msg)
}
