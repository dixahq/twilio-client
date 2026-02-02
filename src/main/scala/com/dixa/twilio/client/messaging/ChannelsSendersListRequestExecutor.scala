package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.SingleRequestExecutor
import com.dixa.twilio.model.messaging._
import enumeratum._

import scala.collection.immutable

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

  sealed abstract class Channel(val value: String) extends EnumEntry
  object Channel                                   extends Enum[Channel] {
    override val values: immutable.IndexedSeq[Channel] = findValues
    case object Whatsapp extends Channel("whatsapp")
  }

  final case class ChannelSendersListRequest(
      channel: Channel = Channel.Whatsapp,
      pageSize: Option[Int] = None
  )

  final case class ChannelSendersListResponse(
      senders: List[ChannelSender]
  )
}
