package com.dixa.twilio.client.iam

import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.client.model.iam.TwilioAccount

trait AccountFetchRequestExecutor
    extends SingleRequestExecutor[
      AccountFetchRequestExecutor.AccountFetchRequest,
      AccountFetchRequestExecutor.AccountFetchException,
      TwilioAccount
    ] {

  import AccountFetchRequestExecutor._

  override final protected type ApiExceptionWrapper = AccountFetchException.Api

  override final protected type UnspecifiedException = AccountFetchException.UnspecifiedError
}

object AccountFetchRequestExecutor {

  final case class AccountFetchRequest(accountSid: TwilioAccount.Sid)

  sealed trait AccountFetchException extends RuntimeException
  object AccountFetchException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with AccountFetchException
    final case class UnspecifiedError(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch account"
          ),
          cause.orNull
        )
        with AccountFetchException
  }
}
