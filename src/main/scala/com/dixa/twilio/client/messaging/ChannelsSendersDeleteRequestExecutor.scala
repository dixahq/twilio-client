package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.SingleRequestExecutor
import com.dixa.twilio.model.FUnit
import com.dixa.twilio.model.messaging.ChannelSender

trait ChannelsSendersDeleteRequestExecutor
    extends SingleRequestExecutor[
      ChannelsSendersDeleteRequestExecutor.ChannelSenderDeleteRequest,
      ChannelSenderException,
      FUnit
    ] {

  override protected type ApiExceptionWrapper = ChannelSenderException.Api

  override protected type UnspecifiedException = ChannelSenderException.Unspecified

}

object ChannelsSendersDeleteRequestExecutor {

  final case class ChannelSenderDeleteRequest(
      channelSenderSid: ChannelSender.Sid
  )
}
