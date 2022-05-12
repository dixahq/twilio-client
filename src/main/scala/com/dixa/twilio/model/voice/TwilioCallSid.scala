package com.dixa.twilio.model.voice

/** Represent a Twilio Call SID
  *
  * Input must apply to the format Twilio what Twilio specify as a Call SID: "It is a 34 character
  * string that starts with CA"
  *
  * The twilio documentation about it can be found here:
  * https://support.twilio.com/hc/en-us/articles/223180488-What-is-a-Call-SID-
  */
final case class TwilioCallSid(override val toString: String) {

  require(
    toString.nonEmpty,
    "A Call SID is not allowed to be a empty String. It should always be: A 34 character string that starts with CA"
  )
  require(
    toString.startsWith("CA"),
    s"$toString does not start with CA. A Call SID should always be: A 34 character string that starts with CA"
  )
  require(
    toString.length == 34,
    s"$toString is not 34 characters. A Call SID should always be: A 34 character string that starts with CA"
  )
}
