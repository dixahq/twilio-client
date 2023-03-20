package com.dixa.twilio.model

import java.time.Instant
import java.time.format.DateTimeFormatter

trait Iso8601DateTime extends TwilioStringValue

object Iso8601DateTime {

  case class Before(instant: Instant) extends Iso8601DateTime {
    override def twilioString: String = s"<=${DateTimeFormatter.ISO_DATE_TIME.format(instant)}"
  }

  case class After(instant: Instant) extends Iso8601DateTime {
    override def twilioString: String = s">=${DateTimeFormatter.ISO_DATE_TIME.format(instant)}"
  }

}
