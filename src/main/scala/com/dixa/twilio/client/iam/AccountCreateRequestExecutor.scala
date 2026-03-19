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

/** See [[com.dixa.twilio.client.iam.TwilioClientIam.accountCreate]] */
trait AccountCreateRequestExecutor
    extends SingleRequestExecutor[
      AccountCreateRequestExecutor.AccountCreateRequest,
      AccountCreateRequestExecutor.AccountCreateException,
      TwilioAccount,
      AccountCreateRequestExecutor.AccountCreateRequest.Builder
    ] {

  override protected type ApiExceptionWrapper =
    AccountCreateRequestExecutor.AccountCreateException.Api

  override protected type UnspecifiedException =
    AccountCreateRequestExecutor.AccountCreateException.Unspecified

  override protected def createBuilderStartState()
      : AccountCreateRequestExecutor.AccountCreateRequest.Builder =
    AccountCreateRequestExecutor.AccountCreateRequest.Builder.empty
}

object AccountCreateRequestExecutor {

  sealed trait AccountCreateRequest {
    def friendlyName: Option[TwilioAccount.Name]
  }

  object AccountCreateRequest {

    type BuilderStartState = Builder

    final class Builder private[AccountCreateRequest] (friendlyName: Option[TwilioAccount.Name]) {

      /** A human readable description of the account to create.
        *
        * Defaults to SubAccount Created at {YYYY-MM-DD HH:MM meridian}
        */
      def withFriendlyName(friendlyName: TwilioAccount.Name): Builder = new Builder(
        Some(friendlyName)
      )

      def build(): AccountCreateRequest = RequestImpl(friendlyName)
    }

    object Builder {
      val empty = new BuilderStartState(None)
    }

    def build(fun: BuilderStartState => AccountCreateRequest): AccountCreateRequest =
      fun(Builder.empty)
  }

  private final case class RequestImpl(friendlyName: Option[TwilioAccount.Name])
      extends AccountCreateRequest

  sealed trait AccountCreateException extends RuntimeException

  object AccountCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with AccountCreateException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to create account"
          ),
          cause.orNull
        )
        with AccountCreateException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }

}
