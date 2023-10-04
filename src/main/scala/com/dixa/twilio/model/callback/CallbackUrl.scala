package com.dixa.twilio.model.callback

import com.dixa.twilio.model.TwilioStringValue

abstract class CallbackUrl private[model] (override val toString: String) extends TwilioStringValue

object CallbackUrl {

  def apply(fromString: String): CallbackUrl = new BaseImpl(fromString)

  def unapply(callbackUrl: CallbackUrl): Option[String] = {
    Some(callbackUrl.toString)
  }

  private final class BaseImpl(wrapped: String) extends CallbackUrl(wrapped)
}
