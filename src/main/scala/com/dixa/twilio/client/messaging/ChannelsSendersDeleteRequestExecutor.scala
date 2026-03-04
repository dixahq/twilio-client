package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.SingleRequestExecutor
import com.dixa.twilio.model.FUnit
import com.dixa.twilio.model.messaging.ChannelSender

trait ChannelsSendersDeleteRequestExecutor
    extends SingleRequestExecutor[
      ChannelsSendersDeleteRequestExecutor.ChannelSenderDeleteRequest,
      ChannelSenderException,
      FUnit,
      ChannelsSendersDeleteRequestExecutor.ChannelSenderDeleteRequest.Builder
    ] {

  override protected type ApiExceptionWrapper = ChannelSenderException.Api

  override protected type UnspecifiedException = ChannelSenderException.Unspecified

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
