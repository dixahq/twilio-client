package com.dixa.twilio.client.impl.phonenumber

import com.dixa.twilio.model.phonenumber._
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

private[phonenumber] final case class OutgoingCallerIdJsonRep(
    sid: String,
    account_sid: String,
    phone_number: String,
    friendly_name: String,
) {

  private[phonenumber] def toModel = OutgoingCallerId(
    sid = OutgoingCallerId.Sid.unsafe(sid),
    friendlyName = OutgoingCallerId.FriendlyName(friendly_name),
    phoneNumber = PhoneNumberE164.unsafe(phone_number)
  )
}

private[phonenumber] object OutgoingCallerIdJsonRep {

  implicit val outgoingCallerIdJsonRepReader: Reader[OutgoingCallerIdJsonRep] =
    macroR[OutgoingCallerIdJsonRep]
}
