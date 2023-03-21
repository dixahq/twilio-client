package com.dixa.twilio.model

import java.time.format.DateTimeFormatter

trait Iso8601DateTime extends TwilioStringValue

object Iso8601DateTime {

  case class Before(instant: java.time.Instant)(
      implicit formatter: DateTimeFormatter
  ) extends Iso8601DateTime {
    override def twilioString: String = s"<=${formatter.format(instant)}"
  }

  case class After(instant: java.time.Instant)(
      implicit formatter: DateTimeFormatter
  ) extends Iso8601DateTime {
    override def twilioString: String = s">=${formatter.format(instant)}"
  }
}
