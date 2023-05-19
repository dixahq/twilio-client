package com.dixa.twilio.client.iam

import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount

/** Allows you to modify the properties of an account.
  *
  * @see
  *   https://www.twilio.com/docs/iam/api/account#update-an-account-resource
  */
trait AccountUpdateRequestExecutor
    extends SingleRequestExecutor[
      AccountUpdateRequestExecutor.AccountUpdateRequest,
      AccountUpdateRequestExecutor.AccountUpdateException,
      TwilioAccount
    ] {

  import AccountUpdateRequestExecutor._

  override final protected type ApiExceptionWrapper = AccountUpdateException.Api

  override final protected type UnspecifiedException = AccountUpdateException.UnspecifiedError
}

object AccountUpdateRequestExecutor {

  /** Representation of the AccountUpdateRequest
    *
    * This request does not have any complex constraints, and it's therefore just a plain case
    * class, that you can just create instances of, without complex builders.
    */
  final case class AccountUpdateRequest(
      accountSid: TwilioAccount.Sid,
      friendlyName: Option[TwilioAccount.Name],
      status: Option[TwilioAccount.Status]
  )

  sealed trait AccountUpdateException extends RuntimeException
  object AccountUpdateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with AccountUpdateException

    final case class AccountNotFound(accountSid: TwilioAccount.Sid)
        extends RuntimeException(s"Account with sid $accountSid was not found.")
        with AccountUpdateException

    final case class ClosedAccountCannotBeReopened(accountSid: TwilioAccount.Sid)
        extends RuntimeException(
          s"Account with sid $accountSid is permanently closed and cannot be reopened"
        )
        with AccountUpdateException
    final case class UnspecifiedError(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to update account"
          ),
          cause.orNull
        )
        with AccountUpdateException
  }
}
