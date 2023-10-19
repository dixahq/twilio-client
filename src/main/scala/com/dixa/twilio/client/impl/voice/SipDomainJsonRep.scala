package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.model.voice.SipDomain
import com.dixa.twilio.client.impl.JsonParsingUtil.emptyStringToNone
import com.dixa.twilio.model.{CallbackUrlOptionalAndRequiredMethod, HttpMethod, SidAbstract}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.iam.TwilioAccount

import java.time.Instant

final case class SipDomainJsonRep(
    account_sid: String,
    auth_type: String,
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

  private def authTypeToModel: SipDomain.AuthType =
    if (auth_type.contains("IP_ACL") && auth_type.contains("CREDENTIAL_LIST"))
      SipDomain.AuthType.IpAclAndCredentialList
    else if (auth_type.contains("IP_ACL"))
      SipDomain.AuthType.IpAcl
    else if (auth_type.contains("CREDENTIAL_LIST"))
      SipDomain.AuthType.CredentialList
    else
      throw new IllegalArgumentException(
        s"Don't know how to parse auth_type value of '$auth_type' into known types of ${SipDomain.AuthType.values}"
      )

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
    emptyStringToNone(byoc_trunk_sid).map(SipDomainJsonRep.TempPlaceHolderSid),
    emptyStringToNone(emergency_caller_sid).map(SipDomainJsonRep.TempPlaceHolderSid)
  )
}

private[voice] object SipDomainJsonRep {

  implicit val upickleReader: Reader[SipDomainJsonRep] =
    macroR[SipDomainJsonRep]

  /** Temp sid implementation to use as byoc trunc sid and emergency caller sid, until this library
    * get real support for these conceps.
    */
  private[SipDomainJsonRep] final case class TempPlaceHolderSid private[SipDomainJsonRep] (
      override val toString: String
  ) extends SidAbstract
}
