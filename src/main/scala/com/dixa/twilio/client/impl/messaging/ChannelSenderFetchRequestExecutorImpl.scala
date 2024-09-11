package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString}
import com.dixa.twilio.client.messaging.{ChannelSenderException, ChannelSenderFetchRequestExecutor}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.messaging.ChannelSender
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import org.apache.pekko.stream.Materializer

import scala.concurrent.ExecutionContext

private[impl] class ChannelSenderFetchRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ChannelSenderFetchRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Messaging

  override protected def method: org.apache.pekko.http.scaladsl.model.HttpMethod = HttpMethods.GET

  override def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ChannelSenderFetchRequestExecutor.ChannelSenderFetchRequest
  ): Either[ChannelSenderException, HttpRequest] = {
    createHttpRequestFor(
      s"/${ApiVersion.V2}/Channels/Senders/${req.channelSenderSid}",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): ChannelSenderException.Api =
    ChannelSenderException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ChannelSenderException.Unspecified = ChannelSenderException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      request: ChannelSenderFetchRequestExecutor.ChannelSenderFetchRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[ChannelSenderException, ChannelSender] = {
    httpResponse.status match {
      case StatusCodes.OK =>
        parseBody(entity)
      case _ =>
        Left(
          ChannelSenderException.Unexpected(Some(entity.toString), None)
        )
    }
  }

  private def parseBody(entity: HttpEntityString): Either[ChannelSenderException, ChannelSender] = {
    entity.parse[ChannelSenderJsonRep]() match {
      case Left(ex) =>
        Left(
          ChannelSenderException.ParseFailure(ex.cause.getMessage)
        )
      case Right(decoded: ChannelSenderJsonRep) => ChannelSenderJsonRep.toModel(decoded)
    }
  }
}
