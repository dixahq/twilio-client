package com.dixa.twilio.client.impl.messaging

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.stream.Materializer
import com.dixa.twilio.client.impl.Formatter.dateTime
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.messaging.MediaResourceUrlFactory.buildMediaResourcePath
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, TwilioUri}
import com.dixa.twilio.client.messaging.MessageMediaResourceReadRequestExecutor
import com.dixa.twilio.client.messaging.MessageMediaResourceReadRequestExecutor.{
  MessageMediaResourceReadException,
  MessageMediaResourceReadRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging.{MediaResourceReference, MediaSid, MessageSid}
import io.circe.generic.auto._

import java.time.Instant
import scala.concurrent.ExecutionContext
import scala.util.Try

private[impl] class MessageMediaResourceReadRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends MessageMediaResourceReadRequestExecutor {

  override def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: MessageMediaResourceReadRequestExecutor.MessageMediaResourceReadRequest
  ): Either[MessageMediaResourceReadException, HttpRequest] = {
    val requestPath = buildMediaResourcePath(connSettings.accountSid, req.messageSid)
    Right(
      TwilioPath(
        ApiSubDomain.Api,
        HttpMethods.GET,
        requestPath
      ).createHttpRequest(connSettings)
    )
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    MessageMediaResourceReadException.Api.apply(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Exception]
  ): UnspecifiedException = MessageMediaResourceReadException.Unspecified(msg, cause)

  private case class MediaResourceListJsonRep(
      first_page_uri: String,
      end: Int,
      media_list: List[MediaResourcesReferenceJsonRep],
      previous_page_uri: Option[String],
      uri: String,
      page_size: Int,
      start: Int,
      next_page_uri: Option[String],
      page: Int
  )

  private case class MediaResourcesReferenceJsonRep(
      sid: String,
      account_sid: String,
      parent_sid: String,
      content_type: String,
      date_created: String,
      date_updated: String,
      uri: String
  ) {
    def toModel(
        messageSid: MessageSid,
        connSettings: TwilioConnectionSettings
    ): MediaResourceReference = {
      val accountSid = TwilioAccount.Sid(account_sid)
      val mediaSid   = MediaSid(sid)
      MediaResourceReference(
        sid = mediaSid,
        accountSid = accountSid,
        parentSid = MessageSid(parent_sid),
        contentType = content_type,
        dateCreated = Try(Instant.from(dateTime.parse(date_created))).getOrElse(Instant.now),
        dateUpdated = Try(Instant.from(dateTime.parse(date_updated))).getOrElse(Instant.now),
        MediaResourceUrlFactory.resourceUrl(accountSid, messageSid, mediaSid, connSettings)
      )
    }
  }

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

  private case class TwilioResponseNextPageJsonRep(next_page_uri: Option[String])

  override protected def nextPageHttpRequestBuilder(
      connectionSettings: TwilioConnectionSettings,
      entityString: HttpEntityString
  ): Either[UnspecifiedException, Option[HttpRequest]] = {
    entityString
      .parse[TwilioResponseNextPageJsonRep]()
      .left
      .map { ex =>
        createUnspecifiedException(Some(ex.getMessage), Some(ex.cause))
      }
      .map { response =>
        response.next_page_uri.map { nextPage =>
          TwilioUri
            .autoDetect(nextPage, HttpMethods.GET, ApiSubDomain.Api)
            .createHttpRequest(connectionSettings)
        }
      }
  }
}
