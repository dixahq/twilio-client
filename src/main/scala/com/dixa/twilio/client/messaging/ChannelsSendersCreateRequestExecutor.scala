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
      ChannelsSendersCreateRequestExecutor.ChannelSenderCreateRequest,
      ChannelSenderException,
      ChannelSender,
      ChannelsSendersCreateRequestExecutor.ChannelSenderCreateRequest.Builder
    ] {

  override protected type ApiExceptionWrapper = ChannelSenderException.Api

  override protected type UnspecifiedException = ChannelSenderException.Unspecified

  override protected def createBuilderStartState()
      : ChannelsSendersCreateRequestExecutor.ChannelSenderCreateRequest.Builder =
    ChannelsSendersCreateRequestExecutor.ChannelSenderCreateRequest.Builder.empty
}

object ChannelsSendersCreateRequestExecutor {

  final case class ChannelSenderCreateRequest(
      senderId: MessageRecipient,
      configuration: ChannelSender.Configuration,
      webhooks: ChannelSender.Webhooks,
      profile: ChannelSender.Profile
  )
  object ChannelSenderCreateRequest {
    type BuilderStartState = Builder

    final class Builder private[messaging] (
        senderId: Option[MessageRecipient],
        configuration: Option[ChannelSender.Configuration],
        webhooks: Option[ChannelSender.Webhooks],
        profile: Option[ChannelSender.Profile]
    ) {
      def withSenderId(senderId: MessageRecipient): Builder =
        new Builder(Some(senderId), configuration, webhooks, profile)
      def withConfiguration(configuration: ChannelSender.Configuration): Builder =
        new Builder(senderId, Some(configuration), webhooks, profile)
      def withWebhooks(webhooks: ChannelSender.Webhooks): Builder =
        new Builder(senderId, configuration, Some(webhooks), profile)
      def withProfile(profile: ChannelSender.Profile): Builder =
        new Builder(senderId, configuration, webhooks, Some(profile))
      def build(): ChannelSenderCreateRequest =
        ChannelSenderCreateRequest(senderId.get, configuration.get, webhooks.get, profile.get)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(None, None, None, None)
    }

    def build(
        fun: BuilderStartState => ChannelSenderCreateRequest
    ): ChannelSenderCreateRequest = fun(Builder.empty)
  }
}
