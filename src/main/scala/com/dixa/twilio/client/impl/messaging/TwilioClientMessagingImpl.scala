package com.dixa.twilio.client.impl.messaging

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpRequest
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.client.impl.HttpEntityString
import com.dixa.twilio.client.messaging.MessageResourceReadSource.MessageResourceReadException
import com.dixa.twilio.client.messaging.{
  MessageResourceReadSource,
  MessageSendRequestExecutor,
  PhoneNumberCreateRequestExecutor,
  PhoneNumberDeleteRequestExecutor,
  TwilioClientMessaging
}
import com.dixa.twilio.model.messaging.{
  MediaResourceReference,
  MessageResource,
  TwilioMessagingService
}

import scala.concurrent.{ExecutionContext, Future}

private[client] final class TwilioClientMessagingImpl(
    implicit httpExt: HttpExt,
    materializer: Materializer,
    executionContext: ExecutionContext
) extends TwilioClientMessaging {

  override def servicesRead(
      connSettings: TwilioConnectionSettings
  ): Source[TwilioMessagingService, NotUsed] = {
    new ServicesReadRequest().apply(connSettings)
  }

  override def serviceCreate(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.ServiceCreateRequest
  ): Future[TwilioMessagingService] = {
    new ServiceCreateRequest().apply(connSettings, req)
  }

  override val phoneNumberCreate: PhoneNumberCreateRequestExecutor =
    new PhoneNumberCreateRequestExecutorImpl()

  override val phoneNumberDelete: PhoneNumberDeleteRequestExecutor =
    new PhoneNumberDeleteRequestExecutorImpl()

  override val messageSend: MessageSendRequestExecutor = new MessageSendRequestExecutorImpl()

  override def mediaResourceRead(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.MediaResourceReadRequest
  ): Source[MediaResourceReference, NotUsed] = {
    MediaResourceReadSource(connSettings, req)
  }

  override val messageResourceRead: MessageResourceReadSource =
    new MessageResourceReadSourceImpl()
}
