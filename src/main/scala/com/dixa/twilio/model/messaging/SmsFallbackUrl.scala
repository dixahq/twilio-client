package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.callback.CallbackUrl

final case class SmsFallbackUrl(asString: String) extends CallbackUrl(toString)
