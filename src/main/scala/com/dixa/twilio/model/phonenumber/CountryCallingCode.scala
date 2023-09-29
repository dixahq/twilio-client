package com.dixa.twilio.model.phonenumber

case class CountryCallingCode(toInt: Int) {
  override def toString: String = toInt.toString
}
