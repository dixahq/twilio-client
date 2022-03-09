package com.dixa.twilio.model.messaging

import java.net.URL

final case class StatusCallback(url: URL) {
  override val toString: String = url.toString
}
