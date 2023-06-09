package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

private[voice] final case class CallListJsonRep(
    first_page_uri: String,
    end: Int,
    calls: List[CallJsonRep],
    previous_page_uri: Option[String] = None,
    uri: String,
    page_size: Int,
    start: Int,
    next_page_uri: Option[String] = None,
    page: Int
)

private[voice] object CallListJsonRep {

  implicit val callListJsonRepReader: Reader[CallListJsonRep] =
    macroR[CallListJsonRep]
}
