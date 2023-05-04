package com.dixa.twilio.client.impl.phonenumber

import akka.Done
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.phonenumber.IncomingPhoneNumberDeleteRequestExecutor
import com.dixa.twilio.client.phonenumber.IncomingPhoneNumberDeleteRequestExecutor.IncomingPhoneNumberDeleteException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}

import scala.concurrent.ExecutionContext

private[impl] class IncomingPhoneNumberDeleteRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends IncomingPhoneNumberDeleteRequestExecutor {

  override protected def parseHttpResponse(
      request: IncomingPhoneNumberDeleteRequestExecutor.IncomingPhoneNumberDeleteRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[IncomingPhoneNumberDeleteRequestExecutor.IncomingPhoneNumberDeleteException, Done] = {
    httpResponse.status match {
      case StatusCodes.NoContent | StatusCodes.OK => Right(Done)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: IncomingPhoneNumberDeleteRequestExecutor.IncomingPhoneNumberDeleteRequest
  ): Either[
    IncomingPhoneNumberDeleteRequestExecutor.IncomingPhoneNumberDeleteException,
    HttpRequest
  ] =
    createHttpRequestFor(
      s"/2010-04-01/Accounts/${req.accountSid.twilioString}/IncomingPhoneNumbers/${req.phoneNumberId.twilioString}.json",
      connSettings
    )

  override protected def mapApiException(
      apiException: ApiException
  ): IncomingPhoneNumberDeleteException.Api = IncomingPhoneNumberDeleteException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): IncomingPhoneNumberDeleteException.Unspecified =
    IncomingPhoneNumberDeleteException.Unspecified(msg, cause)
}
