package com.dixa.twilio.model.dtmf
import com.dixa.twilio.model.TwilioStringValue
import com.dixa.twilio.model.dtmf.DtmfDigit.DtmfDigitException

import scala.collection.immutable
import scala.language.implicitConversions

/** Represent a string of DTMF digits and potentially waits.
  *
  * There are two subtypes of this class, one for representing a String of pure DTMF digits, and one
  * that also allows a waiting digit (represented by a w when printed). The latter is often used,
  * when sending DTMF digits over the wire. It is implemented so that you get the most specific type
  * back, based on what you provide to the factory methods. The subtype that include waits, wraps
  * all it's elements in a special type, but an implicit conversion from DtmfDigit to that wrapper
  * should do, that you in most cases don't have to care about it, as you can just provide
  * DtmfDigits instances to the factory methods.
  *
  * This type does not support representing an empty value, and it is enforced at compile time. In a
  * lot of places it won't make sense to have an empty value, like when providing digits to the Play
  * TwiML verb, and you can always wrap it in an Option if you need it.
  */
sealed abstract class DtmfString extends TwilioStringValue {

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
      twilioString == that.twilioString
    case _ => false
  }

  /** hashCode implementation, that follows the classic Java hashCode contract.
    *
    * For an good explanation of the Java hashCode contract, you could look it up in the book:
    * Effective Java
    */
  override def hashCode(): Int = twilioString.hashCode

  override lazy val toString = s"DtmfString.${getClass.getSimpleName}($twilioString)"

}

object DtmfString {

  /** ADT Represent an element of a DtmfString, that can either be a DtmfDigit or a wait (w).
    *
    * There is an implicit conversion in the companion object, for converting a DtmfDigit to this
    * type, making it pretty easy to create DtmfString instances, without needing to wrap each
    * DtmfDigit in this type explicitly.
    */
  sealed abstract class DtmfStringElement extends TwilioStringValue
  object DtmfStringElement {
    final case class DtmfDigitElement(digit: DtmfDigit) extends DtmfStringElement {
      override def twilioString: String = digit.twilioString
    }

    case object WaitElement extends DtmfStringElement {
      override def twilioString: String = "w"
    }

    implicit def fromDtmfDigit(digit: DtmfDigit): DtmfDigitElement = DtmfDigitElement(digit)
  }

  def w: DtmfStringElement = DtmfStringElement.WaitElement

  final class OnlyDtmfDigits private[DtmfString] (private[DtmfString] val seq: Vector[DtmfDigit])
      extends DtmfString {

    override lazy val twilioString: String = seq.mkString

    def asSeqDtmfDigit: immutable.Seq[DtmfDigit] = seq

    def map(fun: DtmfDigit => DtmfDigit): DtmfString.OnlyDtmfDigits = new OnlyDtmfDigits(
      seq.map(fun)
    )

    def map(fun: DtmfDigit => DtmfStringElement): IncludeWaits = new IncludeWaits(seq.map(fun))

    def flatMap(fun: DtmfDigit => DtmfString.OnlyDtmfDigits): DtmfString.OnlyDtmfDigits = {
      val newSeq = seq.flatMap(fun(_).seq)
      new OnlyDtmfDigits(newSeq)
    }

    def flatMap(fun: DtmfDigit => DtmfString.IncludeWaits): DtmfString.IncludeWaits = {
      val newSeq = seq.flatMap(fun(_).seq)
      new IncludeWaits(newSeq)
    }
  }

  final class IncludeWaits private[DtmfString] (
      private[DtmfString] val seq: Vector[DtmfStringElement]
  ) extends DtmfString {
    override lazy val twilioString: String = seq.view.map(_.twilioString).mkString

    def map(fun: DtmfStringElement => DtmfStringElement): DtmfString.IncludeWaits =
      new IncludeWaits(seq.map(fun))

    def map(fun: DtmfStringElement => DtmfDigit): OnlyDtmfDigits = new OnlyDtmfDigits(seq.map(fun))

    def flatMap(fun: DtmfStringElement => DtmfString.OnlyDtmfDigits): DtmfString.OnlyDtmfDigits = {
      val newSeq = seq.flatMap(fun(_).seq)
      new OnlyDtmfDigits(newSeq)
    }

    def flatMap(fun: DtmfStringElement => DtmfString.IncludeWaits): DtmfString.IncludeWaits = {
      val newSeq = seq.flatMap(fun(_).seq)
      new IncludeWaits(newSeq)
    }
  }

  sealed trait DtmfStringException extends RuntimeException

  object DtmfStringException {
    final case object EmptyValue
        extends IllegalStateException("DtmfString not allowed to be empty")
        with DtmfStringException

    final case class InvalidChar(invalidCharException: DtmfDigitException.InvalidChar)
        extends IllegalArgumentException(invalidCharException.getMessage)
        with DtmfStringException
  }

  def apply(first: DtmfDigit, rest: DtmfDigit*): OnlyDtmfDigits = new OnlyDtmfDigits(
    first +: rest.toVector
  )

  def apply(first: DtmfStringElement, rest: DtmfStringElement*): IncludeWaits = new IncludeWaits(
    first +: rest.toVector
  )

  def fromSeq(first: DtmfDigit, rest: Seq[DtmfDigit]): OnlyDtmfDigits = new OnlyDtmfDigits(
    first +: rest.toVector
  )

  def fromSeq(first: DtmfStringElement, rest: Seq[DtmfStringElement]): IncludeWaits =
    new IncludeWaits(first +: rest.toVector)

  def fromStringIncludeWaits(s: String): Either[DtmfStringException, IncludeWaits] = s match {
    case empty if empty == null || empty.isEmpty => Left(DtmfStringException.EmptyValue)
    case singleChar if singleChar.length == 1    =>
      singleChar.head match {
        case 'w'           => Right(new IncludeWaits(Vector(w)))
        case possibleDigit =>
          DtmfDigit
            .fromChar(possibleDigit)
            .map(d => new IncludeWaits(Vector(d)))
            .left
            .map(DtmfStringException.InvalidChar)
      }
    case multiCharString =>
      val mapped = multiCharString.map {
        case 'w'           => Right(w)
        case possibleDigit => DtmfDigit.fromChar(possibleDigit).map(DtmfStringElement.fromDtmfDigit)
      }
      mapped.find(_.isLeft) match {
        case Some(Left(e)) => Left(DtmfStringException.InvalidChar(e))
        case _             =>
          val mappedUnwrapped = mapped.flatMap(_.toOption)
          Right(new IncludeWaits(mappedUnwrapped.toVector))
      }
  }

  def fromStringIncludeWaitsUnsafe(s: String): DtmfString =
    fromStringIncludeWaits(s).fold(e => throw e, identity)

  def fromStringOnlyDtmfDigits(s: String): Either[DtmfStringException, OnlyDtmfDigits] = s match {
    case empty if empty == null || empty.isEmpty => Left(DtmfStringException.EmptyValue)
    case charString                              =>
      val mapped = charString.map { possibleDigit =>
        DtmfDigit.fromChar(possibleDigit)
      }
      mapped.find(_.isLeft) match {
        case Some(Left(e)) => Left(DtmfStringException.InvalidChar(e))
        case _             => Right(new OnlyDtmfDigits(mapped.flatMap(_.toOption).toVector))

      }
  }

  def fromStringOnlyDtmfDigitsUnsafe(s: String): OnlyDtmfDigits =
    fromStringOnlyDtmfDigits(s).fold(e => throw e, identity)

}
