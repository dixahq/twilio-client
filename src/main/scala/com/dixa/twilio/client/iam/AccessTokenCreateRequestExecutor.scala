package com.dixa.twilio.client.iam

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.Region
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
    def region: Option[Region]
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
        region: Option[Region],
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
          region,
          grants,
          ttl
        )

      def withRegion(region: Region): Builder[Attributes] = new Builder(
        identity,
        Some(region),
        grants,
        ttl
      )

      def withGrants(grants: Seq[TwilioGrant]): Builder[Attributes] = new Builder(
        identity,
        region,
        grants,
        ttl
      )

      def addGrant(grant: TwilioGrant): Builder[Attributes] = new Builder(
        identity,
        region,
        grants :+ grant,
        ttl
      )

      def withTtl(ttl: FiniteDuration): Builder[Attributes] = new Builder(
        identity,
        region,
        grants,
        ttl
      )

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): AccessTokenCreateRequest = {
        RequestImpl(identity.get, region, grants, ttl)
      }
    }

    object Builder {
      import scala.concurrent.duration._
      val empty: BuilderStartState = new Builder(None, None, Nil, 1.hour)
    }

    def build(fun: BuilderStartState => AccessTokenCreateRequest): AccessTokenCreateRequest =
      fun(Builder.empty)
  }

  private final case class RequestImpl(
      identity: String,
      region: Option[Region],
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
