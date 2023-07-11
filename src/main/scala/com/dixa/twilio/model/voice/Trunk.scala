package com.dixa.twilio.model.voice

import com.dixa.twilio.model.{SidAbstract, TwilioStringValue}
import com.dixa.twilio.model.SidAbstract.Prefix

sealed trait Trunk {
  def sid: Trunk.Sid
}

object Trunk {
  final case class Sid private[Trunk] (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(List(Prefix("TK")), new Sid(_))

  final case class Username(override val toString: String) extends TwilioStringValue

  final case class Password(asString: String) extends TwilioStringValue {
    override val toString: String = "TrunkPassword(***)"
  }
}
