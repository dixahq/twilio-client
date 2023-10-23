package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.client.impl.JsonParsingUtil.emptyStringToNone
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.TwilioPhoneNumber
import com.dixa.twilio.model.voice.{ByocTrunk, SipDomain}
import com.dixa.twilio.model.{CallbackUrlOptionalAndRequiredMethod, HttpMethod}

import java.time.Instant

final case class SipDomainJsonRep(
    account_sid: String,
    auth_type: Option[String],
    date_created: String,
    date_updated: String,
    domain_name: String,
    friendly_name: Option[String],
    sip_registration: Boolean,
    sid: String,
    voice_fallback_method: String,
    voice_fallback_url: Option[String],
    voice_method: String,
    voice_status_callback_method: String,
    voice_status_callback_url: Option[String],
    voice_url: Option[String],
    emergency_calling_enabled: Boolean,
    secure: Boolean,
    byoc_trunk_sid: Option[String],
    emergency_caller_sid: Option[String]
) {

  private def authTypeToModel: Option[SipDomain.AuthType] = {
    emptyStringToNone(auth_type).map { noneEmptyAuthType =>
      if (noneEmptyAuthType.contains("IP_ACL") && noneEmptyAuthType.contains("CREDENTIAL_LIST"))
        SipDomain.AuthType.IpAclAndCredentialList
      else if (noneEmptyAuthType.contains("IP_ACL"))
        SipDomain.AuthType.IpAcl
      else if (noneEmptyAuthType.contains("CREDENTIAL_LIST"))
        SipDomain.AuthType.CredentialList
      else
        throw new IllegalArgumentException(
          s"Don't know how to parse noneEmptyAuthType value of '$noneEmptyAuthType' into known types of ${SipDomain.AuthType.values}"
        )
    }
  }

  def toModelUnsafe: SipDomain = SipDomain(
    TwilioAccount.Sid.unsafe(account_sid),
    authTypeToModel,
    Instant.from(Formatter.dateTime.parse(date_created)),
    Instant.from(Formatter.dateTime.parse(date_updated)),
    SipDomain.DomainName.unsafe(domain_name),
    emptyStringToNone(friendly_name).map(SipDomain.FriendlyName.unsafe),
    SipDomain.Sid.unsafe(sid),
    CallbackUrlOptionalAndRequiredMethod(
      emptyStringToNone(voice_fallback_url).map(CallbackUrl.VoiceFallbackUrl),
      HttpMethod.fromTwilioStringUnsafe(voice_fallback_method)
    ),
    CallbackUrlOptionalAndRequiredMethod(
      emptyStringToNone(voice_status_callback_url).map(CallbackUrl.VoiceStatusCallbackUrl),
      HttpMethod.fromTwilioStringUnsafe(voice_status_callback_method)
    ),
    CallbackUrlOptionalAndRequiredMethod(
      emptyStringToNone(voice_url).map(CallbackUrl.VoiceUrl),
      HttpMethod.fromTwilioStringUnsafe(voice_method)
    ),
    sip_registration,
    emergency_calling_enabled,
    secure,
    emptyStringToNone(byoc_trunk_sid).map(ByocTrunk.Sid.unsafe),
    emptyStringToNone(emergency_caller_sid).map(TwilioPhoneNumber.Sid.unsafe)
  )
}

private[voice] object SipDomainJsonRep {

  implicit val upickleReader: Reader[SipDomainJsonRep] =
    macroR[SipDomainJsonRep]
}
