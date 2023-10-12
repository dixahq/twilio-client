package com.dixa.twilio.client.impl.general

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.client.impl.JsonParsingUtil.emptyStringToNone
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.general.Application
import com.dixa.twilio.model.iam.TwilioAccount

import java.time.Instant

/** Json representation of a Application */
private[general] case class ApplicationJsonRep(
    account_sid: String,
    date_created: String,
    date_updated: String,
    friendly_name: Option[String] = None,
    message_status_callback: Option[String] = None,
    sid: String,
    sms_fallback_method: String,
    sms_fallback_url: Option[String] = None,
    sms_method: String,
    sms_status_callback: Option[String] = None,
    sms_url: Option[String] = None,
    status_callback: Option[String] = None,
    status_callback_method: String,
    voice_caller_id_lookup: Boolean,
    voice_fallback_method: String,
    voice_fallback_url: Option[String] = None,
    voice_method: String,
    voice_url: Option[String] = None,
    public_application_connect_enabled: Boolean
) {

  def toModelUnsafe: Application = Application(
    TwilioAccount.Sid.unsafe(account_sid),
    Instant.from(Formatter.dateTime.parse(date_created)),
    Instant.from(Formatter.dateTime.parse(date_updated)),
    emptyStringToNone(friendly_name).map(Application.FriendlyName),
    emptyStringToNone(message_status_callback).map(CallbackUrl.MessageStatusCallback.apply),
    Application.Sid.unsafe(sid),
    HttpMethod.fromTwilioStringUnsafe(sms_fallback_method),
    emptyStringToNone(sms_fallback_url).map(CallbackUrl.SmsFallbackUrl),
    HttpMethod.fromTwilioStringUnsafe(sms_method),
    emptyStringToNone(sms_status_callback).map(CallbackUrl.SmsStatusCallback),
    emptyStringToNone(sms_url).map(CallbackUrl.SmsUrl),
    emptyStringToNone(status_callback).map(CallbackUrl.ApplicationStatusCallback),
    HttpMethod.fromTwilioStringUnsafe(status_callback_method),
    voice_caller_id_lookup,
    HttpMethod.fromTwilioStringUnsafe(voice_fallback_method),
    emptyStringToNone(voice_fallback_url).map(CallbackUrl.VoiceFallbackUrl),
    HttpMethod.fromTwilioStringUnsafe(voice_method),
    emptyStringToNone(voice_url).map(CallbackUrl.VoiceUrl),
    public_application_connect_enabled
  )
}

private[general] object ApplicationJsonRep {
  implicit val upickleReader: Reader[ApplicationJsonRep] =
    macroR[ApplicationJsonRep]
}
