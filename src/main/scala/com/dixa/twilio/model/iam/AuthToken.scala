package com.dixa.twilio.model.iam

/** Represent a AuthToken
  *
  * There is a total of 3 implementations of AuthToken:
  *
  *   - [[AuthToken.Primary]]
  *   - [[AuthToken.Secondary]]
  *   - [[AuthToken.UnknownType]]
  *
  * The first two represent the two known types of AuthTokens that exists in Twilio. Because of
  * that, they are also subtypes of [[AuthToken.KnownType]].
  *
  * [[AuthToken.UnknownType]] can be used in cases where you don't know, or don't care what kind of
  * auth token you are dealing with.
  */
sealed trait AuthToken {

  /** String representation of the auth token */
  def asString: String

  /** Will always return AuthToken.{ImplementationName}(***) to not accidentally log auth tokens */
  override final def toString: String = s"AuthToken.${getClass.getSimpleName}(***)"
}

object AuthToken {

  def unapply(arg: AuthToken): Option[String] = Some(arg.asString)

  sealed trait KnownType extends AuthToken

  final case class Primary(override val asString: String) extends KnownType

  final case class Secondary(override val asString: String) extends KnownType

  final case class UnknownType(override val asString: String) extends AuthToken
}
