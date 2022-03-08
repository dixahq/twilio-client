package com.dixa.twilio.client.impl.messaging

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.impl.Formatter.dateTime
import com.dixa.twilio.client.impl.{ApiSubDomain, TwilioPagingFlow}
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.messaging.TwilioClientMessaging.MediaResourceReadRequest
import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.model.messaging.MediaResourceUrl.buildMediaResourcePath
import com.dixa.twilio.client.model.messaging.{MediaResourceReference, MediaSid, MessageSid}
import io.circe.generic.auto._

import java.time.Instant
import scala.concurrent.ExecutionContext
import scala.util.Try

private[impl] object MediaResourceReadSource {

  def apply(
      connSettings: TwilioConnectionSettings,
      req: MediaResourceReadRequest
  )(
      implicit http: HttpExt,
      materializer: Materializer,
      executionContext: ExecutionContext
  ): Source[MediaResourceReference, NotUsed] = {
    val requestPath = buildMediaResourcePath(connSettings.accountSid, req.messageSid)
    TwilioPagingFlow
      .createPagingSrc(
        connSettings,
        TwilioPath(ApiSubDomain.Api, HttpMethods.GET, requestPath)
      )
      .mapConcat(_.parseUnsafe[MediaResourceListJsonRep].media_list)
      .map(_.toModel)
  }

  private final case class MediaResourceListJsonRep(
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

  private final case class MediaResourcesReferenceJsonRep(
      sid: String,
      account_sid: String,
      parent_sid: String,
      content_type: String,
      date_created: String,
      date_updated: String,
      uri: String
  ) {
    def toModel: MediaResourceReference =
      MediaResourceReference(
        sid = MediaSid(sid),
        accountSid = TwilioAccount.Sid(account_sid),
        parentSid = MessageSid(parent_sid),
        contentType = content_type,
        dateCreated = Try(Instant.from(dateTime.parse(date_created))).getOrElse(Instant.now),
        dateUpdated = Try(Instant.from(dateTime.parse(date_updated))).getOrElse(Instant.now)
      )
  }
}
