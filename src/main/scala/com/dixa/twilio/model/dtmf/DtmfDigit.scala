package com.dixa.twilio.model.dtmf
import com.dixa.twilio.model.EnumWithTwilioString

import scala.collection.immutable

/** Enum representing all the possible DTMF digits. */
sealed abstract class DtmfDigit(private val asChar: Char, val isNumerical: Boolean)
    extends EnumWithTwilioString.EnumEntry {
  override val toString: String = asChar.toString
}

object DtmfDigit extends EnumWithTwilioString[DtmfDigit] {
  override def values: immutable.IndexedSeq[DtmfDigit] = findValues

  case object `1` extends DtmfDigit('1', true)
  case object `2` extends DtmfDigit('2', true)
  case object `3` extends DtmfDigit('3', true)
  case object `4` extends DtmfDigit('4', true)
  case object `5` extends DtmfDigit('5', true)
  case object `6` extends DtmfDigit('6', true)
  case object `7` extends DtmfDigit('7', true)
  case object `8` extends DtmfDigit('8', true)
  case object `9` extends DtmfDigit('9', true)
  case object `0` extends DtmfDigit('0', true)
  case object `*` extends DtmfDigit('*', false)
  case object `#` extends DtmfDigit('#', false)

  sealed trait DtmfDigitException extends RuntimeException
  object DtmfDigitException {
    final case class InvalidChar(char: Char)
        extends IllegalArgumentException(
          s"$char is not a valid DtmfDigit. Allowed values are: ${values.map(_.asChar).mkString(",")}"
        )
        with DtmfDigitException
  }

  def fromChar(char: Char): Either[DtmfDigitException.InvalidChar, DtmfDigit] =
    values
      .find(_.asChar == char)
      .map(Right(_))
      .getOrElse(Left(DtmfDigitException.InvalidChar(char)))

  def fromCharUnsafe(char: Char): DtmfDigit = fromChar(char).fold(e => throw e, identity)

  val allNumerical: immutable.IndexedSeq[DtmfDigit] = values.filter(_.isNumerical).sortBy(_.asChar)

}
