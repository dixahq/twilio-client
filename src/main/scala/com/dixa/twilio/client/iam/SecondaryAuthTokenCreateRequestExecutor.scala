package com.dixa.twilio.client.iam

import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.AuthToken

trait SecondaryAuthTokenCreateRequestExecutor
    extends SingleRequestExecutor[
      SecondaryAuthTokenCreateRequestExecutor.SecondaryAuthTokenCreateRequest,
      SecondaryAuthTokenCreateRequestExecutor.SecondaryAuthTokenCreateException,
      AuthToken.AuthTokenAndMetaData[AuthToken.Secondary]
    ] {

  import SecondaryAuthTokenCreateRequestExecutor._

  override final protected type ApiExceptionWrapper = SecondaryAuthTokenCreateException.Api

  override final protected type UnspecifiedException =
    SecondaryAuthTokenCreateException.UnspecifiedError
}

object SecondaryAuthTokenCreateRequestExecutor {

  final case class SecondaryAuthTokenCreateRequest()

  sealed trait SecondaryAuthTokenCreateException extends RuntimeException
  object SecondaryAuthTokenCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with SecondaryAuthTokenCreateException
    final case class UnspecifiedError(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to create secondary auth token."
          ),
          cause.orNull
        )
        with SecondaryAuthTokenCreateException
  }
}
