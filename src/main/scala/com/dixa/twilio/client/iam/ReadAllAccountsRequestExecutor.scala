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
import com.dixa.twilio.client.iam.ReadAllAccountsRequestExecutor.ReadAllAccountsException
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount

/** Read all Twilio accounts
  *
  * @see
  *   https://www.twilio.com/docs/iam/api/account#read-multiple-account-resources
  */
trait ReadAllAccountsRequestExecutor
    extends MultipleResponseRequestExecutor[
      ReadAllAccountsRequestExecutor.ReadAllAccountsRequest,
      ReadAllAccountsRequestExecutor.ReadAllAccountsException,
      TwilioAccount,
      ReadAllAccountsRequestExecutor.ReadAllAccountsRequest.Builder
    ] {

  override protected final type ApiExceptionWrapper = ReadAllAccountsException.Api

  override protected final type UnspecifiedException = ReadAllAccountsException.Unspecified

  override protected final def createBuilderStartState()
      : ReadAllAccountsRequestExecutor.ReadAllAccountsRequest.Builder =
    ReadAllAccountsRequestExecutor.ReadAllAccountsRequest.Builder.empty
}

object ReadAllAccountsRequestExecutor {

  /** Request for reading all accounts
    *
    * @param status
    *   If Some(status) then only fetch accounts of that status. If None no status filtering is
    *   applied.
    * @param name
    *   if Some(name) is provided, then only return accounts that friendly name match this value
    *   exactly. If None is supplied then no filtering is done on name.
    */
  final case class ReadAllAccountsRequest(
      status: Option[TwilioAccount.Status] = None,
      name: Option[TwilioAccount.Name] = None
  )
  object ReadAllAccountsRequest {
    type BuilderStartState = Builder

    final class Builder private[iam] (
        status: Option[TwilioAccount.Status],
        name: Option[TwilioAccount.Name]
    ) {
      def withStatus(status: TwilioAccount.Status): Builder =
        new Builder(Some(status), name)
      def withName(name: TwilioAccount.Name): Builder =
        new Builder(status, Some(name))
      def build(): ReadAllAccountsRequest = ReadAllAccountsRequest(status, name)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(None, None)
    }

    def build(fun: BuilderStartState => ReadAllAccountsRequest): ReadAllAccountsRequest =
      fun(Builder.empty)
  }

  sealed trait ReadAllAccountsException extends RuntimeException
  object ReadAllAccountsException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ReadAllAccountsException
        with ApiExceptionWrapper

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
