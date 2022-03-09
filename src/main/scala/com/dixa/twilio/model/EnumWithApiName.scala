package com.dixa.twilio.model

import enumeratum.Enum
import org.scalactic.TypeCheckedTripleEquals._

trait EnumWithApiName[A <: EnumWithApiName.EnumEntry] extends Enum[A] {

  def fromApiName(apiName: String): Option[A] = values.find(_.apiName === apiName)

  def fromApiNameEither(apiName: String): Either[EnumWithApiName.ApiNameNotFoundException, A] =
    fromApiName(apiName).toRight(createApiNameNotFoundException(apiName))

  def fromApiNameUnsafe(apiName: String): A =
    fromApiName(apiName).getOrElse(throw createApiNameNotFoundException(apiName))

  def fromApiNameCaceInsensitive(apiName: String): Option[A] =
    values.find(_.apiName.equalsIgnoreCase(apiName))

  def fromApiNameCaceInsensitiveEither(
      apiName: String
  ): Either[EnumWithApiName.ApiNameNotFoundException, A] =
    fromApiNameCaceInsensitive(apiName).toRight(createApiNameNotFoundException(apiName))

  def fromApiNameCaceInsensitiveUnsafe(apiName: String): A =
    fromApiNameCaceInsensitive(apiName).getOrElse(throw createApiNameNotFoundException(apiName))

  private def createApiNameNotFoundException(apiName: String) =
    EnumWithApiName.ApiNameNotFoundException(
      s"$apiName is not a valid ${getClass.getSimpleName} value. Possible values are: $values"
    )

}

object EnumWithApiName {

  trait EnumEntry extends enumeratum.EnumEntry {
    def apiName: String
  }

  final case class ApiNameNotFoundException(msg: String) extends IllegalArgumentException(msg)
}
