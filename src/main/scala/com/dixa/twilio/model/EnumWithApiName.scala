package com.dixa.twilio.model

import enumeratum.Enum
import org.scalactic.TypeCheckedTripleEquals._

/** Base trait for a Enum having an apiName value.
  *
  * Multiple enums in this library, is having a apiName value to represent what the enum entry's
  * String representation is when communicating with Twilio. This trait works as a base trait for
  * all of these enums, unifies the name of the value and how a enum value can be found from it.
  *
  * @tparam A
  *   Type of the EnumEntry. Must extends the special EnumEntry found in the companion object.
  */
trait EnumWithApiName[A <: EnumWithApiName.EnumEntry] extends Enum[A] {

  import EnumWithApiName._

  /** Return the value corresponding to provided apiName. Returns None if not found */
  def fromApiName(apiName: String): Option[A] = values.find(_.apiName === apiName)

  /** Return the value corresponding to provided apiName in a Right. Returns Left if not found */
  def fromApiNameEither(apiName: String): Either[ApiNameNotFoundException, A] =
    fromApiName(apiName).toRight(createApiNameNotFoundException(apiName))

  /** Return the value corresponding to provided apiName.
    *
    * @throws ApiNameNotFoundException
    *   if not found
    */
  def fromApiNameUnsafe(apiName: String): A =
    fromApiName(apiName).getOrElse(throw createApiNameNotFoundException(apiName))

  /** Return the value corresponding to provided apiName with case insensitive matching.
    *
    * Returns None if not found
    */
  def fromApiNameCaseInsensitive(apiName: String): Option[A] =
    values.find(_.apiName.equalsIgnoreCase(apiName))

  /** Return the value corresponding to provided apiName with case insensitive matching as a Right.
    *
    * Returns Left if not found
    */
  def fromApiNameCaseInsensitiveEither(apiName: String): Either[ApiNameNotFoundException, A] =
    fromApiNameCaseInsensitive(apiName).toRight(createApiNameNotFoundException(apiName))

  /** Return the value corresponding to provided apiName with case insensitive matching.
    *
    * @throws ApiNameNotFoundException
    *   if not found
    */
  def fromApiNameCaseInsensitiveUnsafe(apiName: String): A =
    fromApiNameCaseInsensitive(apiName).getOrElse(throw createApiNameNotFoundException(apiName))

  private def createApiNameNotFoundException(apiName: String) = ApiNameNotFoundException(
    s"$apiName is not a valid ${getClass.getSimpleName} value. Possible values are: $values"
  )

}

object EnumWithApiName {

  trait EnumEntry extends enumeratum.EnumEntry {
    def apiName: String
  }

  final case class ApiNameNotFoundException(msg: String) extends IllegalArgumentException(msg)
}
