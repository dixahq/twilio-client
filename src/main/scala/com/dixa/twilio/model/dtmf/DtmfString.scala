package com.dixa.twilio.model.dtmf
import com.dixa.twilio.model.TwilioStringValue
import com.dixa.twilio.model.dtmf.DtmfDigit.DtmfDigitException

/** Represent a string of DTMF digits
  *
  * This type does not support representing an empty value, and it is enforced at compile time. In a
  * lot of places it won't make sense to have an empty value, like when providing digits to the Play
  * TwiML verb, and you can always wrap it in an Option if you need it.
  */
final class DtmfString private (private val seq: Vector[DtmfDigit]) extends TwilioStringValue {

  // Not a case class, so implement equals, hashCode and toString manually.

  /** Equals method that follows the classic Java equals contract.
    *
    * It will return true on matching DtmfString instances. Matches the implementation of the
    * hashCode method.
    *
    * For an good explanation of the Java equals contract, you could look it up in the book:
    * Effective Java.
    */
  override def equals(other: Any): Boolean = other match {
    case that: DtmfString =>
      seq == that.seq
    case _ => false
  }

  /** hashCode implementation, that follows the classic Java hashCode contract.
    *
    * For an good explanation of the Java hashCode contract, you could look it up in the book:
    * Effective Java
    */
  override def hashCode(): Int = {
    val state = Seq(seq)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override lazy val toString = s"DtmfString(${seq.mkString(",")})"

  override lazy val twilioString: String = seq.mkString("")

  def map(fun: DtmfDigit => DtmfDigit): DtmfString = new DtmfString(seq.map(fun))

  def flatMap(fun: DtmfDigit => DtmfString): DtmfString = {
    val newSeq = seq.flatMap(fun(_).seq)
    new DtmfString(newSeq)
  }
}

object DtmfString {

  sealed trait DtmfStringException extends RuntimeException

  object DtmfStringException {
    final case object EmptyValue
        extends IllegalStateException("DtmfString not allowed to be empty")
        with DtmfStringException

    final case class InvalidChar(invalidCharException: DtmfDigitException.InvalidChar)
        extends IllegalArgumentException(invalidCharException.getMessage)
        with DtmfStringException
  }

  def apply(first: DtmfDigit, rest: DtmfDigit*): DtmfString = new DtmfString(first +: rest.toVector)

  def fromSeq(first: DtmfDigit, rest: Seq[DtmfDigit]): DtmfString = new DtmfString(
    first +: rest.toVector
  )

  def fromString(s: String): Either[DtmfStringException, DtmfString] = s match {
    case empty if empty == null || empty.isEmpty => Left(DtmfStringException.EmptyValue)
    case singleChar if singleChar.length == 1 =>
      DtmfDigit.fromChar(s.head).map(DtmfString(_)).left.map(DtmfStringException.InvalidChar)
    case multiCharString =>
      val mapped = multiCharString.map(DtmfDigit.fromChar)
      mapped.find(_.isLeft) match {
        case Some(e) => Left(DtmfStringException.InvalidChar(e.left.get))
        case None =>
          val mappedUnwrapped = mapped.map(_.right.get)
          Right(DtmfString.fromSeq(mappedUnwrapped.head, mappedUnwrapped.tail))
      }
  }

  def fromStringUnsafe(s: String): DtmfString = fromString(s).fold(e => throw e, identity)

}
