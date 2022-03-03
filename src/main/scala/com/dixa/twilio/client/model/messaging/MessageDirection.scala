package com.dixa.twilio.client.model.messaging

import scala.collection.immutable

sealed abstract class MessageDirection(val twilioApiName: String) extends enumeratum.EnumEntry
object MessageDirection extends enumeratum.Enum[MessageDirection] {
  override def values: immutable.IndexedSeq[MessageDirection] = findValues

  case object Inbound       extends MessageDirection("inbound")
  case object OutboundApi   extends MessageDirection("outbound-api")
  case object OutboundCall  extends MessageDirection("outbound-call")
  case object OutboundReply extends MessageDirection("outbound-reply")
}
