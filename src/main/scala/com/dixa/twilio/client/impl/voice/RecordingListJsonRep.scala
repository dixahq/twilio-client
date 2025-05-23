package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

private[voice] final case class RecordingListJsonRep(
    first_page_uri: Option[String] = None,
    end: Int,
    recordings: List[RecordingJsonRep],
    previous_page_uri: Option[String] = None,
    uri: String,
    page_size: Int,
    start: Int,
    next_page_uri: Option[String] = None,
    page: Int,
    total: Option[Int] = None
)

private[voice] object RecordingListJsonRep {
  implicit val upickleReader: Reader[RecordingListJsonRep] =
    macroR[RecordingListJsonRep]
}
