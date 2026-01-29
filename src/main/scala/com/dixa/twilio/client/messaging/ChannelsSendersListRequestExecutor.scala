package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.SingleRequestExecutor
import com.dixa.twilio.model.messaging._

trait ChannelsSendersListRequestExecutor
    extends SingleRequestExecutor[
      ChannelsSendersListRequestExecutor.ChannelSendersListRequest,
      ChannelSenderException,
      ChannelsSendersListRequestExecutor.ChannelSendersListResponse
    ] {

  override protected type ApiExceptionWrapper = ChannelSenderException.Api

  override protected type UnspecifiedException = ChannelSenderException.Unspecified
}

object ChannelsSendersListRequestExecutor {

  sealed trait Channel {
    def value: String
  }
  object Channel {
    case object Whatsapp extends Channel { val value = "whatsapp" }
  }

  final case class ChannelSendersListRequest(
      channel: Channel = Channel.Whatsapp,
      pageSize: Option[Int] = None
  )

  final case class ChannelSendersListResponse(
      senders: List[ChannelSender]
  )
}
