package com.dixa.twilio.client.impl.request

import com.dixa.twilio.client.RequestValidator
import com.dixa.twilio.client.RequestValidator.{
  ValidationRequestStatus,
  ValidationStatus,
  XTwilioSignature
}
import com.dixa.twilio.client.model.iam.TwilioAccount

import java.nio.charset.StandardCharsets
import java.util
import java.util.Collections
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter
import scala.util.{Failure, Success, Try}
import collection.JavaConverters._
import scala.collection.JavaConversions._

private[client] class RequestValidatorImpl() extends RequestValidator {

  private val HMAC = "HmacSHA1"

  override def validate(
      requestUrl: String,
      authToken: TwilioAccount.AuthToken,
      params: Map[String, String],
      xTwilioSignature: XTwilioSignature
  ): ValidationRequestStatus = {
    getValidationSignature(authToken, requestUrl, params) match {
      case Success(signature) => secureCompare(signature, xTwilioSignature)
      case Failure(ex)        => ValidationStatus.Invalid
    }
  }

  /** @see
    *   `com.twilio.security.RequestValidator`
    */
  private def getValidationSignature(
      authToken: TwilioAccount.AuthToken,
      requestUrl: String,
      params: Map[String, String]
  ): Try[String] = {
    Try {
      val builder: StringBuilder = new StringBuilder(requestUrl)

      val sortedKeys: util.List[String] = new util.ArrayList[String](params.keySet.asJava)
      Collections.sort(sortedKeys)
      for (key <- sortedKeys) {
        builder.append(key)
        val value: String = params(key)
        builder.append(
          if (value == null) ""
          else value
        )
      }

      val signingKey = new SecretKeySpec(authToken.toString.getBytes, HMAC)
      val mac: Mac   = Mac.getInstance(HMAC)
      mac.init(signingKey)
      val rawHmac: Array[Byte] = mac.doFinal(builder.toString.getBytes(StandardCharsets.UTF_8))
      DatatypeConverter.printBase64Binary(rawHmac)
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
