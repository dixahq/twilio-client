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
import com.dixa.twilio.model.FUnit
import com.dixa.twilio.model.messaging.ChannelSender

trait ChannelsSendersDeleteRequestExecutor
    extends SingleRequestExecutor[
      ChannelsSendersDeleteRequestExecutor.ChannelSenderDeleteRequest,
      ChannelsSendersCommonExceptions,
      FUnit,
      ChannelsSendersDeleteRequestExecutor.ChannelSenderDeleteRequest.Builder
    ] {

  override protected type ApiExceptionWrapper = ChannelsSendersCommonExceptions.Api

  override protected type UnspecifiedException = ChannelsSendersCommonExceptions.Unspecified

  override protected def createBuilderStartState()
      : ChannelsSendersDeleteRequestExecutor.ChannelSenderDeleteRequest.Builder =
    ChannelsSendersDeleteRequestExecutor.ChannelSenderDeleteRequest.Builder.empty
}

object ChannelsSendersDeleteRequestExecutor {

  final case class ChannelSenderDeleteRequest(
      channelSenderSid: ChannelSender.Sid
  )
  object ChannelSenderDeleteRequest {
    type BuilderStartState = Builder

    final class Builder private[messaging] (channelSenderSid: Option[ChannelSender.Sid]) {
      def withChannelSenderSid(channelSenderSid: ChannelSender.Sid): Builder =
        new Builder(Some(channelSenderSid))
      def build(): ChannelSenderDeleteRequest = ChannelSenderDeleteRequest(channelSenderSid.get)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(None)
    }

    def build(
        fun: BuilderStartState => ChannelSenderDeleteRequest
    ): ChannelSenderDeleteRequest = fun(Builder.empty)
  }
}
