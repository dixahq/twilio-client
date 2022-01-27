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
      toCreate: TwilioClientMessaging.ServiceCreateRequest
  ): Future[TwilioMessagingService] = {
    new ServiceCreateRequest().apply(connSettings, toCreate)
  }

  override def phoneNumberCreate(
      connSettings: TwilioConnectionSettings,
      toCreate: TwilioClientMessaging.PhoneNumberCreateRequest
  ): Future[TwilioMessagingPhoneNumber] = {
    new PhoneNumberCreateRequest().apply(connSettings, toCreate)
  }

  override def phoneNumberDelete(
      connSettings: TwilioConnectionSettings,
      toDelete: TwilioClientMessaging.PhoneNumberDeleteRequest
  ): Future[Done] = {
    new PhoneNumberDeleteRequest().apply(connSettings, toDelete)
  }
}
