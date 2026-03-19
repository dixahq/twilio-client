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

package com.dixa.twilio.client.general

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.FUnit
import com.dixa.twilio.model.general.Application
import com.dixa.twilio.model.iam.TwilioAccount

trait ApplicationDeleteRequestExecutor
    extends SingleRequestExecutor[
      ApplicationDeleteRequestExecutor.ApplicationDeleteRequest,
      ApplicationDeleteRequestExecutor.ApplicationDeleteException,
      FUnit,
      ApplicationDeleteRequestExecutor.ApplicationDeleteRequest.BuilderStartState
    ] {

  override protected type ApiExceptionWrapper =
    ApplicationDeleteRequestExecutor.ApplicationDeleteException.Api

  override protected type UnspecifiedException =
    ApplicationDeleteRequestExecutor.ApplicationDeleteException.Unspecified

  override protected def createBuilderStartState()
      : ApplicationDeleteRequestExecutor.ApplicationDeleteRequest.BuilderStartState =
    ApplicationDeleteRequestExecutor.ApplicationDeleteRequest.Builder.empty
}

object ApplicationDeleteRequestExecutor {

  sealed trait ApplicationDeleteRequest {
    def accountSid: TwilioAccount.Sid
    def sid: Application.Sid
  }

  object ApplicationDeleteRequest {

    type BuilderStartState = Builder[PhantomTypes.AccountSidSetFalse, PhantomTypes.SidSetFalse]

    /** Phantom types used to enforce compile time constraints on the Builder */
    object PhantomTypes {

      sealed trait AccountSidSet
      sealed trait AccountSidSetTrue  extends AccountSidSet
      sealed trait AccountSidSetFalse extends AccountSidSet

      sealed trait SidSet
      sealed trait SidSetTrue  extends SidSet
      sealed trait SidSetFalse extends SidSet
    }

    final class Builder[
        AccountSidSet <: PhantomTypes.AccountSidSet,
        SidSet <: PhantomTypes.SidSet
    ] private[ApplicationDeleteRequest] (
        accountSid: Option[TwilioAccount.Sid],
        sid: Option[Application.Sid]
    ) {

      private def copy[
          NewAccountSidSet <: PhantomTypes.AccountSidSet,
          NewSidSet <: PhantomTypes.SidSet
      ](
          accountSid: Option[TwilioAccount.Sid] = accountSid,
          sid: Option[Application.Sid] = sid
      ): Builder[
        NewAccountSidSet,
        NewSidSet
      ] =
        new Builder(
          accountSid,
          sid
        )

      /** The SID of the Account that will delete the resource. */
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[
        PhantomTypes.AccountSidSetTrue,
        SidSet
      ] =
        copy(accountSid = Some(accountSid))

      /** The SID of the application to delete */
      def withSid(sid: Application.Sid): Builder[AccountSidSet, PhantomTypes.SidSetTrue] =
        copy(sid = Some(sid))

      def build()(
          implicit accountSidSetEv: AccountSidSet =:= PhantomTypes.AccountSidSetTrue,
          sidSetEv: SidSet =:= PhantomTypes.SidSetTrue
      ): ApplicationDeleteRequest = RequestImpl(accountSid.get, sid.get)
    }

    object Builder {
      val empty = new BuilderStartState(None, None)
    }

    def build(fun: BuilderStartState => ApplicationDeleteRequest): ApplicationDeleteRequest =
      fun(Builder.empty)
  }

  private final case class RequestImpl(
      accountSid: TwilioAccount.Sid,
      sid: Application.Sid
  ) extends ApplicationDeleteRequest

  sealed trait ApplicationDeleteException extends RuntimeException

  object ApplicationDeleteException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ApplicationDeleteException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to delete application"
          ),
          cause.orNull
        )
        with ApplicationDeleteException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }

}
