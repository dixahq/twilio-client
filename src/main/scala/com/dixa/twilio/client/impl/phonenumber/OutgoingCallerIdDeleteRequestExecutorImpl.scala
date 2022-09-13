package com.dixa.twilio.client.impl.phonenumber

import akka.Done
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpMethod, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdDeleteRequestExecutor
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}

import scala.concurrent.ExecutionContext

private[impl] class OutgoingCallerIdDeleteRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends OutgoingCallerIdDeleteRequestExecutor {

  override protected def parseHttpResponse(
      request: OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteException, Done] = {
    httpResponse.status match {
      case StatusCodes.NoContent => Right(Done)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteRequest
  ): Either[OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteException, HttpRequest] =
    createHttpRequestFor(
      s"/2010-04-01/Accounts/${req.accountSid}/OutgoingCallerIds/${req.outGoingCallerId}.json",
      connSettings
    )

  override protected def mapApiException(
      apiException: ApiException
  ): OutgoingCallerIdDeleteException.Api = OutgoingCallerIdDeleteException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): OutgoingCallerIdDeleteException.Unspecified =
    OutgoingCallerIdDeleteException.Unspecified(msg, cause)
}
