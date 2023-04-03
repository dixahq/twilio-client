package com.dixa.twilio.client.impl.voice

private[impl] final case class ParticipantListJsonRep(
    first_page_uri: String,
    end: Int,
    participants: List[ParticipantJsonRep],
    previous_page_uri: Option[String],
    uri: String,
    page_size: Int,
    start: Int,
    next_page_uri: Option[String],
    page: Int
)
