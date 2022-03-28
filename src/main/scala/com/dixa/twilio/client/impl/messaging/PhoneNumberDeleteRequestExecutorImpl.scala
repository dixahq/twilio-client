package com.dixa.twilio.client.impl.messaging

import akka.Done
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.Materializer
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.{ApiSubDomain, DefaultApiErrorEntityJsonRep, HttpEntityString}
import com.dixa.twilio.client.messaging.PhoneNumberDeleteRequestExecutor
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

private[impl] final class PhoneNumberDeleteRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends PhoneNumberDeleteRequestExecutor {

  import PhoneNumberDeleteRequestExecutor._

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: PhoneNumberDeleteRequest
  ): HttpRequest = {
    TwilioPath(
      ApiSubDomain.Messaging,
      HttpMethods.DELETE,
      s"/v1/Services/${req.serviceSid}/PhoneNumbers/${req.phoneNumberSid}"
    )
      .createHttpRequest(connSettings)
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    PhoneNumberDeleteException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Exception]
  ): UnspecifiedException = PhoneNumberDeleteException.UnspecifiedError(msg, cause)

  override protected def parseHttpResponse(
      request: PhoneNumberDeleteRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntity.Strict
  ): Either[PhoneNumberDeleteException, Done] = httpResponse.status match {
    case StatusCodes.OK | StatusCodes.NoContent =>
      Right(Done)
    case StatusCodes.NotFound =>
      buildResultForNotFoundResponse(entity)
    case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
  }

  private def buildResultForNotFoundResponse(entity: HttpEntity.Strict) = {
    val entityString = HttpEntityString(entity.data.utf8String)
    val decoded      = entityString.parseUnsafe[DefaultApiErrorEntityJsonRep]()
    Left(PhoneNumberDeleteException.NotFound(decoded.message))
  }
}
