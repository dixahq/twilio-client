// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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

  "RequestValidator" should "encode the url into how twilio would send it before encryption if nessesary" in {
    // Twilio would always send webhooks with the query parameters properly encoded. However for clients, it can
    // sometimes be problematic to get such a URI in it's raw form down to the validator, as there http server of chose,
    // may use abstractions over the URI that displays it in a more human readable form. In such cases clients would need to explicit
    // get or construct the raw form of the URI to give to this validator, and that can both be cumbersome and something
    // you could forget. Also if you forget it, it can be a bit hard to debug what actually goes wrong. So it's more safe
    // to let the validator ensure the needed parts of the URI get encoded as Twilio would have done it, even if it from
    // a performance perspective often will be double work, as it properly does repeat some of the URI parsing that
    // the http server has already performed once.

    val authToken = AuthToken.UnknownType("fakeToken")
    // This signature is the one being expected for the provided url when the query parameter has been re encoded.
    // So it basical the signature expected for this URL:
    // "https://sms-twilio.euw1.stag.dixa.io/v1/e7a04fc4-bba8-48a8-a92e-013606a188a6/sms?param1=that%3Ahas%3Achars%3Athat%3Awas%3Aoriginally%3Aencoded&param2=NoSpecialCharsHere#fragmentPart
    val xTwilioSignature = XTwilioSignature("vXRQRiCcfD1DaD0g6Vs3LzkfJSY=")

    // provide an url where the correct url encoded %3A in the query params, has been replaced with the actual : that they represent.
    // URI abstraction layers will often print them like that, to make it more human readable, and as such, it's easy
    // to end up providing such value to the validator if you not carefully.
    val requestUrl =
      "https://sms-twilio.euw1.stag.dixa.io/v1/e7a04fc4-bba8-48a8-a92e-013606a188a6/sms?param1=that:has:chars:that:was:originally:encoded&param2=NoSpecialCharsHere#fragmentPart"
    val validationSignature =
      requestValidator.validate(requestUrl, authToken, requestParams, xTwilioSignature)
    validationSignature shouldBe ValidationStatus.Valid
  }

  it should "also validated it, if the supplied hash actually matched the url as it, even though the query params is missing encoding" in {
    // twilio actually seem to be a bit confused about this, and sometimes they send a signature, that matched the url where the query params is decoded.
    // None the less, from a logical perspective, it anyway makes sense to get a match, if the hash matched the exact input.

    val authToken = AuthToken.UnknownType("fakeToken")
    // This signature is the one being expected for the provided url as is. even though it has none encoded : chars in the query params.
    val xTwilioSignature = XTwilioSignature("CMJUMMXVTgBR/1Nf17u/a0jL/wM=")

    val requestUrl =
      "https://sms-twilio.euw1.stag.dixa.io/v1/e7a04fc4-bba8-48a8-a92e-013606a188a6/sms?param1=that:has:chars:that:was:originally:encoded&param2=NoSpecialCharsHere#fragmentPart"
    val validationSignature =
      requestValidator.validate(requestUrl, authToken, requestParams, xTwilioSignature)
    validationSignature shouldBe ValidationStatus.Valid
  }

  it should "encrypt properly when the url query params are already encoded correctly" in {
    // corosponding to above test, but where the urls are encoded correctly from the start.

    val authToken        = AuthToken.UnknownType("fakeToken")
    val xTwilioSignature = XTwilioSignature("vXRQRiCcfD1DaD0g6Vs3LzkfJSY=")

    val requestUrl =
      "https://sms-twilio.euw1.stag.dixa.io/v1/e7a04fc4-bba8-48a8-a92e-013606a188a6/sms?param1=that%3Ahas%3Achars%3Athat%3Awas%3Aoriginally%3Aencoded&param2=NoSpecialCharsHere#fragmentPart"
    val validationSignature =
      requestValidator.validate(requestUrl, authToken, requestParams, xTwilioSignature)
    validationSignature shouldBe ValidationStatus.Valid
  }

  it should "return invalid request when invalid token is used" in {
    val authToken = AuthToken.UnknownType("invalidToken")

    val xTwilioSignature = XTwilioSignature("kwVt9t4pyirEUMK+Bm/w6YIC0cc=")
    val requestUrl       =
      "https://sms-twilio.euw1.stag.dixa.io/v1/e7a04fc4-bba8-48a8-a92e-013606a188a6/sms"
    val validationSignature =
      requestValidator.validate(requestUrl, authToken, requestParams, xTwilioSignature)
    validationSignature shouldBe ValidationStatus.Invalid
  }

}
