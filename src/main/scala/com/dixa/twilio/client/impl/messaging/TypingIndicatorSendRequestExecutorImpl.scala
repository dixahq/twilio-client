package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString}
import com.dixa.twilio.client.messaging.TypingIndicatorSendRequestExecutor
import com.dixa.twilio.client.messaging.TypingIndicatorSendRequestExecutor.{
  TypingIndicatorSendException,
  TypingIndicatorSendRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.FUnit
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer

import scala.concurrent.ExecutionContext

private[impl] final class TypingIndicatorSendRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends TypingIndicatorSendRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Messaging

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: TypingIndicatorSendRequest
  ): Either[TypingIndicatorSendException, HttpRequest] = {
    val postParams = s"messageId=${req.messageSid}&channel=whatsapp"

    createHttpRequestFor(s"/${ApiVersion.V2}/Indicators/Typing.json", connSettings)
      .map(_.withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, postParams)))
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    TypingIndicatorSendException.Api.apply(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = TypingIndicatorSendException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      request: TypingIndicatorSendRequestExecutor.TypingIndicatorSendRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[TypingIndicatorSendException, FUnit] = Right(FUnit)
}
