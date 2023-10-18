package com.dixa.twilio.model

import scala.annotation.unused

/** Class that can be extended by classes that represent a String with constraints.
  *
  * This is a easy way to make a class that is basically a String wrapper, but should enforce
  * constrains on what strings it could wrap. At time of writing max lenght is the only supported
  * constraint, but more can be added as needed.
  *
  * For this class to work, you would need to make you class a case class having a private
  * constructor, with exactly one parameter that is an override of toString. You should also create
  * a companion object that extends the [[ConstrainedString.ConstrainedStringCompanionObject]]
  * class.
  *
  * See [[com.dixa.twilio.model.general.Application.FriendlyName]] for an example of how to use
  * this.
  */
abstract class ConstrainedString() {

  @unused
  private def copy(toString: String): ConstrainedString =
    throw new UnsupportedOperationException(
      "Disallow copy, as that would be a way to create instances that break the length limitation"
    )
}

object ConstrainedString {

  /** Base error type representing errors creating a [[ConstrainedString]].
    *
    * This is the Base error type of all the implementations of [[ConstrainedString]]. Each
    * individual implementation will have it's own version of this, scoped withing it's companion
    * object.
    */
  trait CreationException extends RuntimeException

  abstract class NullValueException()
      extends NullPointerException("ConstrainedString does not support wrapping null values")
      with CreationException

  abstract class ToLongException(wrapped: String, maxLength: Int)
      extends IllegalArgumentException(
        s"$wrapped does not conform to maxLength of $maxLength, as it has length: ${wrapped.length}"
      )
      with CreationException

  /** Error to be returned, if wrapped string is expected to represent a Decimal value but does not.
    *
    * A Decimal value is understood as a value only containing digits and optional a single `.`
    */
  abstract class NotDecimalException(wrapped: String)
      extends IllegalArgumentException(
        s"$wrapped is not a deciaml value as required"
      )
      with CreationException

  /** Error to be returned, if wrapped string is expected to only contain a specific set of chars,
    * but does not.
    */
  abstract class InvalidCharException(wrapped: String, allowedChars: Set[Char])
      extends IllegalArgumentException(
        s"$wrapped contains invalid char. Valid chars are: $allowedChars"
      )
      with CreationException

  /** Error to be returned if wrapped string is expected to end on a specific suffix, but does not.
    */
  abstract class InvalidSuffixException(wrapped: String, expectedSuffix: String)
      extends IllegalArgumentException(s"$wrapped does not have required suffix: $expectedSuffix")
      with CreationException

  // format: off
  /** Class to be extended by the companion object of a Constraint Sid.
    *
    * This class will:
    *
    *   1. Provide a sub type of each of the error type.
    *   2. Provide a safe method for constructing instances in a safe way.
    *   3. Provide a unsafe method, for constructing instances in an unsafe way, throwing exception if
    *      input does not conform to constrains.
    *   4. Ensure that a default apply method is not generated in the companion object.
    *   5. enforce all of the constraints specified by the companion object extending this.
    *
    * Constraints are specified by overriding methods. The default implementations don't activate any
    * constraints, so you should always override at least one.
    *
    * @param requireSuffix
    */
  // format: on
  abstract class ConstrainedStringCompanionObject[S <: ConstrainedString] {

    protected def maxLength: Option[Int] = None

    protected def decimalOnly: Boolean = false

    /** set of chars that will be the only one allowed in the String.
      *
      * Provided empty set disables this constraint.
      *
      * Note that if requireSuffix is also set, then that suffix will be stripped from the string,
      * before checking for invalid chars.
      */
    protected def validChars: Set[Char] = Set.empty

    /** suffix that the string should end on. Providing empty string disabled this constraint
      */
    protected def requireSuffix: String = ""

    @unused
    protected final def apply(s: String): S = throw new UnsupportedOperationException(
      "override this as protected, to stop the scala compiler for generating a public apply method, that could " +
        "create instances without checking the constraints. For some reason making it private does not work," +
        "then the scala compiler actually somehow overrides it with a public method in the implementations."
    )

    protected def constructInstance(wrapped: String): S

    sealed trait CreationException extends ConstrainedString.CreationException

    // Follow implementations of CreationException should be final, but due to this issue:
    // https://github.com/scala/bug/issues/4440
    // that produces compile errors like:
    // `The outer reference in this type test cannot be checked at run time.`
    // So instead making them sealed, as that is practically the same, as they
    // are not extended withing this file.
    sealed case class NullValueException()
        extends ConstrainedString.NullValueException
        with CreationException

    sealed case class ToLongException(wrapped: String, maxLength: Int)
        extends ConstrainedString.ToLongException(wrapped, maxLength)
        with CreationException

    sealed case class NotDecimalException(wrapped: String)
        extends ConstrainedString.NotDecimalException(wrapped)
        with CreationException

    sealed case class InvalidCharException(wrapped: String, allowedChars: Set[Char])
        extends IllegalArgumentException(
          s"$wrapped contains invalid char. Valid chars are: $allowedChars"
        )
        with CreationException

    sealed case class InvalidSuffixException(wrapped: String, expectedSuffix: String)
        extends IllegalArgumentException(s"$wrapped does not have required suffix: $expectedSuffix")
        with CreationException

    /** Construct an instance of this ConstrainedString, returning either an result or an error. */
    def safe(wrapped: String): Either[CreationException, S] =
      if (wrapped == null) Left(NullValueException())
      else if (maxLength.isDefined && wrapped.length > maxLength.get)
        Left(ToLongException(wrapped, maxLength.get))
      else if (decimalOnly && !representDecimalValue(wrapped)) Left(NotDecimalException(wrapped))
      else if (requireSuffix.nonEmpty && !wrapped.endsWith(requireSuffix))
        Left(InvalidSuffixException(wrapped, requireSuffix))
      else if (validChars.nonEmpty && containsInvalidValidChars(wrapped))
        Left(InvalidCharException(wrapped, validChars))
      else Right(constructInstance(wrapped))

    /** Construct an instance of this ConstrainedString, returning either an result or throwing an
      * error.
      */
    def unsafe(wrapped: String): S = safe(wrapped).toTry.get

    private def representDecimalValue(s: String): Boolean = {
      var dotEncountered = false
      s.forall { c =>
        if (
          (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' ||
          c == '7' || c == '8' || c == '9')
        ) true
        else if (c == '.') {
          if (dotEncountered) {
            // Then this is second dot, and that is not allowed
            false
          } else {
            // First dot that is ok
            dotEncountered = true
            true
          }
        } else false
      }
    }

    private def containsInvalidValidChars(s: String): Boolean = {
      val toCheck = if (requireSuffix.nonEmpty) s.substring(0, s.indexOf(requireSuffix)) else s
      toCheck.exists(!validChars.contains(_))
    }
  }

}
