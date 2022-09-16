package com.dixa.twilio.client.iam

import com.dixa.twilio.client.iam.ReadAllAccountsRequestExecutor.ReadAllAccountsException
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount

trait ReadAllAccountsRequestExecutor
    extends MultipleResponseRequestExecutor[
      ReadAllAccountsRequestExecutor.ReadAllAccountsRequest,
      ReadAllAccountsRequestExecutor.ReadAllAccountsException,
      TwilioAccount
    ] {

  override protected final type ApiExceptionWrapper = ReadAllAccountsException.Api

  override protected final type UnspecifiedException = ReadAllAccountsException.Unspecified
}

object ReadAllAccountsRequestExecutor {

  /** Request for reading all accounts
    *
    * @param status
    *   If Some(status) then only fetch accounts of that status. If None no status filtering is
    *   applied.
    */
  final case class ReadAllAccountsRequest(
      status: Option[TwilioAccount.Status]
  )

  sealed trait ReadAllAccountsException extends RuntimeException
  object ReadAllAccountsException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ReadAllAccountsException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch accounts"
          ),
          cause.orNull
        )
        with ReadAllAccountsException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Exception) = this(None, Some(cause))
    }
  }
}
