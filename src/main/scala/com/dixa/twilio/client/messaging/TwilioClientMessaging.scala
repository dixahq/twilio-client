package com.dixa.twilio.client.messaging

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.model.messaging.{
  MediaResourceReference,
  MessageSid,
  StatusCallback,
  TwilioMessagingService
}
import com.dixa.twilio.model.messaging.TwilioMessagingService.{
  FallbackWebhook,
  FriendlyName,
  InboundRequestWebhook,
  UseInboundWebhookOnNumber
}

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
  def phoneNumberDelete: PhoneNumberDeleteRequestExecutor

  def messageSend: MessageSendRequestExecutor

  /** Lists the media resources from a given account and message sid.
    */
  @deprecated("Use mediaResourceReadV2 instead", "0.11.0")
  def mediaResourceRead(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.MediaResourceReadRequest
  ): Source[MediaResourceReference, NotUsed]

  /** Lists the media resources from a given account and message sid as a safe source.
    */
  def mediaResourceReadV2: MessageResourceReadRequestExecutor

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
