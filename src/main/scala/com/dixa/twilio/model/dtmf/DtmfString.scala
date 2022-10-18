package com.dixa.twilio.model.dtmf
import com.dixa.twilio.model.TwilioStringValue

/** Represent a string of DTMF digits
  *
  * This type is not supporting representing an empty value. and it is enforced compile time. In a
  * lot of places it won't make sense to have a empty value, like when providing digits to the Play
  * TwiML verb, and you can always wrap it in an Option if you need it.
  */
final class DtmfString private (private val seq: IndexedSeq[DtmfDigit]) extends TwilioStringValue {

  override def equals(other: Any): Boolean = other match {
    case that: DtmfString =>
      seq == that.seq
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(seq)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"DtmfString(${seq.mkString(",")})"

  override def twilioString: String = seq.mkString("")
}

object DtmfString {

  def apply(first: DtmfDigit, rest: DtmfDigit*): DtmfString = new DtmfString(first +: rest.toVector)
}
