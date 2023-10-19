package com.dixa.twilio.client.impl.phonenumber

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.client.impl.JsonParsingUtil.emptyStringToNone
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber._
import com.dixa.twilio.model.voice.Call

private[phonenumber] final case class OutgoingCallerIdCreateResponseJsonRep(
    account_sid: String,
    phone_number: String,
    friendly_name: Option[String],
    validation_code: String,
    call_sid: String,
) {

  private[phonenumber] def toModel = OutgoingCallerIdCreateResponse(
    accountSid = TwilioAccount.Sid.unsafe(account_sid),
    friendlyName = emptyStringToNone(friendly_name).map(OutgoingCallerId.FriendlyName),
    phoneNumber = PhoneNumberE164.unsafe(phone_number),
    validationCode = OutgoingCallerIdCreateResponse.ValidationCode(validation_code),
    callSid = Call.Sid.unsafe(call_sid)
  )
}

private[phonenumber] object OutgoingCallerIdCreateResponseJsonRep {

  implicit val outgoingCallerIdJsonRepReader: Reader[OutgoingCallerIdCreateResponseJsonRep] =
    macroR[OutgoingCallerIdCreateResponseJsonRep]
}
