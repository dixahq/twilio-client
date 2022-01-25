package com.dixa.twilio.client

import com.dixa.twilio.client.model.TwilioConnectionSettings

object TwilioTestConstants {
  val connSettings: TwilioConnectionSettings =
    TwilioConnectionSettings("testHost", 3245, useHttps = false, "testAccountSid", "testAuthToken")
}
