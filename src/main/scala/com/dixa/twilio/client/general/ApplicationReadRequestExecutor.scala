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
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.general.Application
import com.dixa.twilio.model.iam.TwilioAccount

/** Read all applications (TwimlApps) from a subaccount.
  *
  * @see
  *   https://www.twilio.com/docs/usage/api/applications#read-multiple-application-resources
  */
trait ApplicationReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      ApplicationReadRequestExecutor.ApplicationReadRequest,
      ApplicationReadRequestExecutor.ApplicationReadException,
      Application,
      ApplicationReadRequestExecutor.ApplicationReadRequest.BuilderStartState
    ] {

  override protected type ApiExceptionWrapper =
    ApplicationReadRequestExecutor.ApplicationReadException.Api

  override protected type UnspecifiedException =
    ApplicationReadRequestExecutor.ApplicationReadException.Unspecified

  override protected def createBuilderStartState()
      : ApplicationReadRequestExecutor.ApplicationReadRequest.BuilderStartState =
    ApplicationReadRequestExecutor.ApplicationReadRequest.Builder.empty
}

object ApplicationReadRequestExecutor {

  sealed trait ApplicationReadRequest {
    def accountSid: TwilioAccount.Sid

    def friendlyName: Option[Application.FriendlyName]
  }

  object ApplicationReadRequest {

    type BuilderStartState = Builder[PhantomTypes.AccountSidSetFalse]

    /** Phantom types used to enforce compile time constraints on the Builder */
    object PhantomTypes {

      sealed trait AccountSidSet
      sealed trait AccountSidSetTrue  extends AccountSidSet
      sealed trait AccountSidSetFalse extends AccountSidSet
    }

    final class Builder[
        AccountSidSet <: PhantomTypes.AccountSidSet
    ] private[ApplicationReadRequest] (
        accountSid: Option[TwilioAccount.Sid],
        friendlyName: Option[Application.FriendlyName]
    ) {

      /** The SID of the Account that will read applications from. */
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[PhantomTypes.AccountSidSetTrue] =
        new Builder[PhantomTypes.AccountSidSetTrue](Some(accountSid), friendlyName)

      def withFriendlyName(friendlyName: Application.FriendlyName): Builder[AccountSidSet] =
        new Builder[AccountSidSet](accountSid, Some(friendlyName))

      def build()(
          implicit accountSidSetEv: AccountSidSet =:= PhantomTypes.AccountSidSetTrue
      ): ApplicationReadRequest = RequestImpl(accountSid.get, friendlyName)
    }

    object Builder {
      val empty = new BuilderStartState(None, None)
    }

    def build(fun: BuilderStartState => ApplicationReadRequest): ApplicationReadRequest =
      fun(Builder.empty)
  }

  private final case class RequestImpl(
      accountSid: TwilioAccount.Sid,
      friendlyName: Option[Application.FriendlyName]
  ) extends ApplicationReadRequest

  sealed trait ApplicationReadException extends RuntimeException

  object ApplicationReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ApplicationReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to riad applications"
          ),
          cause.orNull
        )
        with ApplicationReadException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }

}
