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
import com.dixa.twilio.client.iam.ApiKeyDeleteRequestExecutor.{KeyDeleteException, KeyDeleteRequest}
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.FUnit
import com.dixa.twilio.model.iam.{ApiKey, TwilioAccount}

/** Delete a Twilio API key.
  *
  * @see
  *   https://www.twilio.com/docs/iam/api-keys/key-resource-v1
  */
trait ApiKeyDeleteRequestExecutor
    extends SingleRequestExecutor[
      KeyDeleteRequest,
      KeyDeleteException,
      FUnit,
      KeyDeleteRequest.BuilderStartState
    ] {

  override protected final type ApiExceptionWrapper  = KeyDeleteException.Api
  override protected final type UnspecifiedException = KeyDeleteException.Unspecified

  override protected def createBuilderStartState(): KeyDeleteRequest.BuilderStartState =
    KeyDeleteRequest.Builder.empty
}

object ApiKeyDeleteRequestExecutor {

  sealed trait KeyDeleteRequest {
    def accountSid: TwilioAccount.Sid
    def sid: ApiKey.Sid
  }

  private final case class KeyDeleteRequestImpl(
      accountSid: TwilioAccount.Sid,
      sid: ApiKey.Sid
  ) extends KeyDeleteRequest

  object KeyDeleteRequest {

    object PhantomTypes {
      sealed trait RequestAttribute
      sealed trait RequestAccountSidAttribute extends RequestAttribute
      sealed trait RequestSidAttribute        extends RequestAttribute
    }

    type RequestRequiredAttributes =
      PhantomTypes.RequestAttribute
        with PhantomTypes.RequestAccountSidAttribute
        with PhantomTypes.RequestSidAttribute

    type BuilderStartState = Builder[PhantomTypes.RequestAttribute]

    final class Builder[Attributes <: PhantomTypes.RequestAttribute] private[KeyDeleteRequest] (
        accountSid: Option[TwilioAccount.Sid],
        sid: Option[ApiKey.Sid]
    ) {
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with PhantomTypes.RequestAccountSidAttribute] =
        new Builder(Some(accountSid), sid)

      def withSid(
          sid: ApiKey.Sid
      ): Builder[Attributes with PhantomTypes.RequestSidAttribute] =
        new Builder(accountSid, Some(sid))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): KeyDeleteRequest =
        KeyDeleteRequestImpl(accountSid.get, sid.get)
    }

    def build(fun: BuilderStartState => KeyDeleteRequest): KeyDeleteRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new BuilderStartState(None, None)
    }
  }

  sealed trait KeyDeleteException extends RuntimeException

  object KeyDeleteException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with KeyDeleteException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse("Unspecified error happened trying to delete API key"),
          cause.orNull
        )
        with KeyDeleteException

    object Unspecified {
      def apply(msg: String): Unspecified      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable): Unspecified =
        new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
