package com.dixa.twilio.client.impl.voice

/** Json representation of the standard error entity that Twilio will send back on none 200
  * responses.
  *
  * More details can be found here: https://www.twilio.com/docs/api/errors
  */
private[impl] case class ConferenceListJsonRep(
    first_page_uri: String,
    end: Int,
    conferences: List[ConferenceJsonResp.TwilioConferenceJsonResp],
    previous_page_uri: Option[String],
    uri: String,
    page_size: Int,
    start: Int,
    next_page_uri: Option[String],
    page: Int
)
