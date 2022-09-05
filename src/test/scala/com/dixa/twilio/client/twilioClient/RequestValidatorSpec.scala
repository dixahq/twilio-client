package com.dixa.twilio.client.twilioClient

import com.dixa.twilio.client.callback.RequestValidator.{ValidationStatus, XTwilioSignature}
import com.dixa.twilio.client.impl.callback.RequestValidatorImpl
import com.dixa.twilio.model.iam.AuthToken
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RequestValidatorSpec extends AnyFlatSpec with Matchers with ScalaFutures {

  val requestValidator = new RequestValidatorImpl()

  private val requestParams = Map(
    "ToCountry"           -> "DK",
    "ToState"             -> "",
    "SmsMessageSid"       -> "SM4dc2350814fbae9b9f364ef2cb8112bd",
    "NumMedia"            -> "0",
    "ToCity"              -> "",
    "FromZip"             -> "",
    "SmsSid"              -> "SM4dc2350814fbae9b9f364ef2cb8112bd",
    "FromState"           -> "",
    "SmsStatus"           -> "received",
    "FromCity"            -> "",
    "Body"                -> "New test",
    "FromCountry"         -> "ES",
    "To"                  -> "+4593751435",
    "MessagingServiceSid" -> "MG9c1cd47537d713e3dce94952364de1c5",
    "ToZip"               -> "",
    "AddOns"              -> """{"status":"successful","message":null,"code":null,"results":{}}""",
    "NumSegments"         -> "1",
    "MessageSid"          -> "SM4dc2350814fbae9b9f364ef2cb8112bd",
    "AccountSid"          -> "ACf6c9aa4f8756c258be45a6d2637cfa15",
    "From"                -> "+34650753378",
    "ApiVersion"          -> "2010-04-01"
  )

  "RequestValidator" should "encript the full sms inbound request using the twilio auth token as signature " in {
    val authToken        = AuthToken.UnknownType("fakeToken")
    val xTwilioSignature = XTwilioSignature("kwVt9t4pyirEUMK+Bm/w6YIC0cc=")

    val requestUrl =
      "https://sms-twilio.euw1.stag.dixa.io/v1/e7a04fc4-bba8-48a8-a92e-013606a188a6/sms"
    val validationSignature =
      requestValidator.validate(requestUrl, authToken, requestParams, xTwilioSignature)
    validationSignature shouldBe ValidationStatus.Valid
  }

  it should "return invalid request when invalid token is used" in {
    val authToken = AuthToken.UnknownType("invalidToken")

    val xTwilioSignature = XTwilioSignature("kwVt9t4pyirEUMK+Bm/w6YIC0cc=")
    val requestUrl =
      "https://sms-twilio.euw1.stag.dixa.io/v1/e7a04fc4-bba8-48a8-a92e-013606a188a6/sms"
    val validationSignature =
      requestValidator.validate(requestUrl, authToken, requestParams, xTwilioSignature)
    validationSignature shouldBe ValidationStatus.Invalid
  }

}
