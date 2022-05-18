package com.dixa.twilio.model.voice

import com.dixa.twilio.model.iam.TwilioAccount

final case class Call(
    sid: Call.Sid,
    accountSid: TwilioAccount.Sid

    // A lot of attributes are missing here, but did not need them at time of writing,
    // so add them later once needed.
    // Beware though, that to and from cannot just be phone numbers, as they
    // are often also sip addresses. So some kind of abstraction over that would be needed.
)

object Call {

  /** Represent a Twilio Call SID
    *
    * Input must apply to the format Twilio what Twilio specify as a Call SID: "It is a 34 character
    * string that starts with CA"
    *
    * The twilio documentation about it can be found here:
    * https://support.twilio.com/hc/en-us/articles/223180488-What-is-a-Call-SID-
    */
  final case class Sid(override val toString: String) {

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

}
