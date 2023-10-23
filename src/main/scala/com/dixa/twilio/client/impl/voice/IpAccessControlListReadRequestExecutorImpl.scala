package com.dixa.twilio.client.impl.voice

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.voice.IpAccessControlListReadRequestExecutor
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.IpAccessControlList

import scala.concurrent.ExecutionContext

private[client] class IpAccessControlListReadRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends IpAccessControlListReadRequestExecutor {

  import IpAccessControlListReadRequestExecutor._
  import IpAccessControlListReadRequestExecutorImpl._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: IpAccessControlListReadRequest
  ): Either[IpAccessControlListReadException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withParam("PageSize", "1000")
      .build

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/SIP/IpAccessControlLists.json$params",
      connSettings
    )
  }

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: IpAccessControlListReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): List[Either[IpAccessControlListReadException, IpAccessControlList]] =
    httpResponse.status match {
      case StatusCodes.OK =>
        parseEntityAs[IpAccessControlListListJsonRep](entity) match {
          case Left(ex) => List(Left(ex))
          case Right(parseResult) =>
            parseResult.ip_access_control_lists.map(appResult => Right(appResult.toModelUnsafe))
        }
      case _ => List(buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity))
    }

  override protected def mapApiException(
      apiException: ApiException
  ): IpAccessControlListReadException.Api =
    IpAccessControlListReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): IpAccessControlListReadException.Unspecified =
    IpAccessControlListReadException.Unspecified(msg, cause)
}

private object IpAccessControlListReadRequestExecutorImpl {

  final case class IpAccessControlListListJsonRep(
      ip_access_control_lists: List[IpAccessControlListJsonRep]
  )

  implicit val applicationListJsonRepReader: Reader[IpAccessControlListListJsonRep] =
    macroR[IpAccessControlListListJsonRep]
}
