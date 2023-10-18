package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.TwilioStringValue
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Call

/** Represents a the response of creating an outgoing caller id
  *
  * Creating an outgoing caller id triggers a validation request in Twilio, which initiates a call
  * to the provided in the create request and listens for a validation code.
  *
  * The validation request is represented in this response object
  *
  * @see
  *   https://www.twilio.com/docs/voice/api/outgoing-caller-ids#outgoingcallerid-instance-resource
  */
final case class OutgoingCallerIdCreateResponse(
    accountSid: TwilioAccount.Sid,
    phoneNumber: PhoneNumberE164,
    friendlyName: Option[OutgoingCallerId.FriendlyName],
    validationCode: OutgoingCallerIdCreateResponse.ValidationCode,
    callSid: Call.Sid
)

object OutgoingCallerIdCreateResponse {
  final case class ValidationCode(code: String) extends TwilioStringValue {
    def digits = {
      code.map(_.asDigit)
    }

    override val toString: String = "XXXXXXX"
  }

}
