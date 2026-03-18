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
import com.dixa.twilio.client.messaging.MessageMediaResourceReadRequestExecutor.MessageMediaResourceReadException
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.messaging.{MediaResourceReference, Message}

trait MessageMediaResourceReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      MessageMediaResourceReadRequestExecutor.MessageMediaResourceReadRequest,
      MessageMediaResourceReadRequestExecutor.MessageMediaResourceReadException,
      MediaResourceReference,
      MessageMediaResourceReadRequestExecutor.MessageMediaResourceReadRequest.Builder
    ] {
  override protected final type ApiExceptionWrapper = MessageMediaResourceReadException.Api

  override protected final type UnspecifiedException = MessageMediaResourceReadException.Unspecified

  override protected final def createBuilderStartState()
      : MessageMediaResourceReadRequestExecutor.MessageMediaResourceReadRequest.Builder =
    MessageMediaResourceReadRequestExecutor.MessageMediaResourceReadRequest.Builder.empty
}

object MessageMediaResourceReadRequestExecutor {
  final case class MessageMediaResourceReadRequest(
      messageSid: Message.Sid
  )
  object MessageMediaResourceReadRequest {
    type BuilderStartState = Builder

    final class Builder private[messaging] (messageSid: Option[Message.Sid]) {
      def withMessageSid(messageSid: Message.Sid): Builder = new Builder(Some(messageSid))
      def build(): MessageMediaResourceReadRequest = MessageMediaResourceReadRequest(messageSid.get)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(None)
    }

    def build(
        fun: BuilderStartState => MessageMediaResourceReadRequest
    ): MessageMediaResourceReadRequest = fun(Builder.empty)
  }

  sealed trait MessageMediaResourceReadException extends RuntimeException
  object MessageMediaResourceReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with MessageMediaResourceReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read message resources"
          ),
          cause.orNull
        )
        with MessageMediaResourceReadException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Exception) = this(None, Some(cause))
    }
  }
}
