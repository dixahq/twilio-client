package com.dixa.twilio.model.iam

sealed trait AuthToken {

  def asString: String

  /** Will always return AuthToken.{ImplementationName}(***) to not accidentially log auth tokens */
  override final def toString: String = s"AuthToken.${getClass.getSimpleName}(***)"
}

object AuthToken {

  def unapply(arg: AuthToken): Option[String] = Some(arg.asString)

  sealed trait KnownType extends AuthToken

  final case class Primary(override val asString: String) extends KnownType

  final case class Secondary(override val asString: String) extends KnownType

  final case class UnknownType(override val asString: String) extends AuthToken
}
