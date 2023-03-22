package com.dixa.twilio.model

sealed trait Iso8601DateTime

object Iso8601DateTime {

  final case class Before(instant: java.time.Instant) extends Iso8601DateTime

  final case class After(instant: java.time.Instant) extends Iso8601DateTime
}
