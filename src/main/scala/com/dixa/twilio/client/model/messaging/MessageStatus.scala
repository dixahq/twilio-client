package com.dixa.twilio.client.model.messaging

import scala.collection.immutable

sealed abstract class MessageStatus(val twilioApiName: String) extends enumeratum.EnumEntry
object MessageStatus extends enumeratum.Enum[MessageStatus] {
  override def values: immutable.IndexedSeq[MessageStatus] = findValues

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
  case object Read        extends MessageStatus("read") // whatsapp only
}
