package com.dixa.twilio.model.voice

import com.dixa.twilio.model.SidAbstract

object ByocTrunk {

  final case class Sid private[ByocTrunk] (override val toString: String) extends SidAbstract
  object Sid extends SidAbstract.SidCompanionObject[Sid](List(SidAbstract.Prefix("BY")), new Sid(_))

}
