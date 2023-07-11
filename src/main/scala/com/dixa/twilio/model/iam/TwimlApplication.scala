package com.dixa.twilio.model.iam

import com.dixa.twilio.model.SidAbstract
import com.dixa.twilio.model.SidAbstract.Prefix

object TwimlApplication {
  final case class Sid private (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject[Sid](List(Prefix("AP")), new Sid(_))
}
