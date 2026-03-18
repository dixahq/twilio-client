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

package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.client.messaging.MessageResourceReadRequestExecutor.MessageResourceReadException
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging._

import java.time.Instant

trait MessageResourceReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      MessageResourceReadRequestExecutor.MessageResourceReadRequest,
      MessageResourceReadRequestExecutor.MessageResourceReadException,
      MessageResource,
      MessageResourceReadRequestExecutor.MessageResourceReadRequest.Builder
    ] {

  override protected type ApiExceptionWrapper = MessageResourceReadException.Api

  override protected type UnspecifiedException = MessageResourceReadException.Unspecified

  override protected def createBuilderStartState()
      : MessageResourceReadRequestExecutor.MessageResourceReadRequest.Builder =
    MessageResourceReadRequestExecutor.MessageResourceReadRequest.Builder.empty
}

object MessageResourceReadRequestExecutor {

  final case class MessageResourceReadRequest(
      accountSid: TwilioAccount.Sid,
      filter: MessageResourcesReadRequestFilter = MessageResourcesReadRequestFilter()
  )
  object MessageResourceReadRequest {
    type BuilderStartState = Builder

    final class Builder private[messaging] (
        accountSid: Option[TwilioAccount.Sid],
        filter: MessageResourcesReadRequestFilter
    ) {
      def withAccountSid(accountSid: TwilioAccount.Sid): Builder =
        new Builder(Some(accountSid), filter)
      def withFilter(filter: MessageResourcesReadRequestFilter): Builder =
        new Builder(accountSid, filter)
      def build(): MessageResourceReadRequest = MessageResourceReadRequest(accountSid.get, filter)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(
        None,
        MessageResourcesReadRequestFilter()
      )
    }

    def build(fun: BuilderStartState => MessageResourceReadRequest): MessageResourceReadRequest =
      fun(Builder.empty)
  }

  final case class MessageResourcesReadRequestFilter(
      to: Option[MessageRecipient] = None,
      from: Option[MessageSender] = None,
      dateSentAfter: Option[Instant] = None,
      dateSentBefore: Option[Instant] = None,
      pageSize: Int = 20
  )

  // TODO: msf - Figure out is Exceptions are valid for this request
  sealed trait MessageResourceReadException extends RuntimeException
  object MessageResourceReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with MessageResourceReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch sms resources"
          ),
          cause.orNull
        )
        with MessageResourceReadException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }

}
