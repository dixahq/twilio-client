package com.dixa.twilio.client.impl.voice

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.voice.IpAccessControlListCreateRequestExecutor
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.IpAccessControlList

import scala.concurrent.ExecutionContext

private[client] class IpAccessControlListCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends IpAccessControlListCreateRequestExecutor {

  import com.dixa.twilio.client.voice.IpAccessControlListCreateRequestExecutor._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: IpAccessControlListCreateRequest
  ): Either[IpAccessControlListCreateException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withParam("AccountSid", req.accountSid)
      .withOptionalParam("FriendlyName", req.friendlyName)
      .buildForPostParams

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/SIP/IpAccessControlLists.json",
      connSettings
    ).map(
      _.withEntity(
        HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params)
      )
    )
  }

  override protected def parseHttpResponse(
      request: IpAccessControlListCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[IpAccessControlListCreateException, IpAccessControlList] = {
    httpResponse.status match {
      case StatusCodes.Created | StatusCodes.OK =>
        parseEntityAs[IpAccessControlListJsonRep](entity).map(j => j.toModelUnsafe)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }

  override protected def mapApiException(
      apiException: ApiException
  ): IpAccessControlListCreateException.Api =
    IpAccessControlListCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): IpAccessControlListCreateException.Unspecified =
    IpAccessControlListCreateException.Unspecified(msg, cause)
}
