package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.iam.TwilioAccount

final case class TwilioActivePhoneNumber(
    sid: TwilioPhoneNumberSid,
    accountSid: TwilioAccount.Sid,
    phoneNumber: PhoneNumberE164,
    `type`: PhoneNumberType,
    lifecycle: PhoneNumberLifecycle,
    capabilities: PhoneNumberCapabilities,
    regulatory: PhoneNumberRegulatoryRequirement,
    geography: PhoneNumberGeography,
    // skipping "configuration" for now
)
