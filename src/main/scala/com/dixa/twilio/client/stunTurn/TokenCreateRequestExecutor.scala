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

package com.dixa.twilio.client.stunTurn

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.stunTurn.Token

/** Create a Network Traversal Service Token.
  *
  * @see
  *   https://www.twilio.com/docs/stun-turn/api
  */
trait TokenCreateRequestExecutor
    extends SingleRequestExecutor[
      TokenCreateRequestExecutor.TokenCreateRequest,
      TokenCreateRequestExecutor.TokenCreateException,
      Token,
      TokenCreateRequestExecutor.TokenCreateRequest.BuilderStartState
    ] {

  override protected type ApiExceptionWrapper =
    TokenCreateRequestExecutor.TokenCreateException.Api

  override protected type UnspecifiedException =
    TokenCreateRequestExecutor.TokenCreateException.Unspecified

  override protected def createBuilderStartState()
      : TokenCreateRequestExecutor.TokenCreateRequest.BuilderStartState =
    TokenCreateRequestExecutor.TokenCreateRequest.Builder.empty
}

object TokenCreateRequestExecutor {

  sealed trait TokenCreateRequest {
    def accountSid: TwilioAccount.Sid
  }

  object TokenCreateRequest {

    type BuilderStartState = Builder[PhantomTypes.AccountSidSetFalse]

    /** Phantom types used to enforce compile time constraints on the Builder */
    object PhantomTypes {

      sealed trait AccountSidSet
      sealed trait AccountSidSetTrue  extends AccountSidSet
      sealed trait AccountSidSetFalse extends AccountSidSet
    }

    final class Builder[AccountSidSet <: PhantomTypes.AccountSidSet] private[TokenCreateRequest] (
        accountSid: Option[TwilioAccount.Sid]
    ) {

      /** The SID of the Account that will create the resource. */
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[PhantomTypes.AccountSidSetTrue] =
        new Builder[PhantomTypes.AccountSidSetTrue](Some(accountSid))

      def build()(
          implicit accountSidSetEv: AccountSidSet =:= PhantomTypes.AccountSidSetTrue
      ): TokenCreateRequest = RequestImpl(accountSid.get)
    }

    object Builder {
      val empty = new BuilderStartState(None)
    }

    def build(fun: BuilderStartState => TokenCreateRequest): TokenCreateRequest =
      fun(Builder.empty)
  }

  private final case class RequestImpl(
      accountSid: TwilioAccount.Sid
  ) extends TokenCreateRequest

  sealed trait TokenCreateException extends RuntimeException

  object TokenCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with TokenCreateException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to create Token"
          ),
          cause.orNull
        )
        with TokenCreateException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }

}
