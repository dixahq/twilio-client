package com.dixa.twilio.model.voice

import com.dixa.twilio.model.SidAbstract
import com.dixa.twilio.model.SidAbstract.Prefix

sealed trait Trunk {
  def sid: Trunk.Sid
}

object Trunk {
  final case class Sid private[Trunk] (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(Prefix("TK"), new Sid(_))
}
