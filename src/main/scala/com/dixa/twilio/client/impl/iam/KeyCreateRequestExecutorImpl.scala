package com.dixa.twilio.client.impl.iam

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.iam.KeyCreateRequestExecutor
import com.dixa.twilio.client.iam.KeyCreateRequestExecutor.{KeyCreateException, KeyCreateRequest}
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.iam.ApiKey

import scala.concurrent.ExecutionContext

private[client] class KeyCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends KeyCreateRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Iam

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: KeyCreateRequest
  ): Either[KeyCreateException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withParam("AccountSid", req.accountSid.toString)
      .withOptionalParam("FriendlyName", req.friendlyName)
      .withOptionalParam("KeyType", req.keyType)
      .buildForPostParams

    createHttpRequestFor("/v1/Keys", connSettings).map(
      _.withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params))
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): KeyCreateException.Api = KeyCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): KeyCreateException.Unspecified = KeyCreateException.Unspecified(msg, cause)

  private case class KeyCreateJsonRep(sid: String, secret: String, friendly_name: String) {
    def toModel: ApiKey = ApiKey(
      sid = ApiKey.Sid(sid),
      secret = ApiKey.Secret(secret),
      friendlyName = ApiKey.FriendlyName(friendly_name)
    )
  }

  private implicit val keyCreateJsonRepReader: Reader[KeyCreateJsonRep] =
    macroR[KeyCreateJsonRep]

  override protected def parseHttpResponse(
      request: KeyCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[KeyCreateException, ApiKey] =
    httpResponse.status match {
      case StatusCodes.Created =>
        parseEntityAs[KeyCreateJsonRep](entity).map(_.toModel)
      case _ =>
        buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
}
