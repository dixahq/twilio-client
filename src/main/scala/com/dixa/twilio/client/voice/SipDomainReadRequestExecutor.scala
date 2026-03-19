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

package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.SipDomain

/** Read all applications (TwimlApps) from a subaccount.
  *
  * @see
  *   https://www.twilio.com/docs/usage/api/applications#read-multiple-application-resources
  */
trait SipDomainReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      SipDomainReadRequestExecutor.SipDomainReadRequest,
      SipDomainReadRequestExecutor.SipDomainReadException,
      SipDomain,
      SipDomainReadRequestExecutor.SipDomainReadRequest.BuilderStartState
    ] {

  override protected type ApiExceptionWrapper =
    SipDomainReadRequestExecutor.SipDomainReadException.Api

  override protected type UnspecifiedException =
    SipDomainReadRequestExecutor.SipDomainReadException.Unspecified

  override protected def createBuilderStartState()
      : SipDomainReadRequestExecutor.SipDomainReadRequest.BuilderStartState =
    SipDomainReadRequestExecutor.SipDomainReadRequest.Builder.empty
}

object SipDomainReadRequestExecutor {

  sealed trait SipDomainReadRequest {
    def accountSid: TwilioAccount.Sid
  }

  object SipDomainReadRequest {

    type BuilderStartState = Builder[PhantomTypes.AccountSidSetFalse]

    /** Phantom types used to enforce compile time constraints on the Builder */
    object PhantomTypes {

      sealed trait AccountSidSet
      sealed trait AccountSidSetTrue  extends AccountSidSet
      sealed trait AccountSidSetFalse extends AccountSidSet
    }

    final class Builder[
        AccountSidSet <: PhantomTypes.AccountSidSet
    ] private[SipDomainReadRequest] (
        accountSid: Option[TwilioAccount.Sid]
    ) {

      /** The SID of the Account that will read applications from. */
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[PhantomTypes.AccountSidSetTrue] =
        new Builder[PhantomTypes.AccountSidSetTrue](Some(accountSid))

      def build()(
          implicit accountSidSetEv: AccountSidSet =:= PhantomTypes.AccountSidSetTrue
      ): SipDomainReadRequest = RequestImpl(accountSid.get)
    }

    object Builder {
      val empty = new BuilderStartState(None)
    }

    def build(fun: BuilderStartState => SipDomainReadRequest): SipDomainReadRequest =
      fun(Builder.empty)
  }

  private final case class RequestImpl(accountSid: TwilioAccount.Sid) extends SipDomainReadRequest

  sealed trait SipDomainReadException extends RuntimeException

  object SipDomainReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with SipDomainReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to riad applications"
          ),
          cause.orNull
        )
        with SipDomainReadException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }

}
