package com.dixa.twilio.client.callback

import com.dixa.twilio.client.impl.callback.RequestValidatorImpl
import com.dixa.twilio.client.model.iam.TwilioAccount

trait RequestValidator {

  import RequestValidator._

  /** Validates that the request payload truly comes from twilio, by encrypting the payload (url +
    * params) with hmac-sha1 algorithm using the twilio account auth token for signing key. Finally
    * compares the result with the `X-Twilio-Signature` header.
    *
    * @see
    *   https://www.twilio.com/docs/usage/security#validating-requests
    */
  def validate(
      requestUrl: String,
      authToken: TwilioAccount.AuthToken,
      params: Map[String, String],
      xTwilioSignature: XTwilioSignature
  ): ValidationRequestStatus

}

object RequestValidator {

  def defaultImpl(): RequestValidator = new RequestValidatorImpl()

  final case class XTwilioSignature(override val toString: String)

  sealed trait ValidationRequestStatus
  object ValidationStatus {
    case object Valid   extends ValidationRequestStatus
    case object Invalid extends ValidationRequestStatus
  }

}
