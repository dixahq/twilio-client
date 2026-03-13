package com.dixa.twilio.model.general

import com.dixa.twilio.model.SidAbstract
import com.dixa.twilio.model.SidAbstract.Prefix

/** Represent a Service SID.
  *
  * It is a 34 character string that starts with IS.
  *
  * Used in Chat and Sync grants.
  */
final case class ServiceSid private[general] (override val toString: String) extends SidAbstract

object ServiceSid extends SidAbstract.SidCompanionObject(List(Prefix("IS")), new ServiceSid(_))
