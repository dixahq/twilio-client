package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.Formatter.dateTime
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.client.impl.messaging.MediaResourceUrlFactory.buildMediaResourcePath
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString}
import com.dixa.twilio.client.messaging.ChannelSenderFetchRequestExecutor.ChannelSenderFetchException
import com.dixa.twilio.client.messaging.{
  ChannelSenderFetchRequestExecutor,
  MessageMediaResourceReadRequestExecutor
}
import com.dixa.twilio.client.messaging.MessageMediaResourceReadRequestExecutor.{
  MessageMediaResourceReadException,
  MessageMediaResourceReadRequest
}
import com.dixa.twilio.client.{messaging, ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging.{Media, MediaResourceReference, Message}
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model.{HttpMethod, HttpMethods, HttpRequest, HttpResponse}
import org.apache.pekko.stream.Materializer

import java.time.Instant
import scala.concurrent.ExecutionContext
import scala.util.Try

private[impl] class ChannelSenderFetchRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ChannelSenderFetchRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Messaging

  override protected def method: HttpMethod = HttpMethods.GET

  override def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ChannelSenderFetchRequestExecutor.ChannelSenderFetchRequest
  ): Either[ChannelSenderFetchException, HttpRequest] = {
    createHttpRequestFor(
      s"/${ApiVersion.V2}/Channels/Senders/${req.channelSenderSid}",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): ChannelSenderFetchException.Api =
    ChannelSenderFetchException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ChannelSenderFetchException.Unspecified = ChannelSenderFetchException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: MessageMediaResourceReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[MessageMediaResourceReadException, MediaResourceReference]] = {
    responseEntity.parse[MediaResourceListJsonRep]() match {
      case Left(ex) =>
        List(
          Left(
            MessageMediaResourceReadException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause))
          )
        )
      case Right(decoded: MediaResourceListJsonRep) =>
        decoded.media_list.map { jsonRep =>
          Right(jsonRep.toModel(request.messageSid, connectionSettings))
        }
    }
  }
}
