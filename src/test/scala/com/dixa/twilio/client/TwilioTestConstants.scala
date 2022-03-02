package com.dixa.twilio.client

import com.dixa.twilio.client.model.iam.TwilioAccount

object TwilioTestConstants {

  val testSid       = "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
  val testAuthToken = "testPassword"

  def connSettings(port: Int): TwilioConnectionSettings = TwilioConnectionSettings(
    baseHostName = "localhost",
    port = port,
    protocol = TwilioConnectionSettings.Protocol.Http,
    accountSid = TwilioAccount.Sid(testSid),
    authToken = TwilioAccount.AuthToken(testAuthToken),
    parallelFactor = TwilioConnectionSettings.ParallelFactor.halfCpuCores,
    timeouts = TwilioConnectionSettings.Timeouts.default
  )
}
