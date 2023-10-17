package com.dixa.twilio.client.impl.general

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.general.ApplicationReadRequestExecutor
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.general.Application

import scala.concurrent.ExecutionContext

private[client] class ApplicationReadRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends ApplicationReadRequestExecutor {

  import ApplicationReadRequestExecutor._
  import ApplicationReadRequestExecutorImpl._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ApplicationReadRequest
  ): Either[ApplicationReadException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withParam("PageSize", "1000")
      .build

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Applications.json$params",
      connSettings
    )
  }

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: ApplicationReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): List[Either[ApplicationReadException, Application]] = httpResponse.status match {
    case StatusCodes.OK =>
      parseEntityAs[ApplicationListJsonRep](entity) match {
        case Left(ex) => List(Left(ex))
        case Right(parseResult) =>
          parseResult.applications.map(appResult => Right(appResult.toModelUnsafe))
      }
    case _ => List(buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity))
  }

  override protected def mapApiException(
      apiException: ApiException
  ): ApplicationReadException.Api =
    ApplicationReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ApplicationReadException.Unspecified = ApplicationReadException.Unspecified(msg, cause)
}

private object ApplicationReadRequestExecutorImpl {

  final case class ApplicationListJsonRep(applications: List[ApplicationJsonRep])

  implicit val applicationListJsonRepReader: Reader[ApplicationListJsonRep] =
    macroR[ApplicationListJsonRep]
}
