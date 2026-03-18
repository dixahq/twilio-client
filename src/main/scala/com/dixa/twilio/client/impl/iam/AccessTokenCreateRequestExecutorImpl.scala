// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.impl.iam

import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.iam.AccessTokenCreateRequestExecutor
import com.dixa.twilio.client.iam.AccessTokenCreateRequestExecutor.{
  AccessTokenCreateException,
  AccessTokenCreateRequest
}
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.iam.TwilioGrant.{
  ChatGrant,
  RawGrant,
  SyncGrant,
  VideoGrant,
  VoiceGrant
}
import com.dixa.twilio.model.iam.{AccessToken, TwilioGrant}

import scala.concurrent.{ExecutionContext, Future}

private[iam] final class AccessTokenCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends AccessTokenCreateRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  /** AccessToken is generated locally and does not involve an HTTP request. We override run to
    * perform the generation directly.
    *
    * Tokens have a maximum age of 24 hours. Exceeding it results in error 20157: Expiration Time
    * Exceeds Maximum Time Allowed on Voice SDK.
    */
  override def run(
      connSettings: TwilioConnectionSettings,
      req: AccessTokenCreateRequest
  ): Future[Either[AccessTokenCreateException, AccessToken]] = Future {
    val ttlSeconds = req.ttl.toSeconds
    if (ttlSeconds <= 0 || ttlSeconds > 86400) {
      Left(AccessTokenCreateException.InvalidTtl(ttlSeconds))
    } else {
      try {
        val now     = System.currentTimeMillis() / 1000
        val encoder = Base64.getUrlEncoder.withoutPadding()

        def base64(s: String): String =
          encoder.encodeToString(s.getBytes("UTF-8"))

        val apiKeySid    = connSettings.apiKeySid.toString
        val apiKeySecret = connSettings.apiKeySecret.value
        val accountSid   = connSettings.accountSid.toString
        val regionValue  = connSettings.region.twilioString

        val grantsFields = req.grants
          .map(grantToJson)
          .map { case (key, json) => s""""$key":$json""" }
          .mkString(",")

        val grantsJson = s""""identity":"${req.identity}",$grantsFields"""

        val rawPayload =
          s"""{""" +
            s""""jti":"$apiKeySid-$now",""" +
            s""""iss":"$apiKeySid",""" +
            s""""sub":"$accountSid",""" +
            s""""iat":$now,""" +
            s""""exp":${now + ttlSeconds},""" +
            s""""region":"$regionValue",""" +
            s""""grants":{$grantsJson}""" +
            s"""}"""

        val header  = base64("""{"typ":"JWT","alg":"HS256","cty":"twilio-fpa;v=1"}""")
        val payload = base64(rawPayload)

        val signingInput = s"$header.$payload"

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(new SecretKeySpec(apiKeySecret.getBytes("UTF-8"), "HmacSHA256"))
        val signature = encoder.encodeToString(mac.doFinal(signingInput.getBytes("UTF-8")))

        Right(AccessToken(s"$signingInput.$signature"))
      } catch {
        case e: Exception =>
          Left(createUnspecifiedException(Some("Error generating AccessToken"), Some(e)))
      }
    }
  }

  private def grantToJson(grant: TwilioGrant): (String, String) = grant match {
    case VoiceGrant(incomingAllow, outgoingAppSid) =>
      val incoming = s""""incoming":{"allow":$incomingAllow}"""
      val outgoing = outgoingAppSid
        .map(sid => s""","outgoing":{"application_sid":"$sid"}""")
        .getOrElse("")
      grant.twilioString -> s"{$incoming$outgoing}"
    case ChatGrant(serviceSid) => grant.twilioString -> s"""{"service_sid":"$serviceSid"}"""
    case SyncGrant(serviceSid) => grant.twilioString -> s"""{"service_sid":"$serviceSid"}"""
    case VideoGrant(room)      =>
      grant.twilioString -> room.map(r => s"""{"room":"$r"}""").getOrElse("{}")
    case RawGrant(_, json) => grant.twilioString -> json
  }

  // These are not used since run is overridden, but must be implemented to satisfy the trait
  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: AccessTokenCreateRequest
  ): Either[AccessTokenCreateException, HttpRequest] =
    Left(
      createUnspecifiedException(Some("AccessToken generation does not use HTTP requests"), None)
    )

  override protected def parseHttpResponse(
      request: AccessTokenCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[AccessTokenCreateException, AccessToken] =
    Left(
      createUnspecifiedException(Some("AccessToken generation does not use HTTP requests"), None)
    )

  override protected def mapApiException(
      apiException: ApiException
  ): AccessTokenCreateException.Api =
    AccessTokenCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): AccessTokenCreateException.Unspecified =
    AccessTokenCreateException.Unspecified(msg, cause)

}
