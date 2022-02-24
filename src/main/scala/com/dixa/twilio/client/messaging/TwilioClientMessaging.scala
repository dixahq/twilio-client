package com.dixa.twilio.client.messaging

import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.model.messaging.TwilioMessagingService.{
  FallbackWebhook,
  FriendlyName,
  InboundRequestWebhook,
  UseInboundWebhookOnNumber
}
import com.dixa.twilio.client.model.messaging.{StatusCallback, TwilioMessagingService}
import com.dixa.twilio.client.model.phonenumber.TwilioPhoneNumberSid

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
    * The create is only in context of messaging. So it takes an existing phone number and creates
    * it / adds it in a messaging service.
    */
  def phoneNumberCreate: PhoneNumberCreateRequestExecutor

  /** Delete a phone number from a messaging service.
    *
    * The delete is only in context of messaging. So it takes an existing phonenumber and delete it
    * / removes it from a messaging service.
    */
  def phoneNumberDelete(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.PhoneNumberDeleteRequest
  ): Future[Done]

  def smsSend: SmsSendRequestExecutor
}

object TwilioClientMessaging {

  final case class ServiceCreateRequest(
      friendlyName: FriendlyName,
      inboundRequestWebhook: Option[InboundRequestWebhook],
      fallbackWebhook: Option[FallbackWebhook],
      statusCallback: Option[StatusCallback],
      useInboundWebhookOnNumber: UseInboundWebhookOnNumber
  )

  final case class PhoneNumberDeleteRequest(
      serviceSid: TwilioMessagingService.Sid,
      phoneNumberSid: TwilioPhoneNumberSid
  )
}
