package com.dixa.twilio.client

import com.dixa.twilio.client.model.TwilioAccount

object TwilioTestConstants {

  def connSettings(port: Int): TwilioConnectionSettings = TwilioConnectionSettings(
    baseHostName = "localhost",
    port = port,
    protocol = TwilioConnectionSettings.Protocol.Http,
    accountSid = TwilioAccount.Sid("testUsername"),
    authToken = TwilioAccount.AuthToken("testPassword"),
    parallelFactor = TwilioConnectionSettings.ParallelFactor.halfCpuCores,
    timeouts = TwilioConnectionSettings.Timeouts.default
  )
}
