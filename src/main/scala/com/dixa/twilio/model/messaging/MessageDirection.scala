package com.dixa.twilio.model.messaging

import com.dixa.twilio.model
import com.dixa.twilio.model.EnumWithTwilioString

import scala.collection.immutable

sealed abstract class MessageDirection(override val twilioString: String)
    extends EnumWithTwilioString.EnumEntry
object MessageDirection extends model.EnumWithTwilioString[MessageDirection] {
  override def values: immutable.IndexedSeq[MessageDirection] = findValues

  case object Inbound       extends MessageDirection("inbound")
  case object OutboundApi   extends MessageDirection("outbound-api")
  case object OutboundCall  extends MessageDirection("outbound-call")
  case object OutboundReply extends MessageDirection("outbound-reply")
}
