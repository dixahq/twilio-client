package com.dixa.twilio.client.impl

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

/** Json representation that can be used in all cases where all you care about is a single sid
  * attribute
  */
private[impl] final case class HasSidJsonRep(sid: String)

private[impl] object HasSidJsonRep {

  implicit val upickleReader: Reader[HasSidJsonRep] = macroR[HasSidJsonRep]
}
