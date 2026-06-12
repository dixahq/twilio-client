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
import com.dixa.twilio.client.messaging.ChannelsSendersCreateRequestExecutor.ChannelsSendersException
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.messaging._

trait ChannelsSendersCreateRequestExecutor
    extends SingleRequestExecutor[
      ChannelsSendersCreateRequestExecutor.ChannelsSendersCreateRequest,
      ChannelsSendersException,
      ChannelSender,
      ChannelsSendersCreateRequestExecutor.ChannelsSendersCreateRequest.BuilderStartState
    ] {

  override protected type ApiExceptionWrapper = ChannelsSendersException.Api

  override protected type UnspecifiedException = ChannelsSendersException.Unspecified

  override protected def createBuilderStartState()
      : ChannelsSendersCreateRequestExecutor.ChannelsSendersCreateRequest.BuilderStartState =
    ChannelsSendersCreateRequestExecutor.ChannelsSendersCreateRequest.Builder.empty
}

object ChannelsSendersCreateRequestExecutor {

  sealed trait ChannelsSendersCreateRequest {
    def senderId: MessageSender
    def configuration: ChannelSender.Configuration
    def webhooks: ChannelSender.Webhooks
    def profile: ChannelSender.Profile
  }

  private final case class ChannelsSendersCreateRequestImpl(
      senderId: MessageSender,
      configuration: ChannelSender.Configuration,
      webhooks: ChannelSender.Webhooks,
      profile: ChannelSender.Profile
  ) extends ChannelsSendersCreateRequest

  object ChannelsSendersCreateRequest {

    object PhantomTypes {
      sealed trait RequestAttribute
      sealed trait RequestSenderIdAttribute      extends RequestAttribute
      sealed trait RequestConfigurationAttribute extends RequestAttribute
      sealed trait RequestWebhooksAttribute      extends RequestAttribute
      sealed trait RequestProfileAttribute       extends RequestAttribute
    }

    type RequestRequiredAttributes =
      PhantomTypes.RequestAttribute
        with PhantomTypes.RequestSenderIdAttribute
        with PhantomTypes.RequestConfigurationAttribute
        with PhantomTypes.RequestWebhooksAttribute
        with PhantomTypes.RequestProfileAttribute

    type BuilderStartState = Builder[PhantomTypes.RequestAttribute]

    final class Builder[
        Attributes <: PhantomTypes.RequestAttribute
    ] private[ChannelsSendersCreateRequest] (
        senderId: Option[MessageSender],
        configuration: Option[ChannelSender.Configuration],
        webhooks: Option[ChannelSender.Webhooks],
        profile: Option[ChannelSender.Profile]
    ) {
      def withSenderId(
          senderId: MessageSender
      ): Builder[Attributes with PhantomTypes.RequestSenderIdAttribute] =
        new Builder(Some(senderId), configuration, webhooks, profile)

      def withConfiguration(
          configuration: ChannelSender.Configuration
      ): Builder[Attributes with PhantomTypes.RequestConfigurationAttribute] =
        new Builder(senderId, Some(configuration), webhooks, profile)

      def withWebhooks(
          webhooks: ChannelSender.Webhooks
      ): Builder[Attributes with PhantomTypes.RequestWebhooksAttribute] =
        new Builder(senderId, configuration, Some(webhooks), profile)

      def withProfile(
          profile: ChannelSender.Profile
      ): Builder[Attributes with PhantomTypes.RequestProfileAttribute] =
        new Builder(senderId, configuration, webhooks, Some(profile))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): ChannelsSendersCreateRequest =
        ChannelsSendersCreateRequestImpl(senderId.get, configuration.get, webhooks.get, profile.get)
    }

    object Builder {
      val empty: BuilderStartState = new Builder(None, None, None, None)
    }

    def build(
        fun: BuilderStartState => ChannelsSendersCreateRequest
    ): ChannelsSendersCreateRequest = fun(Builder.empty)
  }

  sealed trait ChannelsSendersException extends RuntimeException
  object ChannelsSendersException {
    final case class TwilioInternalError(
        errorCode: Option[Long],
        errorMessage: Option[String],
        moreInfo: Option[String],
        rawResponse: String
    ) extends RuntimeException(
          s"Twilio internal error (${errorCode.getOrElse("unknown")}): ${errorMessage.getOrElse("unknown")} - ${moreInfo.getOrElse("")}"
        )
        with ChannelsSendersException

    final case class CouldNotExtendCreditLine(
        wabaId: Option[String],
        apiMsg: String,
        apiLink: String
    ) extends RuntimeException(
          s"Could not extend credit line for WABA: ${wabaId.getOrElse("WABA ID Not found")} - ${apiMsg} - ${apiLink}"
        )
        with ChannelsSendersException

    final case class SenderIdAlreadyExists(senderId: String, apiMsg: String, apiLink: String)
        extends RuntimeException(
          s"SenderId already exists: $senderId - ${apiMsg} - ${apiLink}"
        )
        with ChannelsSendersException

    final case class ChannelSenderNotSupported(sender: String)
        extends RuntimeException(s"Channel sender is not supported: $sender")
        with ChannelsSendersException

    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ChannelsSendersException
        with ApiExceptionWrapper

    final case class ParseFailure(
        rawResponse: String,
        msg: String,
        cause: Option[Throwable]
    ) extends RuntimeException(
          s"$msg - with full raw response: $rawResponse",
          cause.orNull
        )
        with ChannelsSendersException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened when trying to create channel sender"
          ),
          cause.orNull
        )
        with ChannelsSendersException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
