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

trait ChannelsSendersFetchRequestExecutor
    extends SingleRequestExecutor[
      ChannelsSendersFetchRequestExecutor.ChannelSenderFetchRequest,
      ChannelsSendersCommonExceptions,
      ChannelSender,
      ChannelsSendersFetchRequestExecutor.ChannelSenderFetchRequest.Builder
    ] {

  override protected type ApiExceptionWrapper = ChannelsSendersCommonExceptions.Api

  override protected type UnspecifiedException = ChannelsSendersCommonExceptions.Unspecified

  override protected def createBuilderStartState()
      : ChannelsSendersFetchRequestExecutor.ChannelSenderFetchRequest.Builder =
    ChannelsSendersFetchRequestExecutor.ChannelSenderFetchRequest.Builder.empty
}

object ChannelsSendersFetchRequestExecutor {

  final case class ChannelSenderFetchRequest(
      channelSenderSid: ChannelSender.Sid
  )
  object ChannelSenderFetchRequest {
    type BuilderStartState = Builder

    final class Builder private[messaging] (channelSenderSid: Option[ChannelSender.Sid]) {
      def withChannelSenderSid(channelSenderSid: ChannelSender.Sid): Builder =
        new Builder(Some(channelSenderSid))
      def build(): ChannelSenderFetchRequest = ChannelSenderFetchRequest(channelSenderSid.get)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(None)
    }

    def build(
        fun: BuilderStartState => ChannelSenderFetchRequest
    ): ChannelSenderFetchRequest = fun(Builder.empty)
  }
}
