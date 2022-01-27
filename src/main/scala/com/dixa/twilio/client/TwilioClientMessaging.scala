package com.dixa.twilio.client

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.model.messaging.TwilioMessagingService.{
  FallbackWebhook,
  FriendlyName,
  InboundRequestWebhook,
  StatusCallback,
  UseInboundWebhookOnNumber
}
import com.dixa.twilio.client.model.messaging.{TwilioMessagingPhoneNumber, TwilioMessagingService}
import com.dixa.twilio.client.model.phonenumber.ActiveNumber

import scala.concurrent.Future

trait TwilioClientMessaging {

  def readServices(
      conSettings: TwilioConnectionSettings
  ): Source[TwilioMessagingService, NotUsed]

  def createService(
      conSettings: TwilioConnectionSettings,
      toCreate: TwilioClientMessaging.ServiceCreateRequest
  ): Future[TwilioMessagingService]

  def createPhoneNumber(
      conSettings: TwilioConnectionSettings,
      toCreate: TwilioClientMessaging.PhoneNumberCreateRequest
  ): Future[TwilioMessagingPhoneNumber]
}

object TwilioClientMessaging {

  final case class ServiceCreateRequest(
      friendlyName: FriendlyName,
      inboundRequestWebhook: Option[InboundRequestWebhook],
      fallbackWebhook: Option[FallbackWebhook],
      statusCallback: Option[StatusCallback],
      useInboundWebhookOnNumber: UseInboundWebhookOnNumber
  )

  final case class PhoneNumberCreateRequest(
      serviceSid: TwilioMessagingService.Sid,
      activeNumberSid: ActiveNumber.Sid
  )
}
