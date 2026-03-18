// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.model.iam
import java.time.Instant

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

  /** Represent the known types of auth tokens in twilio.
    *
    * See [[AuthToken]] for details.
    */
  sealed trait KnownType extends AuthToken

  /** Represent a Primary auth token.
    *
    * See [[AuthToken]] for details.
    */
  final case class Primary(override val asString: String) extends KnownType

  /** Represent a Secondary auth token
    *
    * See [[AuthToken]] for details.
    */
  final case class Secondary(override val asString: String) extends KnownType

  /** Represent a auth token in cases where the type is unknown.
    *
    * See [[AuthToken]] for details.
    */
  final case class UnknownType(override val asString: String) extends AuthToken

  /** Metadata about a AuthToken
    *
    * Twilio often combine a AuthToken with some metadata, and this class represent that metadata.
    */
  final case class MetaData(
      accountSid: TwilioAccount.Sid,
      createdTime: Instant,
      updatedTime: Instant
  )

  /** Wrapper class that combines a AuthToken instance with it's metadata. */
  final case class AuthTokenAndMetaData[A <: AuthToken](authToken: A, metaData: MetaData)
}
