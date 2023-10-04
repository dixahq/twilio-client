package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.callback.CallbackUrl

import java.net.URL

final case class StatusCallback(url: URL) extends CallbackUrl(url.toString)
