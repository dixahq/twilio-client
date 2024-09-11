package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.ApiException.{BadRequestException, NotFound}
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString}
import com.dixa.twilio.client.messaging.ChannelSenderException.Api
import com.dixa.twilio.client.messaging.{ChannelSenderDeleteRequestExecutor, ChannelSenderException}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.FUnit
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer

import scala.concurrent.ExecutionContext

class ChannelSenderDeleteRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ChannelSenderDeleteRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Messaging

  override protected def method: HttpMethod = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ChannelSenderDeleteRequestExecutor.ChannelSenderDeleteRequest
  ): Either[ChannelSenderException, HttpRequest] = {
    createHttpRequestFor(
      s"/${ApiVersion.V2}/Channels/Senders/${req.channelSenderSid.twilioString}",
      connSettings
    )
  }

  override protected def parseHttpResponse(
      request: ChannelSenderDeleteRequestExecutor.ChannelSenderDeleteRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[ChannelSenderException, FUnit] = {
    httpResponse.status match {
      case StatusCodes.NotFound                   => Left(Api(NotFound(entity.toString)))
      case StatusCodes.BadRequest                 => Left(Api(BadRequestException(entity.toString)))
      case StatusCodes.OK | StatusCodes.NoContent => Right(FUnit)
      case _ => Left(ChannelSenderException.Unexpected(Some(entity.toString), None))
    }
  }

  override protected def mapApiException(apiException: ApiException): ChannelSenderException.Api =
    ChannelSenderException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ChannelSenderException.Unspecified = ChannelSenderException.Unspecified(msg, cause)
}
