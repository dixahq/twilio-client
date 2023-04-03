package com.dixa.twilio.client.impl.voice

private[impl] case class ConferenceListJsonRep(
    first_page_uri: String,
    end: Int,
    conferences: List[ConferenceJsonRep.TwilioConferenceJsonResp],
    previous_page_uri: Option[String],
    uri: String,
    page_size: Int,
    start: Int,
    next_page_uri: Option[String],
    page: Int
)
