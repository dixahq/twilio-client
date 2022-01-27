package com.dixa.twilio.client

import akka.{Done, NotUsed}
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

  def servicesRead(
      conSettings: TwilioConnectionSettings
  ): Source[TwilioMessagingService, NotUsed]

  def serviceCreate(
      conSettings: TwilioConnectionSettings,
      toCreate: TwilioClientMessaging.ServiceCreateRequest
  ): Future[TwilioMessagingService]

  def phoneNumberCreate(
      conSettings: TwilioConnectionSettings,
      toCreate: TwilioClientMessaging.PhoneNumberCreateRequest
  ): Future[TwilioMessagingPhoneNumber]

  def phoneNumberDelete(
      connectionSettings: TwilioConnectionSettings,
      toDelete: TwilioClientMessaging.PhoneNumberDeleteRequest
  ): Future[Done]
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

  final case class PhoneNumberDeleteRequest(
      serviceSid: TwilioMessagingService.Sid,
      activeNumberSid: ActiveNumber.Sid
  )
}
