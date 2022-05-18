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
  final case class Sid private (override val toString: String)

  object Sid {

    def apply(input: String): Sid = safe(input).toTry.get

    def safe(input: String): Either[CreationException, Sid] = {
      if (input.isEmpty) Left(Sid.ArgumentEmptyException())
      else if (!input.startsWith("CA")) Left(Sid.ArgumentMissingCaPrefixException(input))
      else if (input.length != 34) Left(Sid.ArgumentLengthException(input))
      else Right(new Sid(input))
    }

    sealed trait CreationException extends RuntimeException

    final case class ArgumentEmptyException()
        extends IllegalArgumentException(s"Empty string does not conform to: $conformToString")
        with CreationException

    final case class ArgumentMissingCaPrefixException(argument: String)
        extends IllegalArgumentException(
          s"$conformToString does not start with CA and therefor not conform to: $conformToString"
        )
        with CreationException

    final case class ArgumentLengthException(argument: String)
        extends IllegalArgumentException(
          s"$argument has length not conforming to: $conformToString"
        )
        with CreationException

    private val conformToString = "Callsid is a 34 character string that starts with CA"

  }

}
