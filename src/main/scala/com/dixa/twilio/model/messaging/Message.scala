package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.SidAbstract
import com.dixa.twilio.model.SidAbstract.Prefix

object Message {
  final case class Sid private[Message] (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(List(Prefix("SM"), Prefix("MM")), new Sid(_))
}
