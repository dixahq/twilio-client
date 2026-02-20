package com.dixa.twilio.client.impl.voice

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.voice.SipIpAddressReadRequestExecutor
import com.dixa.twilio.client.voice.SipIpAddressReadRequestExecutor.{
  SipIpAddressReadException,
  SipIpAddressReadRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.SipIpAddress

import scala.concurrent.ExecutionContext

private[client] class SipIpAddressReadRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends SipIpAddressReadRequestExecutor {

  import SipIpAddressReadRequestExecutorImpl._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: SipIpAddressReadRequest
  ): Either[SipIpAddressReadException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withParam("PageSize", "1000")
      .build

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/SIP/IpAccessControlLists/${req.ipAccessControlListSid}/IpAddresses.json$params",
      connSettings
    )
  }

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: SipIpAddressReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): List[Either[SipIpAddressReadException, SipIpAddress]] =
    httpResponse.status match {
      case StatusCodes.OK =>
        parseEntityAs[SipIpAddressListJsonRep](entity) match {
          case Left(ex)           => List(Left(ex))
          case Right(parseResult) =>
            parseResult.ip_addresses.map(j => Right(j.toModelUnsafe))
        }
      case _ => List(buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity))
    }

  override protected def mapApiException(
      apiException: ApiException
  ): SipIpAddressReadException.Api =
    SipIpAddressReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): SipIpAddressReadException.Unspecified =
    SipIpAddressReadException.Unspecified(msg, cause)
}

private object SipIpAddressReadRequestExecutorImpl {

  final case class SipIpAddressListJsonRep(
      ip_addresses: List[SipIpAddressJsonRep]
  )

  implicit val sipIpAddressListJsonRepReader: Reader[SipIpAddressListJsonRep] =
    macroR[SipIpAddressListJsonRep]
}
