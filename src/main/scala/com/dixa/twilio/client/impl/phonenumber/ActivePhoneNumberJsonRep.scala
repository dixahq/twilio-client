package com.dixa.twilio.client.impl.phonenumber

import com.dixa.twilio.client.impl.phonenumber.ActivePhoneNumberJsonRep._
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.PhoneNumberCapabilities._
import com.dixa.twilio.model.phonenumber.PhoneNumberRegulatoryRequirement.AddressRequirementType
import com.dixa.twilio.model.phonenumber._
import com.neovisionaries.i18n.CountryCode

private[phonenumber] final case class ActivePhoneNumberJsonRep(
    sid: String,
    account_sid: String,
    phone_number: String,
    url: String,
    `type`: String,
    lifecycle: String,
    geography: PhoneNumberGeographyJsonRep,
    capabilities: PhoneNumberCapabilitiesJsonRep,
    regulatory: PhoneNumberRegulatoryJsonRep,
    configuration: PhoneNumberConfigurationJsonRep,
) {

  private[phonenumber] def toModel = TwilioActivePhoneNumber(
    TwilioPhoneNumberSid(sid),
    TwilioAccount.Sid.unsafe(account_sid),
    PhoneNumberE164(phone_number),
    PhoneNumberType.fromTwilioStringCaseInsensitiveUnsafe(`type`),
    PhoneNumberLifecycle.fromTwilioStringCaseInsensitiveUnsafe(lifecycle),
    PhoneNumberCapabilities(
      voice = VoiceCapabilities(
        capabilities.voice.inbound_connectivity,
        capabilities.voice.outbound_connectivity,
        capabilities.voice.e911,
        capabilities.voice.fax,
        capabilities.voice.calls_per_second,
        capabilities.voice.concurrent_calls_limit,
        capabilities.voice.long_record_length,
        capabilities.voice.inbound_called_dtmf,
        capabilities.voice.inbound_caller_dtmf,
        capabilities.voice.sip_trunking,
        CallerIdPreservation.fromTwilioStringCaseInsensitiveUnsafe(
          capabilities.voice.inbound_caller_id_preservation
        ),
        InboundReachability.fromTwilioStringCaseInsensitiveUnsafe(
          capabilities.voice.inbound_reachability
        ),
      ),
      sms = SmsCapabilities(
        capabilities.sms.inbound_connectivity,
        capabilities.sms.outbound_connectivity,
        capabilities.sms.gsm7,
        capabilities.sms.ucs2,
        CallerIdPreservation.fromTwilioStringCaseInsensitiveUnsafe(
          capabilities.sms.inbound_sender_id_preservation
        ),
        InboundReachability.fromTwilioStringCaseInsensitiveUnsafe(
          capabilities.sms.inbound_reachability
        ),
        capabilities.sms.inbound_mps,
      ),
      mms = MmsCapabilities(
        capabilities.mms.inbound_connectivity,
        capabilities.mms.outbound_connectivity,
        InboundReachability.fromTwilioStringCaseInsensitiveUnsafe(
          capabilities.mms.inbound_reachability
        ),
        capabilities.mms.inbound_mps,
      ),
    ),
    PhoneNumberRegulatoryRequirement(
      AddressRequirementType.fromTwilioStringCaseInsensitiveUnsafe(regulatory.address_requirements)
    ),
    PhoneNumberGeography(
      CountryCode.getByCode(geography.iso_country),
      geography.lata,
      geography.rate_center,
      geography.latitude,
      geography.longitude,
      geography.region,
      geography.locality,
      geography.postal_code,
    ),
  )
}

object ActivePhoneNumberJsonRep {
  private[phonenumber] final case class PhoneNumberVoiceCapabilitiesJsonRep(
      inbound_connectivity: Boolean,
      outbound_connectivity: Boolean,
      e911: Boolean,
      fax: Boolean,
      calls_per_second: Int,
      concurrent_calls_limit: Int,
      long_record_length: Long,
      inbound_called_dtmf: Boolean,
      inbound_caller_dtmf: Boolean,
      sip_trunking: Boolean,
      inbound_caller_id_preservation: String,
      inbound_reachability: String,
  )

  private[phonenumber] final case class PhoneNumberSmsCapabilitiesJsonRep(
      inbound_connectivity: Boolean,
      outbound_connectivity: Boolean,
      gsm7: Boolean,
      ucs2: Boolean,
      inbound_sender_id_preservation: String,
      inbound_reachability: String,
      inbound_mps: Int,
  )

  private[phonenumber] final case class PhoneNumberMmsCapabilitiesJsonRep(
      inbound_connectivity: Boolean,
      outbound_connectivity: Boolean,
      inbound_reachability: String,
      inbound_mps: Int,
  )

  private[phonenumber] final case class PhoneNumberCapabilitiesJsonRep(
      voice: PhoneNumberVoiceCapabilitiesJsonRep,
      sms: PhoneNumberSmsCapabilitiesJsonRep,
      mms: PhoneNumberMmsCapabilitiesJsonRep,
  )

  private[phonenumber] final case class PhoneNumberGeographyJsonRep(
      iso_country: String,
      lata: Option[String],
      rate_center: Option[String],
      latitude: Option[String],
      longitude: Option[String],
      region: Option[String],
      locality: Option[String],
      postal_code: Option[String],
  )

  private[phonenumber] final case class PhoneNumberRegulatoryJsonRep(
      address_requirements: String,
  )

  private[phonenumber] final case class PhoneNumberVoiceConfigurationJsonRep(
      url: Option[String],
      method: Option[String],
      fallback_url: Option[String],
      fallback_method: Option[String],
      application_sid: Option[String],
      trunk_sid: Option[String],
      emergency_address_sid: Option[String],
      emergency_status: Option[String],
      caller_id_lookup: Option[Boolean],
  )

  private[phonenumber] final case class PhoneNumberSmsConfigurationJsonRep(
      url: Option[String],
      method: Option[String],
      fallback_url: Option[String],
      fallback_method: Option[String],
      application_sid: Option[String],
  )

  private[phonenumber] final case class PhoneNumberConfigurationJsonRep(
      friendly_name: String,
      status_callback_url: Option[String],
      status_callback_method: Option[String],
      voice: PhoneNumberVoiceConfigurationJsonRep,
      sms: PhoneNumberSmsConfigurationJsonRep,
  )
}
