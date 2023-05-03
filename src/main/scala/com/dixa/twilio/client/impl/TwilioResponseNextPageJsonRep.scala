package com.dixa.twilio.client.impl

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

private[client] case class TwilioResponseNextPageJsonRep(next_page_uri: Option[String]) {}

private[client] object TwilioResponseNextPageJsonRep {

  private[client] implicit val upickleRW: Reader[TwilioResponseNextPageJsonRep] =
    macroR[TwilioResponseNextPageJsonRep]
}
