package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.SingleRequestExecutor
import com.dixa.twilio.model.messaging._

trait ChannelsSendersFetchRequestExecutor
    extends SingleRequestExecutor[
      ChannelsSendersFetchRequestExecutor.ChannelSenderFetchRequest,
      ChannelSenderException,
      ChannelSender,
      ChannelsSendersFetchRequestExecutor.ChannelSenderFetchRequest.Builder
    ] {

  override protected type ApiExceptionWrapper = ChannelSenderException.Api

  override protected type UnspecifiedException = ChannelSenderException.Unspecified

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
