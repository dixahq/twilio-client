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

package com.dixa.twilio.client.iam

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.{AccessToken, TwilioGrant}

import scala.concurrent.duration.FiniteDuration

/** AccessToken creation doesn't follow the standard Twilio REST API request/response pattern as it
  * is generated locally using API Key credentials.
  */
trait AccessTokenCreateRequestExecutor
    extends SingleRequestExecutor[
      AccessTokenCreateRequestExecutor.AccessTokenCreateRequest,
      AccessTokenCreateRequestExecutor.AccessTokenCreateException,
      AccessToken,
      AccessTokenCreateRequestExecutor.AccessTokenCreateRequest.BuilderStartState
    ] {

  override protected type ApiExceptionWrapper =
    AccessTokenCreateRequestExecutor.AccessTokenCreateException.Api

  override protected type UnspecifiedException =
    AccessTokenCreateRequestExecutor.AccessTokenCreateException.Unspecified

  override protected def createBuilderStartState()
      : AccessTokenCreateRequestExecutor.AccessTokenCreateRequest.BuilderStartState =
    AccessTokenCreateRequestExecutor.AccessTokenCreateRequest.Builder.empty
}

object AccessTokenCreateRequestExecutor {

  sealed trait AccessTokenCreateRequest {
    def identity: String
    def grants: Seq[TwilioGrant]
    def ttl: FiniteDuration
  }

  object AccessTokenCreateRequest {
    object PhantomTypes {
      sealed trait RequestAttribute
      sealed trait RequestIdentityAttribute extends RequestAttribute
    }

    type RequestRequiredAttributes =
      PhantomTypes.RequestAttribute with PhantomTypes.RequestIdentityAttribute

    type BuilderStartState = Builder[PhantomTypes.RequestAttribute]

    final class Builder[Attributes <: PhantomTypes.RequestAttribute] private (
        identity: Option[String],
        grants: Seq[TwilioGrant],
        ttl: FiniteDuration
    ) {

      /** Identity is a shared identifier that applies across all grants in the access token. It
        * associates the access token with a specific user, and all the grants assigned to the
        * access token operate under that identity.
        */
      def withIdentity(
          identity: String
      ): Builder[Attributes with PhantomTypes.RequestIdentityAttribute] =
        new Builder(
          Some(identity),
          grants,
          ttl
        )

      def withGrants(grants: Seq[TwilioGrant]): Builder[Attributes] = new Builder(
        identity,
        grants,
        ttl
      )

      def addGrant(grant: TwilioGrant): Builder[Attributes] = new Builder(
        identity,
        grants :+ grant,
        ttl
      )

      /** The time-to-live (TTL) of an Access Token, i.e. the duration for which the token is valid
        * after it is generated. Must be between 1 and 86400 seconds (24 hours). More than 24 hours
        * will make SDK throw error. More info: https://www.twilio.com/docs/iam/access-tokens
        */
      def withTtl(ttl: FiniteDuration): Builder[Attributes] = new Builder(
        identity,
        grants,
        ttl
      )

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): AccessTokenCreateRequest = {
        RequestImpl(identity.get, grants, ttl)
      }
    }

    object Builder {
      import scala.concurrent.duration._
      val empty: BuilderStartState = new Builder(None, Nil, 1.hour)
    }

    def build(fun: BuilderStartState => AccessTokenCreateRequest): AccessTokenCreateRequest =
      fun(Builder.empty)
  }

  private final case class RequestImpl(
      identity: String,
      grants: Seq[TwilioGrant],
      ttl: FiniteDuration
  ) extends AccessTokenCreateRequest

  sealed trait AccessTokenCreateException extends RuntimeException

  object AccessTokenCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with AccessTokenCreateException
        with ApiExceptionWrapper

    final case class InvalidTtl(ttlSeconds: Long)
        extends RuntimeException(s"ttl must be between 1 and 86400 seconds, got $ttlSeconds")
        with AccessTokenCreateException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse("Unspecified error happened trying to create access token"),
          cause.orNull
        )
        with AccessTokenCreateException

    object Unspecified {
      def apply(msg: String): Unspecified      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable): Unspecified =
        new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
