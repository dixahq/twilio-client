package com.dixa.twilio.model.messaging

import com.dixa.twilio.model
import com.dixa.twilio.model.EnumWithApiName

import scala.collection.immutable

sealed abstract class MessageDirection(val apiName: String) extends EnumWithApiName.EnumEntry
object MessageDirection extends model.EnumWithApiName[MessageDirection] {
  override def values: immutable.IndexedSeq[MessageDirection] = findValues

  case object Inbound       extends MessageDirection("inbound")
  case object OutboundApi   extends MessageDirection("outbound-api")
  case object OutboundCall  extends MessageDirection("outbound-call")
  case object OutboundReply extends MessageDirection("outbound-reply")
}
