package com.dixa.twilio.model.callback

import com.dixa.twilio.model.TwilioStringValue
import com.dixa.twilio.model.callback.CallbackUrl.{
  ApplicationStatusCallback,
  AsyncAmdStatusCallbackUrl,
  RecordingStatusCallbackUrl,
  SmsFallbackUrl,
  SmsStatusCallback,
  SmsUrl,
  UsageTriggerUrl,
  VoiceFallbackUrl,
  VoiceStatusCallbackUrl,
  VoiceUrl
}

import java.net.{URI, URL}

sealed abstract class CallbackUrl private (override val toString: String)
    extends TwilioStringValue {
  def toSmsFallbackUrl: SmsFallbackUrl                 = SmsFallbackUrl(toString)
  def toSmsStatusCallback: SmsStatusCallback           = SmsStatusCallback(toString)
  def toSmsUrl: SmsUrl                                 = SmsUrl(toString)
  def toVoiceFallbackUrl: VoiceFallbackUrl             = VoiceFallbackUrl(toString)
  def toVoiceUrl: VoiceUrl                             = VoiceUrl(toString)
  def toVoiceStatusCallbackUrl: VoiceStatusCallbackUrl = VoiceStatusCallbackUrl(toString)
  def toRecordingStatusCallbackUrl: RecordingStatusCallbackUrl = RecordingStatusCallbackUrl(
    toString
  )
  def toAsyncAmdStatusCallbackUrl: AsyncAmdStatusCallbackUrl = AsyncAmdStatusCallbackUrl(toString)
  def toUsageTriggerUrl: UsageTriggerUrl                     = UsageTriggerUrl(toString)
  def toApplicationStatusCallback: ApplicationStatusCallback = ApplicationStatusCallback(toString)
}

object CallbackUrl {

  def apply(fromString: String): CallbackUrl = new BaseImpl(fromString)

  def unapply(callbackUrl: CallbackUrl): Option[String] = {
    Some(callbackUrl.toString)
  }

  private final class BaseImpl(wrapped: String) extends CallbackUrl(wrapped)

  final case class SmsFallbackUrl(asString: String) extends CallbackUrl(asString)

  final case class SmsStatusCallback(asString: String) extends CallbackUrl(asString)

  final case class SmsUrl(asString: String) extends CallbackUrl(asString)

  final case class MessageStatusCallback(url: URL) extends CallbackUrl(url.toString)
  object MessageStatusCallback {
    def apply(s: String): MessageStatusCallback = MessageStatusCallback(new URI(s).toURL)
  }

  final case class VoiceFallbackUrl(asString: String) extends CallbackUrl(asString)

  final case class VoiceUrl(asString: String) extends CallbackUrl(asString)

  final case class UsageTriggerUrl(asString: String) extends CallbackUrl(asString)

  final case class VoiceStatusCallbackUrl(asString: String) extends CallbackUrl(asString)

  final case class RecordingStatusCallbackUrl(asString: String) extends CallbackUrl(asString)

  final case class AsyncAmdStatusCallbackUrl(asString: String) extends CallbackUrl(asString)

  final case class OutgoingCallerIdVerificationUrl(asString: String) extends CallbackUrl(asString)

  final case class ApplicationStatusCallback(asString: String) extends CallbackUrl(asString) {
    override def toApplicationStatusCallback: ApplicationStatusCallback = this
  }
}
