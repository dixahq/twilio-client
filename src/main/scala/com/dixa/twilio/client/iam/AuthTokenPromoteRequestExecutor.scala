package com.dixa.twilio.client.iam

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.AuthToken

trait AuthTokenPromoteRequestExecutor
    extends SingleRequestExecutor[
      AuthTokenPromoteRequestExecutor.AuthTokenPromoteRequest,
      AuthTokenPromoteRequestExecutor.AuthTokenPromoteException,
      AuthToken.AuthTokenAndMetaData[AuthToken.Primary]
    ] {

  import AuthTokenPromoteRequestExecutor._

  override final protected type ApiExceptionWrapper = AuthTokenPromoteException.Api

  override final protected type UnspecifiedException =
    AuthTokenPromoteException.UnspecifiedError
}

object AuthTokenPromoteRequestExecutor {

  final case class AuthTokenPromoteRequest()

  sealed trait AuthTokenPromoteException extends RuntimeException
  object AuthTokenPromoteException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with AuthTokenPromoteException
        with ApiExceptionWrapper

    /** Exceptions for when the account has no secondary token.
      *
      * Note that this can also mean that you just don't have access to using this API on the
      * account.
      */
    final case class SecondaryAuthTokenNotFoundOnAccountException()
        extends RuntimeException(
          s"Account has no secondary token to delete (or the API is not enabled on this account)."
        )
        with AuthTokenPromoteException

    final case class UnspecifiedError(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to promote auth token."
          ),
          cause.orNull
        )
        with AuthTokenPromoteException
  }
}
