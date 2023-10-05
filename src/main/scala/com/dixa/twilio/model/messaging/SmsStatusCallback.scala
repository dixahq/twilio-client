package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.callback.CallbackUrl

final case class SmsStatusCallback(asString: String) extends CallbackUrl(asString)
