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
    * Input must apply to the format that Twilio specify as a Call SID: "It is a 34 character string
    * that starts with CA"
    *
    * The twilio documentation about it can be found here:
    * https://support.twilio.com/hc/en-us/articles/223180488-What-is-a-Call-SID-
    */
  final case class Sid(override val toString: String) {
    if (toString.isEmpty) throw Sid.ArgumentEmptyException()
    if (!toString.startsWith("CA")) throw Sid.ArgumentMissingCaPrefixException(toString)
    if (toString.length != 34) throw Sid.ArgumentLengthException(toString)
  }

  object Sid {
    private val conformToString = "Callsid is a 34 character string that starts with CA"

    final case class ArgumentEmptyException()
        extends IllegalArgumentException(s"Empty string does not conform to: $conformToString")

    final case class ArgumentMissingCaPrefixException(argument: String)
        extends IllegalArgumentException(
          s"$conformToString does not start with CA and therefor not conform to: $conformToString"
        )

    final case class ArgumentLengthException(argument: String)
        extends IllegalArgumentException(
          s"$argument has length not conforming to: $conformToString"
        )

  }

}
