package com.dixa.twilio.client

object TwilioTestConstants {

  def connSettings(port: Int): TwilioConnectionSettings = TwilioConnectionSettings(
    host = "localhost",
    port = port,
    useHttps = false,
    accountSid = "testUsername",
    authToken = "testPassword",
    parallelFactor = TwilioConnectionSettings.ParallelFactor.halfCpuCores
  )
}
