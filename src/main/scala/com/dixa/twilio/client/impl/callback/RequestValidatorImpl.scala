package com.dixa.twilio.client.impl.callback

import com.dixa.twilio.client.callback.RequestValidator
import com.dixa.twilio.client.callback.RequestValidator.{
  ValidationRequestStatus,
  ValidationStatus,
  XTwilioSignature
}
import com.dixa.twilio.model.iam.AuthToken

import java.lang.{StringBuilder => JavaStringBuilder}
import java.net.{URLDecoder, URLEncoder}
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import scala.util.{Failure, Success, Try}

private[client] class RequestValidatorImpl() extends RequestValidator {

  private val HMAC = "HmacSHA1"

  override def validate(
      requestUrl: String,
      authToken: AuthToken,
      params: Map[String, String],
      xTwilioSignature: XTwilioSignature
  ): ValidationRequestStatus = {
    val urlWhereQueryParamsIsEncodedIfNeeded = encodeQueryParamsPartOfUriIfNecessary(requestUrl)
    getValidationSignature(authToken, urlWhereQueryParamsIsEncodedIfNeeded, params) match {
      case Success(signature) => secureCompare(signature, xTwilioSignature)
      case Failure(_)         => ValidationStatus.Invalid
    }
  }

  private def encodeQueryParamsPartOfUriIfNecessary(requestUri: String): String = {
    var queryPart              = ""
    var fragmentPartWithPrefix = ""
    val s1                     = requestUri.split('?')
    val partBeforeQueryParams  = s1(0)
    if (s1.length > 1) {
      val s2 = s1(1).split('#')
      queryPart = s2(0)
      if (s2.length > 1) {
        fragmentPartWithPrefix = s"#${s2(1)}"
      }
    }
    val encodedQueryParams = queryPart
      .split('&')
      .map { singleRequestParamPair =>
        val keyValueArray = singleRequestParamPair.split('=')
        if (keyValueArray.length > 1) {
          // It looks counter intuitive to decode it before encoding it, but it is necessary to avoid double encoding
          // of the % in case something in the string is already encoded.
          val decodedValue = URLDecoder.decode(keyValueArray(1), "utf-8")
          val encodedValue = URLEncoder.encode(decodedValue, "utf-8")
          s"${keyValueArray(0)}=$encodedValue"
        } else singleRequestParamPair
      }
      .mkString("&")
    val encodedQueryParamsWithPrefix =
      if (encodedQueryParams.nonEmpty) s"?$encodedQueryParams" else ""
    val result = s"$partBeforeQueryParams$encodedQueryParamsWithPrefix$fragmentPartWithPrefix"
    result
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
