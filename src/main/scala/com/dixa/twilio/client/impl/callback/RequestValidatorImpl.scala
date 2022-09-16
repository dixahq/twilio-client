package com.dixa.twilio.client.impl.callback

import com.dixa.twilio.client.callback.RequestValidator
import com.dixa.twilio.client.callback.RequestValidator.{
  ValidationRequestStatus,
  ValidationStatus,
  XTwilioSignature
}
import com.dixa.twilio.model.iam.AuthToken

import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.lang.{StringBuilder => JavaStringBuilder}
import scala.util.{Failure, Success, Try}

private[client] class RequestValidatorImpl() extends RequestValidator {

  private val HMAC = "HmacSHA1"

  override def validate(
      requestUrl: String,
      authToken: AuthToken,
      params: Map[String, String],
      xTwilioSignature: XTwilioSignature
  ): ValidationRequestStatus = {
    getValidationSignature(authToken, requestUrl, params) match {
      case Success(signature) => secureCompare(signature, xTwilioSignature)
      case Failure(_)         => ValidationStatus.Invalid
    }
  }

  /** @see
    *   `com.twilio.security.RequestValidator`
    */
  private def getValidationSignature(
      authToken: AuthToken,
      requestUrl: String,
      params: Map[String, String]
  ): Try[String] = {
    Try {
      val builder: JavaStringBuilder = new JavaStringBuilder(requestUrl)

      val sortedKeys = params.keySet.toArray.sorted
      for (key <- sortedKeys) {
        builder.append(key)
        val value: String = params(key)
        builder.append(
          if (value == null) ""
          else value
        )
      }

      val signingKey = new SecretKeySpec(authToken.asString.getBytes, HMAC)
      val mac: Mac   = Mac.getInstance(HMAC)
      mac.init(signingKey)
      val rawHmac: Array[Byte] = mac.doFinal(builder.toString.getBytes(StandardCharsets.UTF_8))
      Base64.getEncoder.encodeToString(rawHmac)
    }
  }

  /** @see
    *   `com.twilio.security.RequestValidator`
    */
  private def secureCompare(a: String, b: XTwilioSignature): ValidationRequestStatus = {
    if (a.length != b.toString.length) return ValidationStatus.Invalid
    var mismatch = 0
    for (i <- 0 until a.length) {
      mismatch |= a.charAt(i) ^ b.toString.charAt(i)
    }
    if (mismatch == 0) ValidationStatus.Valid else ValidationStatus.Invalid
  }

}
