package com.dixa.twilio.client.impl.messaging

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.messaging.{
  ChannelSenderCreateRequestExecutor,
  ChannelSenderFetchRequestExecutor,
  MessageMediaResourceReadRequestExecutor,
  MessageResourceReadRequestExecutor,
  MessageSendRequestExecutor,
  PhoneNumberCreateRequestExecutor,
  PhoneNumberDeleteRequestExecutor,
  ServicesReadRequestExecutor,
  TwilioClientMessaging
}
import com.dixa.twilio.model.messaging.TwilioMessagingService

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

  override val mediaResourceRead: MessageMediaResourceReadRequestExecutor =
    new MessageMediaResourceReadRequestExecutorImpl()

  override val messageResourceRead: MessageResourceReadRequestExecutor =
    new MessageResourceReadRequestExecutorImpl()

  override val channelSenderFetch: ChannelSenderFetchRequestExecutor =
    new ChannelSenderFetchRequestExecutorImpl()

  override val channelSenderCreate: ChannelSenderCreateRequestExecutor =
    new ChannelSenderCreateRequestExecutorImpl()
}
