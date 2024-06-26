package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.SingleRequestExecutor
import com.dixa.twilio.model.messaging._

trait ChannelSenderCreateRequestExecutor
    extends SingleRequestExecutor[
      ChannelSenderCreateRequestExecutor.ChannelSenderCreateRequest,
      ChannelSenderException,
      ChannelSender
    ] {

  override protected type ApiExceptionWrapper = ChannelSenderException.Api

  override protected type UnspecifiedException = ChannelSenderException.Unspecified
}

object ChannelSenderCreateRequestExecutor {

  final case class ChannelSenderCreateRequest(
      senderId: MessageRecipient,
      configuration: ChannelSender.Configuration,
      webhooks: ChannelSender.Webhooks,
      profile: ChannelSender.Profile,
  )
}
