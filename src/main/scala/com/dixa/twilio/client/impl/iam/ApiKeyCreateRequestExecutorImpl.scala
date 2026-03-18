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

import com.dixa.twilio.client.iam.ApiKeyCreateRequestExecutor
import com.dixa.twilio.client.iam.ApiKeyCreateRequestExecutor.{KeyCreateException, KeyCreateRequest}
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.iam.ApiKey.HasSecret
import com.dixa.twilio.model.iam.{ApiKey, ApiKeyPolicy}
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer

import java.time.Instant
import scala.concurrent.ExecutionContext

private[client] class ApiKeyCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ApiKeyCreateRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Iam

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: KeyCreateRequest
  ): Either[KeyCreateException, HttpRequest] = {
    val policyJson = req.policy.map { p =>
      val allowList = p.map(policy => s""""${policy.twilioString}"""").mkString(",")
      s"""{"allow":[$allowList]}"""
    }

    // The KeyType attribute is only allowed if it's restricted. If you want to create a standard key, then you must omit the KeyType attribute.
    val keyTypeParamValue = req.standardKey match {
      case true  => None
      case false => Some("restricted")
    }

    val params = QueryParamBuilder.empty
      .withParam("AccountSid", req.accountSid.toString)
      .withOptionalParam("FriendlyName", req.friendlyName)
      .withOptionalStringParam("KeyType", keyTypeParamValue)

    val finalParams = policyJson.fold(params)(p => params.withParam("Policy", p))

    createHttpRequestFor("/v1/Keys", connSettings).map(
      _.withEntity(
        HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, finalParams.buildForPostParams)
      )
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): KeyCreateException.Api = KeyCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): KeyCreateException.Unspecified = KeyCreateException.Unspecified(msg, cause)

  private case class KeyCreateJsonRep(
      sid: String,
      secret: String,
      friendly_name: String,
      date_created: String,
      date_updated: String,
      flags: Option[Set[String]] = None,
      policy_allow: Option[Set[String]] = None
  ) {
    def toModel: ApiKey with ApiKey.HasSecret = {
      val base = ApiKey(
        sid = ApiKey.Sid(sid),
        friendlyName = ApiKey.FriendlyName(friendly_name),
        dateCreated =
          Instant.from(java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.parse(date_created)),
        dateUpdated =
          Instant.from(java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.parse(date_updated))
      ).withSecret(ApiKey.Secret(secret))

      val withFlags = flags match {
        case Some(fStrings) =>
          base.withFlags(fStrings.flatMap(ApiKey.Flag.fromTwilioString))
        case None =>
          base
      }

      policy_allow match {
        case Some(pStrings) =>
          withFlags.withPolicyAllow(pStrings.flatMap(ApiKeyPolicy.fromTwilioString))
        case None =>
          withFlags
      }
    }
  }

  private implicit val keyCreateJsonRepReader: Reader[KeyCreateJsonRep] =
    macroR[KeyCreateJsonRep]

  override protected def parseHttpResponse(
      request: KeyCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[KeyCreateException, ApiKey with HasSecret] =
    httpResponse.status match {
      case StatusCodes.Created =>
        parseEntityAs[KeyCreateJsonRep](entity).map(_.toModel)
      case _ =>
        buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
}
