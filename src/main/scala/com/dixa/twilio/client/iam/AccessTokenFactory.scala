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

package com.dixa.twilio.client.iam

import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.model.Region
import com.dixa.twilio.model.iam.TwilioGrant.{
  ChatGrant,
  RawGrant,
  SyncGrant,
  VideoGrant,
  VoiceGrant
}
import com.dixa.twilio.model.iam.{AccessToken, TwilioAccount, TwilioGrant}

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

/** Generates Twilio Access Tokens locally using API key credentials.
  *
  * Access tokens are short-lived JWTs signed with a Twilio API key. They are used to authenticate
  * client-side SDKs (Voice, Chat, Video, etc.) and must be regenerated before expiry.
  *
  * Tokens have a maximum lifetime of 24 hours. See https://www.twilio.com/docs/iam/access-tokens
  * for details.
  */
object AccessTokenFactory {

  sealed trait Error extends RuntimeException

  object Error {

    final case class InvalidTtl(ttlSeconds: Long)
        extends RuntimeException(
          s"TTL must be between 1 and 86400 seconds, got $ttlSeconds"
        )
        with Error

    final case class GenerationFailed(cause: Throwable)
        extends RuntimeException("Failed to generate access token", cause)
        with Error
  }

  /** Generate an access token signed with the given API key credentials.
    *
    * @param credentials
    *   The API key credentials to sign the token with.
    * @param accountSid
    *   The Twilio account SID the token is issued for.
    * @param region
    *   The Twilio region the token is bound to.
    * @param identity
    *   A unique identifier for the end user the token is issued to.
    * @param grants
    *   The set of grants defining which Twilio products the token holder may access.
    * @param ttl
    *   How long the token is valid. Must be between 1 second and 24 hours (86400 seconds).
    */
  def generate(
      credentials: TwilioConnectionSettings.Credentials.ApiKeyCredentials,
      accountSid: TwilioAccount.Sid,
      region: Region,
      identity: String,
      grants: Seq[TwilioGrant],
      ttl: FiniteDuration
  ): Either[Error, AccessToken] = {
    val ttlSeconds = ttl.toSeconds
    if (ttlSeconds <= 0 || ttlSeconds > 86400) {
      Left(Error.InvalidTtl(ttlSeconds))
    } else {
      Try {
        val now     = System.currentTimeMillis() / 1000
        val encoder = Base64.getUrlEncoder.withoutPadding()

        def base64(s: String): String = encoder.encodeToString(s.getBytes("UTF-8"))

        val apiKeySid    = credentials.apiKeySid.toString
        val apiKeySecret = credentials.apiKeySecret.value

        val grantsFields = grants
          .map(grantToJson)
          .map { case (key, json) => s""""$key":$json""" }
          .mkString(",")

        val rawPayload =
          s"""{""" +
            s""""jti":"$apiKeySid-$now",""" +
            s""""iss":"$apiKeySid",""" +
            s""""sub":"${accountSid.toString}",""" +
            s""""iat":$now,""" +
            s""""exp":${now + ttlSeconds},""" +
            s""""grants":{"identity":"$identity",$grantsFields}""" +
            s"""}"""

        val header =
          base64(
            s"""{"typ":"JWT","alg":"HS256","cty":"twilio-fpa;v=1","twr":"${region.twilioString}"}"""
          )
        val payload      = base64(rawPayload)
        val signingInput = s"$header.$payload"

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(new SecretKeySpec(apiKeySecret.getBytes("UTF-8"), "HmacSHA256"))
        val signature = encoder.encodeToString(mac.doFinal(signingInput.getBytes("UTF-8")))

        AccessToken(s"$signingInput.$signature")
      }.toEither.left.map(Error.GenerationFailed)
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
}
