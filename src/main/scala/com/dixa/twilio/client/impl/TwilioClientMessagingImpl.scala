package com.dixa.twilio.client.impl

import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import com.dixa.twilio.client.impl.request.messaging.{
  PhoneNumberCreateRequestClientImpl,
  PhoneNumberDeleteRequest,
  ServiceCreateRequest,
  ServicesReadRequest
}
import com.dixa.twilio.client.messaging.{PhoneNumberCreateRequestClient, TwilioClientMessaging}
import com.dixa.twilio.client.model.messaging.TwilioMessagingService
import com.dixa.twilio.client.TwilioConnectionSettings

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

  override def phoneNumberCreate(): PhoneNumberCreateRequestClient =
    new PhoneNumberCreateRequestClientImpl()

  override def phoneNumberDelete(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.PhoneNumberDeleteRequest
  ): Future[Done] = {
    new PhoneNumberDeleteRequest().apply(connSettings, req)
  }
}
