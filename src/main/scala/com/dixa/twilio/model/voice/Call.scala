package com.dixa.twilio.model.voice

import com.dixa.twilio.model.iam.TwilioAccount

final case class Call(
    sid: TwilioCallSid,
    accountSid: TwilioAccount.Sid

    // A lot of attributes are missing here, but did not need them at time of writing,
    // so add them later once needed.
    // Beware though, that to and from cannot just be phone numbers, as they
    // are often also sip addresses. So some kind of abstraction over that would be needed.
)
