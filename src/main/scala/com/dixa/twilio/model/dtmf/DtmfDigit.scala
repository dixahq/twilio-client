package com.dixa.twilio.model.dtmf
import com.dixa.twilio.model.EnumWithTwilioString

import scala.collection.immutable

/** Enum representing all the possible DTMF digits. */
sealed abstract class DtmfDigit(asChar: Char) extends EnumWithTwilioString.EnumEntry {
  override val toString: String = asChar.toString
}

object DtmfDigit extends EnumWithTwilioString[DtmfDigit] {
  override def values: immutable.IndexedSeq[DtmfDigit] = findValues

  case object `1` extends DtmfDigit('1')
  case object `2` extends DtmfDigit('2')
  case object `3` extends DtmfDigit('3')
  case object `4` extends DtmfDigit('4')
  case object `5` extends DtmfDigit('5')
  case object `6` extends DtmfDigit('6')
  case object `7` extends DtmfDigit('7')
  case object `8` extends DtmfDigit('8')
  case object `9` extends DtmfDigit('9')
  case object `0` extends DtmfDigit('0')
  case object `*` extends DtmfDigit('*')
  case object `#` extends DtmfDigit('#')
  case object `w` extends DtmfDigit('w')
}
