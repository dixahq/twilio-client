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

/** Generates Twilio Access Tokens locally using API key credentials.
  *
  * Access tokens are short-lived JWTs signed with a Twilio API key. They are used to authenticate
  * client-side SDKs (Voice, Chat, Video, etc.) and must be regenerated before expiry.
  *
  * Tokens have a maximum lifetime of 24 hours. See https://www.twilio.com/docs/iam/access-tokens
  * for details.
  *
  * Use [[AccessTokenFactory.defaultImpl]] to get an instance, or provide your own implementation in
  * tests.
  */
trait AccessTokenFactory {

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
  ): Either[AccessTokenFactory.Error, AccessToken]

  /** Like [[generate]], but throws on error instead of returning a `Left`. */
  def generateUnsafe(
      credentials: TwilioConnectionSettings.Credentials.ApiKeyCredentials,
      accountSid: TwilioAccount.Sid,
      region: Region,
      identity: String,
      grants: Seq[TwilioGrant],
      ttl: FiniteDuration
  ): AccessToken =
    generate(credentials, accountSid, region, identity, grants, ttl).fold(
      e => throw e,
      token => token
    )
}

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

  val defaultImpl: AccessTokenFactory = new AccessTokenFactoryImpl()

  private final class AccessTokenFactoryImpl extends AccessTokenFactory {

    import scala.util.Try

    override def generate(
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

          val grantsObj = ujson.Obj.from(
            Seq("identity" -> ujson.Str(identity)) ++
              grants.map(g => g.twilioString -> grantToJson(g))
          )

          val headerJson = ujson.write(
            ujson.Obj(
              "typ" -> ujson.Str("JWT"),
              "alg" -> ujson.Str("HS256"),
              "cty" -> ujson.Str("twilio-fpa;v=1"),
              "twr" -> ujson.Str(region.twilioString)
            )
          )

          val payloadJson = ujson.write(
            ujson.Obj(
              "jti"    -> ujson.Str(s"$apiKeySid-$now"),
              "iss"    -> ujson.Str(apiKeySid),
              "sub"    -> ujson.Str(accountSid.toString),
              "iat"    -> ujson.Num(now.toDouble),
              "exp"    -> ujson.Num((now + ttlSeconds).toDouble),
              "grants" -> grantsObj
            )
          )

          val signingInput = s"${base64(headerJson)}.${base64(payloadJson)}"

          val mac = Mac.getInstance("HmacSHA256")
          mac.init(new SecretKeySpec(apiKeySecret.getBytes("UTF-8"), "HmacSHA256"))
          val signature = encoder.encodeToString(mac.doFinal(signingInput.getBytes("UTF-8")))

          AccessToken(s"$signingInput.$signature")
        }.toEither.left.map(Error.GenerationFailed)
      }
    }

    private def grantToJson(grant: TwilioGrant): ujson.Value = grant match {
      case VoiceGrant(incomingAllow, outgoingAppSid) =>
        val obj = ujson.Obj("incoming" -> ujson.Obj("allow" -> ujson.Bool(incomingAllow)))
        outgoingAppSid.foreach(sid =>
          obj("outgoing") = ujson.Obj("application_sid" -> ujson.Str(sid.toString))
        )
        obj
      case ChatGrant(serviceSid) => ujson.Obj("service_sid" -> ujson.Str(serviceSid.twilioString))
      case SyncGrant(serviceSid) => ujson.Obj("service_sid" -> ujson.Str(serviceSid.twilioString))
      case VideoGrant(room)      =>
        room.fold(ujson.Obj())(r => ujson.Obj("room" -> ujson.Str(r.twilioString)))
      case RawGrant(_, json) => ujson.read(json)
    }
  }
}
