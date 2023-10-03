package com.dixa.twilio.model

import scala.annotation.nowarn

/** Wrapper of an integer, that guarantees the wrapped integer to be positive.
  *
  * In Twilio there is a lot of attributes that is of type integer, but requires it to be positive,
  * and this type is therefore useful for representing such cases, as a method taking this type, can
  * be sure that it represents a positive integer, and that potential errors for guaranteeing that
  * have already been handled.
  */
final case class PositiveInteger private (int: Int) extends TwilioStringValue {
  override def toString: String = int.toString
}

object PositiveInteger {

  sealed trait Err extends RuntimeException
  object Err {
    case class NotPositive(int: Int)
        extends RuntimeException(s"$int is not a positive integer ( > 0 )")
        with Err
  }

  // override apply method as private, to ensure clients cannot create invalid instance.
  @nowarn(value = "cat=unused")
  private def apply(int: Int): PositiveInteger = new PositiveInteger(int)

  def safe(int: Int): Either[Err, PositiveInteger] =
    if (int > 0) Right(new PositiveInteger(int)) else Left(Err.NotPositive(int))

  def unsafe(int: Int): PositiveInteger = safe(int).toTry.get

}
