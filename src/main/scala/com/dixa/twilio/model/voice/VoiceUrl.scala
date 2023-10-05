package com.dixa.twilio.model.voice

import com.dixa.twilio.model.callback.CallbackUrl

final case class VoiceUrl(asString: String) extends CallbackUrl(asString)
