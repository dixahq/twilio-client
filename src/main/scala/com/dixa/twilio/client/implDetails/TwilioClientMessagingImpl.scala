package com.dixa.twilio.client.implDetails

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.implDetails.request.messaging.{
  CreateServiceRequest,
  PhoneNumberCreateRequest,
  ReadServicesRequest
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
    new ReadServicesRequest().apply(conSettings)
  }

  override def createService(
      conSettings: TwilioConnectionSettings,
      toCreate: TwilioClientMessaging.ServiceCreateRequest
  ): Future[TwilioMessagingService] = {
    new CreateServiceRequest().apply(conSettings, toCreate)
  }

  override def createPhoneNumber(
      conSettings: TwilioConnectionSettings,
      toCreate: TwilioClientMessaging.PhoneNumberCreateRequest
  ): Future[TwilioMessagingPhoneNumber] = {
    new PhoneNumberCreateRequest().apply(conSettings, toCreate)
  }
}
