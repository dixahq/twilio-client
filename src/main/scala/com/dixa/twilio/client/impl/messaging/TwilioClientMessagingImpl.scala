package com.dixa.twilio.client.impl.messaging

import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.messaging.{
  PhoneNumberCreateRequestExecutor,
  PhoneNumberDeleteRequestExecutor,
  TwilioClientMessaging
}
import com.dixa.twilio.client.model.messaging.TwilioMessagingService

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
}
