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

import com.dixa.twilio.client.SingleRequestExecutor
import com.dixa.twilio.model.messaging._

trait ChannelsSendersCreateRequestExecutor
    extends SingleRequestExecutor[
      ChannelsSendersCreateRequestExecutor.ChannelSendersCreateRequest,
      ChannelSendersException,
      ChannelSender,
      ChannelsSendersCreateRequestExecutor.ChannelSendersCreateRequest.BuilderStartState
    ] {

  override protected type ApiExceptionWrapper = ChannelSendersException.Api

  override protected type UnspecifiedException = ChannelSendersException.Unspecified

  override protected def createBuilderStartState()
      : ChannelsSendersCreateRequestExecutor.ChannelSendersCreateRequest.BuilderStartState =
    ChannelsSendersCreateRequestExecutor.ChannelSendersCreateRequest.Builder.empty
}

object ChannelsSendersCreateRequestExecutor {

  sealed trait ChannelSendersCreateRequest {
    def senderId: MessageSender
    def configuration: ChannelSender.Configuration
    def webhooks: ChannelSender.Webhooks
    def profile: ChannelSender.Profile
  }

  private final case class ChannelSendersCreateRequestImpl(
      senderId: MessageSender,
      configuration: ChannelSender.Configuration,
      webhooks: ChannelSender.Webhooks,
      profile: ChannelSender.Profile
  ) extends ChannelSendersCreateRequest

  object ChannelSendersCreateRequest {

    sealed trait RequestAttribute
    sealed trait RequestSenderIdAttribute      extends RequestAttribute
    sealed trait RequestConfigurationAttribute extends RequestAttribute
    sealed trait RequestWebhooksAttribute      extends RequestAttribute
    sealed trait RequestProfileAttribute       extends RequestAttribute

    type RequestRequiredAttributes =
      RequestAttribute
        with RequestSenderIdAttribute
        with RequestConfigurationAttribute
        with RequestWebhooksAttribute
        with RequestProfileAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[
        Attributes <: RequestAttribute
    ] private[ChannelSendersCreateRequest] (
        senderId: Option[MessageSender],
        configuration: Option[ChannelSender.Configuration],
        webhooks: Option[ChannelSender.Webhooks],
        profile: Option[ChannelSender.Profile]
    ) {
      def withSenderId(
          senderId: MessageSender
      ): Builder[Attributes with RequestSenderIdAttribute] =
        new Builder(Some(senderId), configuration, webhooks, profile)

      def withConfiguration(
          configuration: ChannelSender.Configuration
      ): Builder[Attributes with RequestConfigurationAttribute] =
        new Builder(senderId, Some(configuration), webhooks, profile)

      def withWebhooks(
          webhooks: ChannelSender.Webhooks
      ): Builder[Attributes with RequestWebhooksAttribute] =
        new Builder(senderId, configuration, Some(webhooks), profile)

      def withProfile(
          profile: ChannelSender.Profile
      ): Builder[Attributes with RequestProfileAttribute] =
        new Builder(senderId, configuration, webhooks, Some(profile))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): ChannelSendersCreateRequest =
        ChannelSendersCreateRequestImpl(senderId.get, configuration.get, webhooks.get, profile.get)
    }

    object Builder {
      val empty: BuilderStartState = new Builder(None, None, None, None)
    }

    def build(
        fun: BuilderStartState => ChannelSendersCreateRequest
    ): ChannelSendersCreateRequest = fun(Builder.empty)
  }
}
