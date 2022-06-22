package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.model.messaging.{MessageSid, StatusCallback, TwilioMessagingService}
import com.dixa.twilio.model.messaging.TwilioMessagingService.{
  FallbackWebhook,
  FriendlyName,
  InboundRequestWebhook,
  UseInboundWebhookOnNumber
}
import scala.concurrent.Future

trait TwilioClientMessaging {

  def servicesRead: ServicesReadRequestExecutor

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
  def phoneNumberDelete: PhoneNumberDeleteRequestExecutor

  def messageSend: MessageSendRequestExecutor

  /** Lists the media resources from a given account and message sid.
    */
  def mediaResourceRead: MessageMediaResourceReadRequestExecutor

  /** Returns a Source of messages from a given account, can be filtered based on to and/or from
    * phone number and sent date
    */
  def messageResourceRead: MessageResourceReadRequestExecutor
}

object TwilioClientMessaging {

  final case class ServiceCreateRequest(
      friendlyName: FriendlyName,
      inboundRequestWebhook: Option[InboundRequestWebhook],
      fallbackWebhook: Option[FallbackWebhook],
      statusCallback: Option[StatusCallback],
      useInboundWebhookOnNumber: UseInboundWebhookOnNumber
  )

  final case class MediaResourceReadRequest(messageSid: MessageSid)

}
