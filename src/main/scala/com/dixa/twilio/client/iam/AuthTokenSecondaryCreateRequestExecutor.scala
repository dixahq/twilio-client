package com.dixa.twilio.client.iam

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.AuthToken

trait AuthTokenSecondaryCreateRequestExecutor
    extends SingleRequestExecutor[
      AuthTokenSecondaryCreateRequestExecutor.AuthTokenSecondaryCreateRequest,
      AuthTokenSecondaryCreateRequestExecutor.AuthTokenSecondaryCreateException,
      AuthToken.AuthTokenAndMetaData[AuthToken.Secondary],
      AuthTokenSecondaryCreateRequestExecutor.AuthTokenSecondaryCreateRequest.Builder
    ] {

  import AuthTokenSecondaryCreateRequestExecutor._

  override final protected type ApiExceptionWrapper = AuthTokenSecondaryCreateException.Api

  override final protected type UnspecifiedException =
    AuthTokenSecondaryCreateException.UnspecifiedError

  override final protected def createBuilderStartState(): AuthTokenSecondaryCreateRequest.Builder =
    AuthTokenSecondaryCreateRequest.Builder.empty
}

object AuthTokenSecondaryCreateRequestExecutor {

  final case class AuthTokenSecondaryCreateRequest()
  object AuthTokenSecondaryCreateRequest {
    type BuilderStartState = Builder

    final class Builder private[iam] () {
      def build(): AuthTokenSecondaryCreateRequest = AuthTokenSecondaryCreateRequest()
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState()
    }

    def build(
        fun: BuilderStartState => AuthTokenSecondaryCreateRequest
    ): AuthTokenSecondaryCreateRequest = fun(Builder.empty)
  }

  sealed trait AuthTokenSecondaryCreateException extends RuntimeException
  object AuthTokenSecondaryCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with AuthTokenSecondaryCreateException
        with ApiExceptionWrapper

    /** Exceptions for cases where creating secondary auth tokens via API is not enabled on account.
      *
      * In such cases Twilio will return a 404 status. You can contact Twilio support to get them to
      * activate the API call.
      */
    final case class ApiCallNotEnabledOnAccountException()
        extends RuntimeException(
          "API for creating secondary auth token is not enabled on this account. Contact Twilio to get it enabled."
        )
        with AuthTokenSecondaryCreateException

    final case class SecondaryAuthTokenAlreadyExistsException()
        extends RuntimeException("A secondary auth token already exists on this account")
        with AuthTokenSecondaryCreateException

    final case class UnspecifiedError(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to create secondary auth token."
          ),
          cause.orNull
        )
        with AuthTokenSecondaryCreateException
    object UnspecifiedError {
      def apply(msg: String): UnspecifiedError  = new UnspecifiedError(Some(msg), None)
      def apply(t: Throwable): UnspecifiedError = new UnspecifiedError(None, Some(t))
    }
  }
}
