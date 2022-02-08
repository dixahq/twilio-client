package com.dixa.twilio.client.impl.phonenumber

import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.model.phonenumber.{
  PhoneNumberE164,
  TwilioIncomingPhoneNumber,
  TwilioPhoneNumberSid
}

private[phonenumber] final case class IncomingPhoneNumberJsonRep(
    sid: String,
    account_sid: String,
    friendly_name: String,
    phone_number: String
) {

  private[phonenumber] def toModel = TwilioIncomingPhoneNumber(
    TwilioPhoneNumberSid.IncomingPhoneNumberSid(sid),
    TwilioAccount.Sid(account_sid),
    TwilioIncomingPhoneNumber.FriendlyName(friendly_name),
    PhoneNumberE164.unchecked(phone_number)
  )
}
