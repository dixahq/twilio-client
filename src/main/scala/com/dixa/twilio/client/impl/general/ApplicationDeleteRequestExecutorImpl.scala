package com.dixa.twilio.client.impl.general

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.general.ApplicationDeleteRequestExecutor
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.Funit

import scala.concurrent.ExecutionContext

private[client] class ApplicationDeleteRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends ApplicationDeleteRequestExecutor {

  import ApplicationDeleteRequestExecutor._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ApplicationDeleteRequest
  ): Either[ApplicationDeleteException, HttpRequest] = {
    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Applications/${req.sid}.json",
      connSettings
    )
  }

  override protected def parseHttpResponse(
      request: ApplicationDeleteRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[ApplicationDeleteException, Funit] = {
    httpResponse.status match {
      case StatusCodes.NoContent => Right(Funit)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }

  override protected def mapApiException(
      apiException: ApiException
  ): ApplicationDeleteException.Api =
    ApplicationDeleteException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ApplicationDeleteException.Unspecified = ApplicationDeleteException.Unspecified(msg, cause)
}
