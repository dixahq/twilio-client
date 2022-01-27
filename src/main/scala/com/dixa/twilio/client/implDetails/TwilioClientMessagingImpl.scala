package com.dixa.twilio.client.implDetails

import akka.{Done, NotUsed}
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.implDetails.request.messaging.{
  PhoneNumberCreateRequest,
  PhoneNumberDeleteRequest,
  ServiceCreateRequest,
  ServicesReadRequest
}
import com.dixa.twilio.client.model.messaging.{TwilioMessagingPhoneNumber, TwilioMessagingService}
import com.dixa.twilio.client.{TwilioClientMessaging, TwilioConnectionSettings}

import scala.concurrent.{ExecutionContext, Future}

final class TwilioClientMessagingImpl(
    implicit httpExt: HttpExt,
    materializer: Materializer,
    executionContext: ExecutionContext
) extends TwilioClientMessaging {

  override def readServices(
      conSettings: TwilioConnectionSettings
  ): Source[TwilioMessagingService, NotUsed] = {
    new ServicesReadRequest().apply(conSettings)
  }

  override def createService(
      conSettings: TwilioConnectionSettings,
      toCreate: TwilioClientMessaging.ServiceCreateRequest
  ): Future[TwilioMessagingService] = {
    new ServiceCreateRequest().apply(conSettings, toCreate)
  }

  override def createPhoneNumber(
      conSettings: TwilioConnectionSettings,
      toCreate: TwilioClientMessaging.PhoneNumberCreateRequest
  ): Future[TwilioMessagingPhoneNumber] = {
    new PhoneNumberCreateRequest().apply(conSettings, toCreate)
  }

  override def deletePhoneNumber(
      connectionSettings: TwilioConnectionSettings,
      toDelete: TwilioClientMessaging.PhoneNumberDeleteRequest
  ): Future[Done] = {
    new PhoneNumberDeleteRequest().apply(connectionSettings, toDelete)
  }
}
