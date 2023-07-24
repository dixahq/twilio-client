package com.dixa.twilio.model

import java.text.SimpleDateFormat

sealed trait Iso8601Date

object Iso8601Date {

  final case class Before(date: SimpleDateFormat) extends Iso8601Date

  final case class After(date: SimpleDateFormat) extends Iso8601Date
}
