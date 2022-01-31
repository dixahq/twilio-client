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
      connSettings: TwilioConnectionSettings
  ): Source[TwilioMessagingService, NotUsed]

  def serviceCreate(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.ServiceCreateRequest
  ): Future[TwilioMessagingService]

  /** Add a phone number to a messaging service.
    *
    * The create is only in context of messaging. So it takes an existing phonenumber and creates it
    * / adds it in a messaging service.
    */
  def phoneNumberCreate(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.PhoneNumberCreateRequest
  ): Future[TwilioMessagingPhoneNumber]

  /** Delete a phone number from a messaging service.
    *
    * The delete is only in context of messaging. So it takes an existing phonenumber and delete it
    * / removes it from a messaging service.
    */
  def phoneNumberDelete(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.PhoneNumberDeleteRequest
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
