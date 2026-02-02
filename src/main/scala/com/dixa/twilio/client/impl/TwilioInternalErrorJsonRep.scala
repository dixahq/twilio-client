package com.dixa.twilio.client.impl

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

/** Json representation of Twilio internal error (500) responses.
  *
  * These errors may have varying structure, so all fields are optional. More details:
  * https://www.twilio.com/docs/api/errors/20500
  */
private[client] final case class TwilioInternalErrorJsonRep(
    code: Option[Long],
    message: Option[String],
    more_info: Option[String],
    status: Option[Int]
)

private[client] object TwilioInternalErrorJsonRep {

  private[client] implicit val upickleRW: Reader[TwilioInternalErrorJsonRep] =
    macroR[TwilioInternalErrorJsonRep]
}
