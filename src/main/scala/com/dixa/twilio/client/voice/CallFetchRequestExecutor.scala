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

package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Call

trait CallFetchRequestExecutor
    extends SingleRequestExecutor[
      CallFetchRequestExecutor.CallFetchRequest,
      CallFetchRequestExecutor.CallFetchException,
      Call,
      CallFetchRequestExecutor.CallFetchRequest.BuilderStartState
    ] {

  import CallFetchRequestExecutor._

  override final protected type ApiExceptionWrapper = CallFetchException.Api

  override final protected type UnspecifiedException = CallFetchException.Unspecified

  override protected def createBuilderStartState()
      : CallFetchRequestExecutor.CallFetchRequest.BuilderStartState =
    CallFetchRequestExecutor.CallFetchRequest.Builder.empty
}

object CallFetchRequestExecutor {

  sealed trait CallFetchRequest {
    def accountSid: TwilioAccount.Sid
    def sid: Call.Sid
  }

  private final case class CallFetchRequestImpl(
      accountSid: TwilioAccount.Sid,
      sid: Call.Sid
  ) extends CallFetchRequest

  object CallFetchRequest {

    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute extends RequestAttribute
    sealed trait RequestSidAttribute        extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute
      with RequestAccountSidAttribute
      with RequestSidAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[
        Attributes <: RequestAttribute
    ] private[CallFetchRequest] (
        accountSid: Option[TwilioAccount.Sid],
        sid: Option[Call.Sid]
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(Some(accountSid), sid)

      def withSid(
          sid: Call.Sid
      ): Builder[Attributes with RequestSidAttribute] =
        new Builder(accountSid, Some(sid))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): CallFetchRequest =
        CallFetchRequestImpl(accountSid.get, sid.get)
    }

    def build(fun: BuilderStartState => CallFetchRequest): CallFetchRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new BuilderStartState(None, None)
    }
  }

  sealed trait CallFetchException extends RuntimeException
  object CallFetchException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with CallFetchException
        with ApiExceptionWrapper

    final case class CallNotFound(accountSid: TwilioAccount.Sid, sid: Call.Sid)
        extends RuntimeException(s"Call with sid $sid was not found in account: $accountSid")
        with CallFetchException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch call"
          ),
          cause.orNull
        )
        with CallFetchException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
