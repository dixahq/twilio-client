package com.dixa.twilio.client

import java.net.URL

object TwilioTestConstants {

  def connSettings(port: Int): TwilioConnectionSettings = TwilioConnectionSettings(
    url = new URL(s"http://localhost:$port"),
    accountSid = "testUsername",
    authToken = "testPassword",
    parallelFactor = TwilioConnectionSettings.ParallelFactor.halfCpuCores,
    timeouts = TwilioConnectionSettings.Timeouts.default
  )
}
