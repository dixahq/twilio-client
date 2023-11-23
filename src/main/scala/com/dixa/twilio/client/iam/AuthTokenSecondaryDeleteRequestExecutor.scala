package com.dixa.twilio.client.iam

import org.apache.pekko.Done
import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}

trait AuthTokenSecondaryDeleteRequestExecutor
    extends SingleRequestExecutor[
      AuthTokenSecondaryDeleteRequestExecutor.AuthTokenSecondaryDeleteRequest,
      AuthTokenSecondaryDeleteRequestExecutor.AuthTokenSecondaryDeleteException,
      Done
    ] {

  import AuthTokenSecondaryDeleteRequestExecutor._

  override final protected type ApiExceptionWrapper = AuthTokenSecondaryDeleteException.Api

  override final protected type UnspecifiedException =
    AuthTokenSecondaryDeleteException.UnspecifiedError
}

object AuthTokenSecondaryDeleteRequestExecutor {

  final case class AuthTokenSecondaryDeleteRequest()

  sealed trait AuthTokenSecondaryDeleteException extends RuntimeException
  object AuthTokenSecondaryDeleteException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with AuthTokenSecondaryDeleteException
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
        with AuthTokenSecondaryDeleteException

    final case class UnspecifiedError(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to delete secondary auth token."
          ),
          cause.orNull
        )
        with AuthTokenSecondaryDeleteException
  }
}
