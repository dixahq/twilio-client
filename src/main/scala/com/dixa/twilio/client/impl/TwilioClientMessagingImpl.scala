package com.dixa.twilio.client.impl

import akka.{Done, NotUsed}
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.impl.request.messaging.{
  PhoneNumberCreateRequest,
  PhoneNumberDeleteRequest,
  ServiceCreateRequest,
  ServicesReadRequest
}
import com.dixa.twilio.client.model.messaging.{TwilioMessagingPhoneNumber, TwilioMessagingService}
import com.dixa.twilio.client.{TwilioClientMessaging, TwilioConnectionSettings}

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

  override def phoneNumberCreate(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.PhoneNumberCreateRequest
  ): Future[Either[TwilioClientMessaging.PhoneNumberCreateException, TwilioMessagingPhoneNumber]] =
    new PhoneNumberCreateRequest().apply(connSettings, req)

  override def phoneNumberCreateUnsafe(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.PhoneNumberCreateRequest
  ): Future[TwilioMessagingPhoneNumber] =
    new PhoneNumberCreateRequest().apply(connSettings, req).map(_.fold(e => throw e, res => res))

  override def phoneNumberDelete(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.PhoneNumberDeleteRequest
  ): Future[Done] = {
    new PhoneNumberDeleteRequest().apply(connSettings, req)
  }
}
