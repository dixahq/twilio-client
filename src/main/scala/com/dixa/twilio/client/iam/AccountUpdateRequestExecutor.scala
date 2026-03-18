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
      TwilioAccount,
      AccountUpdateRequestExecutor.AccountUpdateRequest.Builder
    ] {

  import AccountUpdateRequestExecutor._

  override final protected type ApiExceptionWrapper = AccountUpdateException.Api

  override final protected type UnspecifiedException = AccountUpdateException.UnspecifiedError

  override final protected def createBuilderStartState(): AccountUpdateRequest.Builder =
    AccountUpdateRequest.Builder.empty
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
  object AccountUpdateRequest {
    type BuilderStartState = Builder

    final class Builder private[iam] (
        accountSid: Option[TwilioAccount.Sid],
        friendlyName: Option[TwilioAccount.Name],
        status: Option[TwilioAccount.Status]
    ) {
      def withAccountSid(accountSid: TwilioAccount.Sid): Builder =
        new Builder(Some(accountSid), friendlyName, status)
      def withFriendlyName(friendlyName: TwilioAccount.Name): Builder =
        new Builder(accountSid, Some(friendlyName), status)
      def withStatus(status: TwilioAccount.Status): Builder =
        new Builder(accountSid, friendlyName, Some(status))
      def build(): AccountUpdateRequest = AccountUpdateRequest(accountSid.get, friendlyName, status)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(None, None, None)
    }

    def build(fun: BuilderStartState => AccountUpdateRequest): AccountUpdateRequest =
      fun(Builder.empty)
  }

  sealed trait AccountUpdateException extends RuntimeException
  object AccountUpdateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with AccountUpdateException
        with ApiExceptionWrapper

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
