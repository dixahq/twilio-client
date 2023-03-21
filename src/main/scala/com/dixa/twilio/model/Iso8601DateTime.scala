package com.dixa.twilio.model

trait Iso8601DateTime

object Iso8601DateTime {

  case class Before(instant: java.time.Instant) extends Iso8601DateTime

  case class After(instant: java.time.Instant) extends Iso8601DateTime
}
