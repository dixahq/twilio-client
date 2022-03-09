package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.EnumWithApiName

import scala.collection.immutable

sealed abstract class MessageStatus(val apiName: String) extends EnumWithApiName.EnumEntry
object MessageStatus extends EnumWithApiName[MessageStatus] {
  override val values: immutable.IndexedSeq[MessageStatus] = findValues

  case object Accepted    extends MessageStatus("accepted")
  case object Scheduled   extends MessageStatus("scheduled")
  case object Canceled    extends MessageStatus("canceled")
  case object Queued      extends MessageStatus("queued")
  case object Sending     extends MessageStatus("sending")
  case object Sent        extends MessageStatus("sent")
  case object Failed      extends MessageStatus("failed")
  case object Delivered   extends MessageStatus("delivered")
  case object Undelivered extends MessageStatus("undelivered")
  case object Receiving   extends MessageStatus("receiving")
  case object Received    extends MessageStatus("received")

  /** WhatsApp only */
  case object Read extends MessageStatus("read")
}
