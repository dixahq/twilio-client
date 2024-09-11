package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.SingleRequestExecutor
import com.dixa.twilio.model.messaging._

trait ChannelsSendersFetchRequestExecutor
    extends SingleRequestExecutor[
      ChannelsSendersFetchRequestExecutor.ChannelSenderFetchRequest,
      ChannelSenderException,
      ChannelSender
    ] {

  override protected type ApiExceptionWrapper = ChannelSenderException.Api

  override protected type UnspecifiedException = ChannelSenderException.Unspecified
}

object ChannelsSendersFetchRequestExecutor {

  final case class ChannelSenderFetchRequest(
      channelSenderSid: ChannelSender.Sid,
  )
}
