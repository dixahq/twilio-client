package com.dixa.twilio.client.impl

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

/** Json representation of the standard error entity that Twilio will send back on none 200
  * responses.
  *
  * More details can be found here: https://www.twilio.com/docs/api/errors
  */
private[client] final case class DefaultApiErrorEntityJsonRep(
    code: Long,
    message: String,
    more_info: String,
    status: Int
)

private[client] object DefaultApiErrorEntityJsonRep {

  private[client] implicit val upickleRW: Reader[DefaultApiErrorEntityJsonRep] =
    macroR[DefaultApiErrorEntityJsonRep]
}
