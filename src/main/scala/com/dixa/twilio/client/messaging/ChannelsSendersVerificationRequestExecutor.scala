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
import com.dixa.twilio.client.messaging.ChannelsSendersVerificationRequestExecutor.ChannelSenderVerificationException
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.messaging._

trait ChannelsSendersVerificationRequestExecutor
    extends SingleRequestExecutor[
      ChannelsSendersVerificationRequestExecutor.ChannelSenderVerificationRequest,
      ChannelsSendersVerificationRequestExecutor.ChannelSenderVerificationException,
      Unit,
      ChannelsSendersVerificationRequestExecutor.ChannelSenderVerificationRequest.Builder
    ] {

  override protected type ApiExceptionWrapper = ChannelSenderVerificationException.Api

  override protected type UnspecifiedException = ChannelSenderVerificationException.Unspecified

  override protected def createBuilderStartState()
      : ChannelsSendersVerificationRequestExecutor.ChannelSenderVerificationRequest.Builder =
    ChannelsSendersVerificationRequestExecutor.ChannelSenderVerificationRequest.Builder.empty
}

object ChannelsSendersVerificationRequestExecutor {

  final case class ChannelSenderVerificationRequest(
      senderSid: ChannelSender.Sid,
      verificationCode: ChannelSender.VerificationCodeConfiguration
  )
  object ChannelSenderVerificationRequest {
    type BuilderStartState = Builder

    final class Builder private[messaging] (
        senderSid: Option[ChannelSender.Sid],
        verificationCode: Option[ChannelSender.VerificationCodeConfiguration]
    ) {
      def withSenderSid(senderSid: ChannelSender.Sid): Builder =
        new Builder(Some(senderSid), verificationCode)
      def withVerificationCode(
          verificationCode: ChannelSender.VerificationCodeConfiguration
      ): Builder =
        new Builder(senderSid, Some(verificationCode))
      def build(): ChannelSenderVerificationRequest =
        ChannelSenderVerificationRequest(senderSid.get, verificationCode.get)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(None, None)
    }

    def build(
        fun: BuilderStartState => ChannelSenderVerificationRequest
    ): ChannelSenderVerificationRequest = fun(Builder.empty)
  }

  sealed trait ChannelSenderVerificationException extends RuntimeException
  object ChannelSenderVerificationException {

    final case class ChannelNotSupported(channel: String)
        extends RuntimeException(s"Channel are not supported: $channel")
        with ChannelSenderVerificationException
    final case class ParseFailure(msg: String)
        extends RuntimeException(msg)
        with ChannelSenderVerificationException
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ChannelSenderVerificationException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch channel sender resource"
          ),
          cause.orNull
        )
        with ChannelSenderVerificationException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }

}
