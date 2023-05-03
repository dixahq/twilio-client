package com.dixa.twilio.client.impl.phonenumber

import com.dixa.twilio.client.impl.phonenumber.ActivePhoneNumberJsonRep._
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.PhoneNumberCapabilities._
import com.dixa.twilio.model.phonenumber.PhoneNumberRegulatoryRequirement.AddressRequirementType
import com.dixa.twilio.model.phonenumber._
import com.neovisionaries.i18n.CountryCode
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

import scala.annotation.nowarn

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
    TwilioPhoneNumber.Sid.unsafe(sid),
    TwilioAccount.Sid.unsafe(account_sid),
    PhoneNumberE164.unsafe(phone_number),
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

private[phonenumber] object ActivePhoneNumberJsonRep {
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

  @nowarn(value = "cat=unused") // used by macro generated code
  private implicit val phoneNumberVoiceCapabilitiesJsonRepReader
      : Reader[PhoneNumberVoiceCapabilitiesJsonRep] =
    macroR[PhoneNumberVoiceCapabilitiesJsonRep]

  private[phonenumber] final case class PhoneNumberSmsCapabilitiesJsonRep(
      inbound_connectivity: Boolean,
      outbound_connectivity: Boolean,
      gsm7: Boolean,
      ucs2: Boolean,
      inbound_sender_id_preservation: String,
      inbound_reachability: String,
      inbound_mps: Int,
  )

  @nowarn(value = "cat=unused") // used by macro generated code
  private implicit val phoneNumberSmsCapabilitiesJsonRepReader
      : Reader[PhoneNumberSmsCapabilitiesJsonRep] =
    macroR[PhoneNumberSmsCapabilitiesJsonRep]

  private[phonenumber] final case class PhoneNumberMmsCapabilitiesJsonRep(
      inbound_connectivity: Boolean,
      outbound_connectivity: Boolean,
      inbound_reachability: String,
      inbound_mps: Int,
  )

  @nowarn(value = "cat=unused") // used by macro generated code
  private implicit val phoneNumberMmsCapabilitiesJsonRepReader
      : Reader[PhoneNumberMmsCapabilitiesJsonRep] =
    macroR[PhoneNumberMmsCapabilitiesJsonRep]

  private[phonenumber] final case class PhoneNumberCapabilitiesJsonRep(
      voice: PhoneNumberVoiceCapabilitiesJsonRep,
      sms: PhoneNumberSmsCapabilitiesJsonRep,
      mms: PhoneNumberMmsCapabilitiesJsonRep,
  )

  @nowarn(value = "cat=unused") // used by macro generated code
  private implicit val phoneNumberCapabilitiesJsonRepReader
      : Reader[PhoneNumberCapabilitiesJsonRep] =
    macroR[PhoneNumberCapabilitiesJsonRep]

  private[phonenumber] final case class PhoneNumberGeographyJsonRep(
      iso_country: String,
      lata: Option[String] = None,
      rate_center: Option[String] = None,
      latitude: Option[String] = None,
      longitude: Option[String] = None,
      region: Option[String] = None,
      locality: Option[String] = None,
      postal_code: Option[String] = None,
  )

  @nowarn(value = "cat=unused") // used by macro generated code
  private implicit val outerJsonRepReader: Reader[PhoneNumberGeographyJsonRep] =
    macroR[PhoneNumberGeographyJsonRep]

  private[phonenumber] final case class PhoneNumberRegulatoryJsonRep(
      address_requirements: String,
  )

  @nowarn(value = "cat=unused") // used by macro generated code
  private implicit val phoneNumberRegulatoryJsonRepReader: Reader[PhoneNumberRegulatoryJsonRep] =
    macroR[PhoneNumberRegulatoryJsonRep]

  private[phonenumber] final case class PhoneNumberVoiceConfigurationJsonRep(
      url: Option[String] = None,
      method: Option[String] = None,
      fallback_url: Option[String] = None,
      fallback_method: Option[String] = None,
      application_sid: Option[String] = None,
      trunk_sid: Option[String] = None,
      emergency_address_sid: Option[String] = None,
      emergency_status: Option[String] = None,
      caller_id_lookup: Option[Boolean] = None,
  )

  @nowarn(value = "cat=unused") // used by macro generated code
  private implicit val phoneNumberVoiceConfigurationJsonRepReader
      : Reader[PhoneNumberVoiceConfigurationJsonRep] =
    macroR[PhoneNumberVoiceConfigurationJsonRep]

  private[phonenumber] final case class PhoneNumberSmsConfigurationJsonRep(
      url: Option[String] = None,
      method: Option[String] = None,
      fallback_url: Option[String] = None,
      fallback_method: Option[String] = None,
      application_sid: Option[String] = None,
  )

  @nowarn(value = "cat=unused") // used by macro generated code
  private implicit val phoneNumberSmsConfigurationJsonRepReader
      : Reader[PhoneNumberSmsConfigurationJsonRep] =
    macroR[PhoneNumberSmsConfigurationJsonRep]

  private[phonenumber] final case class PhoneNumberConfigurationJsonRep(
      friendly_name: String,
      status_callback_url: Option[String] = None,
      status_callback_method: Option[String] = None,
      voice: PhoneNumberVoiceConfigurationJsonRep,
      sms: PhoneNumberSmsConfigurationJsonRep,
  )

  @nowarn(value = "cat=unused") // used by macro generated code
  private implicit val phoneNumberConfigurationJsonRepReader
      : Reader[PhoneNumberConfigurationJsonRep] =
    macroR[PhoneNumberConfigurationJsonRep]

  implicit val activePhoneNumberJsonRepReader: Reader[ActivePhoneNumberJsonRep] =
    macroR[ActivePhoneNumberJsonRep]
}
