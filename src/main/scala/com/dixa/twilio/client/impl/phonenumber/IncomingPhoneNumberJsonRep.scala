package com.dixa.twilio.client.impl.phonenumber

import com.dixa.twilio.client.impl.phonenumber.IncomingPhoneNumberJsonRep._
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.TwilioIncomingPhoneNumber.PhoneNumberCapabilitiesSummary
import com.dixa.twilio.model.phonenumber.{
  PhoneNumberE164,
  TwilioIncomingPhoneNumber,
  TwilioPhoneNumber
}
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

import scala.annotation.nowarn

private[phonenumber] final case class IncomingPhoneNumberJsonRep(
    sid: String,
    account_sid: String,
    friendly_name: String,
    phone_number: String,
    capabilities: IncomingNumberCapabilitiesJsonRep,
) {

  private[phonenumber] def toModel = TwilioIncomingPhoneNumber(
    TwilioPhoneNumber.Sid.unsafe(sid),
    TwilioAccount.Sid.unsafe(account_sid),
    TwilioIncomingPhoneNumber.FriendlyName(friendly_name),
    PhoneNumberE164.unsafe(phone_number),
    PhoneNumberCapabilitiesSummary(
      capabilities.voice,
      capabilities.sms,
      capabilities.mms,
      capabilities.fax.getOrElse(false)
    )
  )
}

private[phonenumber] object IncomingPhoneNumberJsonRep {
  final case class IncomingNumberCapabilitiesJsonRep(
      voice: Boolean,
      sms: Boolean,
      mms: Boolean,
      fax: Option[Boolean] = None,
  )

  @nowarn(value = "cat=unused") // used by macro generated code
  private implicit val incomingNumberCapabilitiesJsonRepReader
      : Reader[IncomingNumberCapabilitiesJsonRep] =
    macroR[IncomingNumberCapabilitiesJsonRep]

  implicit val incomingPhoneNumberJsonRepReader: Reader[IncomingPhoneNumberJsonRep] =
    macroR[IncomingPhoneNumberJsonRep]
}
