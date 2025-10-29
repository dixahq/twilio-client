package com.dixa.twilio.client.impl.voice

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.voice.SipDomainReadRequestExecutor
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.SipDomain

import scala.concurrent.ExecutionContext

private[client] class SipDomainReadRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends SipDomainReadRequestExecutor {

  import SipDomainReadRequestExecutor._
  import SipDomainReadRequestExecutorImpl._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: SipDomainReadRequest
  ): Either[SipDomainReadException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withParam("PageSize", "1000")
      .build

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/SIP/Domains.json$params",
      connSettings
    )
  }

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: SipDomainReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): List[Either[SipDomainReadException, SipDomain]] = httpResponse.status match {
    case StatusCodes.OK =>
      parseEntityAs[SipDomainListJsonRep](entity) match {
        case Left(ex)           => List(Left(ex))
        case Right(parseResult) =>
          parseResult.domains.map(appResult => Right(appResult.toModelUnsafe))
      }
    case _ => List(buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity))
  }

  override protected def mapApiException(
      apiException: ApiException
  ): SipDomainReadException.Api =
    SipDomainReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): SipDomainReadException.Unspecified = SipDomainReadException.Unspecified(msg, cause)
}

private object SipDomainReadRequestExecutorImpl {

  final case class SipDomainListJsonRep(domains: List[SipDomainJsonRep])

  implicit val applicationListJsonRepReader: Reader[SipDomainListJsonRep] =
    macroR[SipDomainListJsonRep]
}
