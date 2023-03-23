package com.dixa.twilio.model.voice

import com.dixa.twilio.model.SidAbstract
import com.dixa.twilio.model.SidAbstract.Prefix

sealed trait Group {
  def sid: Group.Sid
}

object Group {
  final case class Sid private[Group] (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(Prefix("GP"), new Sid(_))
}
