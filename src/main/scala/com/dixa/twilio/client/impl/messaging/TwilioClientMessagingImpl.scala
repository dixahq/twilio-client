package com.dixa.twilio.client.impl.messaging

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.messaging.{
  MessageResourceReadRequestExecutor,
  MessageSendRequestExecutor,
  PhoneNumberCreateRequestExecutor,
  PhoneNumberDeleteRequestExecutor,
  TwilioClientMessaging
}
import com.dixa.twilio.model.messaging.{MediaResourceReference, TwilioMessagingService}

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

  override val mediaResourceReadV2: MessageResourceReadRequestExecutor =
    new MessageResourceReadRequestExecutorImpl()
}
