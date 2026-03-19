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
import enumeratum._

import scala.collection.immutable

trait ChannelsSendersListRequestExecutor
    extends SingleRequestExecutor[
      ChannelsSendersListRequestExecutor.ChannelSendersListRequest,
      ChannelSenderException,
      ChannelsSendersListRequestExecutor.ChannelSendersListResponse,
      ChannelsSendersListRequestExecutor.ChannelSendersListRequest.Builder
    ] {

  override protected type ApiExceptionWrapper = ChannelSenderException.Api

  override protected type UnspecifiedException = ChannelSenderException.Unspecified

  override protected def createBuilderStartState()
      : ChannelsSendersListRequestExecutor.ChannelSendersListRequest.Builder =
    ChannelsSendersListRequestExecutor.ChannelSendersListRequest.Builder.empty
}

object ChannelsSendersListRequestExecutor {

  sealed abstract class Channel(val value: String) extends EnumEntry
  object Channel                                   extends Enum[Channel] {
    override val values: immutable.IndexedSeq[Channel] = findValues
    case object Whatsapp extends Channel("whatsapp")
  }

  final case class ChannelSendersListRequest(
      channel: Channel = Channel.Whatsapp,
      pageSize: Option[Int] = None
  )
  object ChannelSendersListRequest {
    type BuilderStartState = Builder

    final class Builder private[messaging] (channel: Option[Channel], pageSize: Option[Int]) {
      def withChannel(channel: Channel): Builder = new Builder(Some(channel), pageSize)
      def withPageSize(pageSize: Int): Builder   = new Builder(channel, Some(pageSize))
      def build(): ChannelSendersListRequest     =
        ChannelSendersListRequest(channel.getOrElse(Channel.Whatsapp), pageSize)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(None, None)
    }

    def build(
        fun: BuilderStartState => ChannelSendersListRequest
    ): ChannelSendersListRequest = fun(Builder.empty)
  }

  final case class ChannelSendersListResponse(
      senders: List[ChannelSender]
  )
}
