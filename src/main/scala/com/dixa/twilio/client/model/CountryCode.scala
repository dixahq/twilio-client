package com.dixa.twilio.client.model

final case class CountryCode(toInt: Int) extends AnyVal {
  override def toString: String = toInt.toString
}
