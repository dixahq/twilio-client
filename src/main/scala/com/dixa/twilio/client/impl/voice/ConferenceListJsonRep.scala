package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

private[voice] case class ConferenceListJsonRep(
    first_page_uri: String,
    end: Int,
    conferences: List[ConferenceJsonRep.TwilioConferenceJsonResp],
    previous_page_uri: Option[String],
    uri: String,
    page_size: Int,
    start: Int,
    next_page_uri: Option[String] = None,
    page: Int
)

private[voice] object ConferenceListJsonRep {

  implicit val conferenceListJsonRepReader: Reader[ConferenceListJsonRep] =
    macroR[ConferenceListJsonRep]
}
