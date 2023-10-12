package com.dixa.twilio.model

import scala.reflect.{classTag, ClassTag}

/** Base type for all classes representing a SID in Twilio.
  *
  * That is Sids that conform to: "It is a 34 character string that starts with a 2 character
  * prefix, where the prefix indicates the entity the sid represent". This representation of a Sid
  * is documented by Twilio here: https://www.twilio.com/docs/glossary/what-is-a-sid
  *
  * It is recommended for subclasses, to make there constructor private, and instead let the
  * companion object extend the SidCompanionObject class. This will ensure consistency in how we
  * create Sid instances, Errors returned if creation fails, and save a lot of code.
  */
// TODO PR Fix so that you cannot copy into invalid instance
abstract class SidAbstract extends TwilioStringValue {

  /** The string representation of this SID.
    *
    * This will return the raw value, without any class names, parentheses etc.
    */
  def toString: String
}

object SidAbstract {

  private final case class ConformToString(override val toString: String) extends AnyVal

  /** Represent a Prefix of a SID. See SidAbstract scaladoc for details. */
  final case class Prefix(override val toString: String) extends AnyVal

  /** Base error type representing an error in creation of a Sid
    *
    * This is the Base error types of all Sid implementations. Each individual Sid subclass will
    * have it's own version of this, scoped within it's companion object.
    */
  trait CreationException extends RuntimeException

  /** Base error type for creating a Sid out of of empty input.
    *
    * This is the Base error types of all Sid implementations. Each individual Sid subclass will
    * have it's own version of this, scoped within it's companion object.
    */
  abstract class ArgumentEmptyException(conformToString: ConformToString)
      extends IllegalArgumentException(s"Empty string does not conform to: $conformToString")
      with CreationException

  /** Base error type for creating a Sid, missing the appropriate prefix.
    *
    * This is the Base error types of all Sid implementations. Each individual Sid subclass will
    * have it's own version of this, scoped within it's companion object.
    */
  abstract class ArgumentMissingPrefixException(
      argument: String,
      prefixes: List[Prefix],
      conformToString: ConformToString
  ) extends IllegalArgumentException(
        s"$argument does not start with ${prefixes.mkString(",")} and therefore not conform to: $conformToString"
      )
      with CreationException

  /** Base error type for creating a Sid with input having invalid length.
    *
    * This is the Base error types of all Sid implementations. Each individual Sid subclass will
    * have it's own version of this, scoped within it's companion object.
    */
  abstract class ArgumentLengthException(argument: String, conformToString: ConformToString)
      extends IllegalArgumentException(
        s"$argument has length not conforming to: $conformToString"
      )
      with CreationException

  // format: off
  /** Class to be extended by the companion object of a Sid subclass.
    *
    *   1. This class will provide a sub type specific version of each possible error type.
    *   2. A Safe method for construction of a Sid having errors as part of the return type.
    *   3. An Unsafe method that will construct a Sid, throwing exceptions on errors.
    */
  // format: on
  abstract class SidCompanionObject[S <: SidAbstract: ClassTag](
      val prefixes: List[Prefix],
      instanceFactory: String => S
  ) {

    private val entityName = classTag[S].runtimeClass.getName

    private val conformToString = ConformToString(
      s"$entityName is a 34 character string that starts with ${prefixes.mkString(",")}"
    )

    sealed trait CreationException extends SidAbstract.CreationException

    // Follow implementations of CreationException should be final, but due to this issue:
    // https://github.com/scala/bug/issues/4440
    // that produces compile errors like:
    // `The outer reference in this type test cannot be checked at run time.`
    // So instead making them sealed, as that is practically the same, as they
    // are not extended withing this file.
    sealed case class ArgumentEmptyException()
        extends SidAbstract.ArgumentEmptyException(conformToString)
        with CreationException

    sealed case class ArgumentMissingPrefixException(argument: String)
        extends SidAbstract.ArgumentMissingPrefixException(argument, prefixes, conformToString)
        with CreationException

    sealed case class ArgumentLengthException(argument: String)
        extends SidAbstract.ArgumentLengthException(argument, conformToString)
        with CreationException

    /** Construct an instance of this Sid throwing exception on errors.
      *
      * @throws ArgumentEmptyException
      *   if the input is empty
      * @throws ArgumentMissingPrefixException
      *   if the input don't have the required prefix
      * @throws ArgumentLengthException
      *   if the input don't have the required lenght.
      */
    def unsafe(input: String): S = safe(input).toTry.get

    /** Construct an instance of this Sid returning either an error or the successfully result. */
    def safe(input: String): Either[CreationException, S] = {
      val isValidPrefix = prefixes.exists(prefix => input.startsWith(prefix.toString))
      if (input.isEmpty) Left(ArgumentEmptyException())
      else if (!isValidPrefix) Left(ArgumentMissingPrefixException(input))
      else if (input.length != 34) Left(ArgumentLengthException(input))
      else Right(instanceFactory(input))
    }

    // make apply do the same as safe for two reason:
    // 1. apply is the default constructor in a lot of cases, so might be what clients expect.
    // 2. If you make the constructor of the Sid subclass private, then you cannot lift
    //    the constuctor into a function when providing it SidCompanionObject constructor
    //    arguments. So typically you would make the constructor private[mostNarrowScopePossible],
    //    but when you do that, in generated apply method on the companion object, generated
    //    because it's a case class, would no longer be private, and hence be a way to
    //    create a instance without the needed validation. Specifying the apply method here,
    //    solves that problem, as no default one will then be generated.
    /** Construct an instance of this Sid returning either an error or the successfull result.
      *
      * Is just an alias for safe.
      */
    def apply(input: String): Either[CreationException, S] = safe(input)
  }
}
