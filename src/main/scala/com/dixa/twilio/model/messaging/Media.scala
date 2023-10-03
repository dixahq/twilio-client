package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.SidAbstract
import com.dixa.twilio.model.SidAbstract.Prefix

object Media {

  final case class Sid private[Media] (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(List(Prefix("ME")), new Sid(_))
}
