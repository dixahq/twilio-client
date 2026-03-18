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

package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.messaging.TypingIndicatorSendRequestExecutor.TypingIndicatorSendException
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.FUnit
import com.dixa.twilio.model.messaging.Message

trait TypingIndicatorSendRequestExecutor
    extends SingleRequestExecutor[
      TypingIndicatorSendRequestExecutor.TypingIndicatorSendRequest,
      TypingIndicatorSendRequestExecutor.TypingIndicatorSendException,
      FUnit,
      TypingIndicatorSendRequestExecutor.TypingIndicatorSendRequest.Builder
    ] {

  override protected final type ApiExceptionWrapper = TypingIndicatorSendException.Api

  override protected final type UnspecifiedException = TypingIndicatorSendException.Unspecified

  override protected final def createBuilderStartState()
      : TypingIndicatorSendRequestExecutor.TypingIndicatorSendRequest.Builder =
    TypingIndicatorSendRequestExecutor.TypingIndicatorSendRequest.Builder.empty
}

object TypingIndicatorSendRequestExecutor {

  final case class TypingIndicatorSendRequest(
      messageSid: Message.Sid,
  )
  object TypingIndicatorSendRequest {
    type BuilderStartState = Builder

    final class Builder private[messaging] (
        messageSid: Option[Message.Sid]
    ) {
      def withMessageSid(messageSid: Message.Sid): Builder = new Builder(Some(messageSid))
      def build(): TypingIndicatorSendRequest = TypingIndicatorSendRequest(messageSid.get)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(None)
    }

    def build(fun: BuilderStartState => TypingIndicatorSendRequest): TypingIndicatorSendRequest =
      fun(Builder.empty)
  }

  sealed trait TypingIndicatorSendException extends RuntimeException
  object TypingIndicatorSendException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with TypingIndicatorSendException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to send a typing indicator"
          ),
          cause.orNull
        )
        with TypingIndicatorSendException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }
}
