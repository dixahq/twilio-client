// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
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

/** Fetch a single account for an account sid.
  *
  * @see
  *   https://www.twilio.com/docs/iam/api/account#fetch-an-account-resource
  */
trait AccountFetchRequestExecutor
    extends SingleRequestExecutor[
      AccountFetchRequestExecutor.AccountFetchRequest,
      AccountFetchRequestExecutor.AccountFetchException,
      TwilioAccount,
      AccountFetchRequestExecutor.AccountFetchRequest.Builder
    ] {

  import AccountFetchRequestExecutor._

  override final protected type ApiExceptionWrapper = AccountFetchException.Api

  override final protected type UnspecifiedException = AccountFetchException.UnspecifiedError

  override final protected def createBuilderStartState(): AccountFetchRequest.Builder =
    AccountFetchRequest.Builder.empty
}

object AccountFetchRequestExecutor {

  final case class AccountFetchRequest(accountSid: TwilioAccount.Sid)
  object AccountFetchRequest {
    type BuilderStartState = Builder

    final class Builder private[iam] (accountSid: Option[TwilioAccount.Sid]) {
      def withAccountSid(accountSid: TwilioAccount.Sid): Builder = new Builder(Some(accountSid))
      def build(): AccountFetchRequest = AccountFetchRequest(accountSid.get)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(None)
    }

    def build(fun: BuilderStartState => AccountFetchRequest): AccountFetchRequest =
      fun(Builder.empty)
  }

  sealed trait AccountFetchException extends RuntimeException
  object AccountFetchException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with AccountFetchException
        with ApiExceptionWrapper
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
