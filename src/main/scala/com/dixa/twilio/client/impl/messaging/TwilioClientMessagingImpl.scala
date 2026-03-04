package com.dixa.twilio.client.impl.messaging

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.messaging.{
  ChannelsSendersCreateRequestExecutor,
  ChannelsSendersDeleteRequestExecutor,
  ChannelsSendersFetchRequestExecutor,
  ChannelsSendersListRequestExecutor,
  ChannelsSendersVerificationRequestExecutor,
  MessageMediaResourceReadRequestExecutor,
  MessageResourceReadRequestExecutor,
  MessageSendRequestExecutor,
  PhoneNumberCreateRequestExecutor,
  PhoneNumberDeleteRequestExecutor,
  ServicesReadRequestExecutor,
  TwilioClientMessaging,
  TypingIndicatorSendRequestExecutor
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

  override val channelsSendersFetch: ChannelsSendersFetchRequestExecutor =
    new ChannelsSendersFetchRequestExecutorImpl()

  override val channelsSendersCreate: ChannelsSendersCreateRequestExecutor =
    new ChannelsSendersCreateRequestExecutorImpl()

  override val channelsSendersVerification: ChannelsSendersVerificationRequestExecutor =
    new ChannelsSendersVerificationRequestExecutorImpl()

  override val channelsSendersDelete: ChannelsSendersDeleteRequestExecutor =
    new ChannelsSendersDeleteRequestExecutorImpl()

  override val channelsSendersList: ChannelsSendersListRequestExecutor =
    new ChannelsSendersListRequestExecutorImpl()

  override val typingIndicatorSend: TypingIndicatorSendRequestExecutor =
    new TypingIndicatorSendRequestExecutorImpl()
}
