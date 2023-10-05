package com.dixa.twilio.model.callback

import com.dixa.twilio.model.TwilioStringValue

import java.net.URL

sealed abstract class CallbackUrl private (override val toString: String) extends TwilioStringValue

object CallbackUrl {

  def apply(fromString: String): CallbackUrl = new BaseImpl(fromString)

  def unapply(callbackUrl: CallbackUrl): Option[String] = {
    Some(callbackUrl.toString)
  }

  private final class BaseImpl(wrapped: String) extends CallbackUrl(wrapped)

  final case class SmsFallbackUrl(asString: String) extends CallbackUrl(toString)

  final case class SmsStatusCallback(asString: String) extends CallbackUrl(asString)

  final case class SmsUrl(asString: String) extends CallbackUrl(asString)

  final case class MessageStatusCallback(url: URL) extends CallbackUrl(url.toString)

  final case class VoiceFallbackUrl(asString: String) extends CallbackUrl(asString)

  final case class VoiceUrl(asString: String) extends CallbackUrl(asString)

}
