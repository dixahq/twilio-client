package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.Iso4127CountryCode

final case class MessagePrice(amount: BigDecimal, unit: Iso4127CountryCode) {
  override def toString: String = s"$amount $unit"
}
