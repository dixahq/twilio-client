package com.dixa.twilio.client.model.phonenumber

import com.dixa.twilio.client.model.iam.TwilioAccount

case class TwilioActivePhoneNumber(
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
