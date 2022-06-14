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
  MessageResourceReadRequestExecutor,
  MessageResourceReadSource,
  MessageSendRequestExecutor,
  PhoneNumberCreateRequestExecutor,
  PhoneNumberDeleteRequestExecutor,
  ServicesReadRequestExecutor,
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

  override val servicesRead: ServicesReadRequestExecutor = new ServicesReadRequestExecutorImpl()

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

  override val messageResourceRead: MessageResourceReadSource =
    new MessageResourceReadSourceImpl()
  override val mediaResourceRead: MessageResourceReadRequestExecutor =
    new MessageResourceReadRequestExecutorImpl()
}
