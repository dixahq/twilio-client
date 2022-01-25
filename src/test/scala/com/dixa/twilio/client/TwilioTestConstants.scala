package com.dixa.twilio.client

import com.dixa.twilio.client.model.TwilioAccount

import java.net.URL

object TwilioTestConstants {

  def connSettings(port: Int): TwilioConnectionSettings = TwilioConnectionSettings(
    url = new URL(s"http://localhost:$port"),
    accountSid = TwilioAccount.Sid("testUsername"),
    authToken = TwilioAccount.AuthToken("testPassword"),
    parallelFactor = TwilioConnectionSettings.ParallelFactor.halfCpuCores,
    timeouts = TwilioConnectionSettings.Timeouts.default
  )
}
