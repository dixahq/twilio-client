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

package com.dixa.twilio.client.phonenumber

import org.apache.pekko.Done
import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteException
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.OutgoingCallerId

trait OutgoingCallerIdDeleteRequestExecutor
    extends SingleRequestExecutor[
      OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteRequest,
      OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteException,
      Done,
      OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteRequest.Builder
    ] {
  override protected final type ApiExceptionWrapper = OutgoingCallerIdDeleteException.Api

  override protected final type UnspecifiedException = OutgoingCallerIdDeleteException.Unspecified

  override protected final def createBuilderStartState()
      : OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteRequest.Builder =
    OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteRequest.Builder.empty
}

object OutgoingCallerIdDeleteRequestExecutor {
  final case class OutgoingCallerIdDeleteRequest(
      accountSid: TwilioAccount.Sid,
      outGoingCallerId: OutgoingCallerId.Sid
  )
  object OutgoingCallerIdDeleteRequest {
    type BuilderStartState = Builder

    final class Builder private[phonenumber] (
        accountSid: Option[TwilioAccount.Sid],
        outGoingCallerId: Option[OutgoingCallerId.Sid]
    ) {
      def withAccountSid(accountSid: TwilioAccount.Sid): Builder =
        new Builder(Some(accountSid), outGoingCallerId)
      def withOutgoingCallerId(outGoingCallerId: OutgoingCallerId.Sid): Builder =
        new Builder(accountSid, Some(outGoingCallerId))
      def build(): OutgoingCallerIdDeleteRequest =
        OutgoingCallerIdDeleteRequest(accountSid.get, outGoingCallerId.get)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(None, None)
    }

    def build(
        fun: BuilderStartState => OutgoingCallerIdDeleteRequest
    ): OutgoingCallerIdDeleteRequest = fun(Builder.empty)
  }

  sealed trait OutgoingCallerIdDeleteException extends RuntimeException
  object OutgoingCallerIdDeleteException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with OutgoingCallerIdDeleteException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read incoming numbers"
          ),
          cause.orNull
        )
        with OutgoingCallerIdDeleteException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Exception) = this(None, Some(cause))
    }
  }
}
