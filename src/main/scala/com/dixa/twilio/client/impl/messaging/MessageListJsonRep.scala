package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

/** Json representation of the standard error entity that Twilio will send back on none 200
  * responses.
  *
  * More details can be found here: https://www.twilio.com/docs/api/errors
  */
private[impl] case class MessageListJsonRep(
    first_page_uri: String,
    end: Int,
    messages: List[MessageJsonRep],
    previous_page_uri: Option[String] = None,
    uri: String,
    page_size: Int,
    start: Int,
    next_page_uri: Option[String] = None,
    page: Int
)

private[impl] object MessageListJsonRep {

  implicit val TwilioAccountsOuterJsonRepReader: Reader[MessageListJsonRep] =
    macroR[MessageListJsonRep]
}
