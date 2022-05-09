package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{Call, TwilioCallSid}

/** Json representation of a Call */
private[impl] case class CallJsonRep(
    account_sid: String,
    sid: String
    // A lot more fields to add here, once needed.
) {

  def toModel: Call = Call(
    TwilioCallSid(sid),
    TwilioAccount.Sid(account_sid)
  )
}
