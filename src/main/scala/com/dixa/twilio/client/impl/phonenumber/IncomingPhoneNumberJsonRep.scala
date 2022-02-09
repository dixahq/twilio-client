package com.dixa.twilio.client.impl.phonenumber

import com.dixa.twilio.client.impl.phonenumber.IncomingPhoneNumberJsonRep._
import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.model.phonenumber.{
  PhoneNumberE164,
  TwilioIncomingPhoneNumber,
  TwilioPhoneNumberSid
}
import com.dixa.twilio.client.model.phonenumber.TwilioIncomingPhoneNumber.PhoneNumberCapabilitiesSummary

private[phonenumber] final case class IncomingPhoneNumberJsonRep(
    sid: String,
    account_sid: String,
    friendly_name: String,
    phone_number: String,
    capabilities: IncomingNumberCapabilitiesJsonRep,
) {

  private[phonenumber] def toModel = TwilioIncomingPhoneNumber(
    TwilioPhoneNumberSid(sid),
    TwilioAccount.Sid(account_sid),
    TwilioIncomingPhoneNumber.FriendlyName(friendly_name),
    PhoneNumberE164.unchecked(phone_number),
    PhoneNumberCapabilitiesSummary(
      capabilities.voice,
      capabilities.sms,
      capabilities.mms,
      capabilities.fax.getOrElse(false)
    )
  )
}

object IncomingPhoneNumberJsonRep {
  private[phonenumber] final case class IncomingNumberCapabilitiesJsonRep(
      voice: Boolean,
      sms: Boolean,
      mms: Boolean,
      fax: Option[Boolean],
  )
}
