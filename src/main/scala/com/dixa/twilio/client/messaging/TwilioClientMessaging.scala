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

  val servicesRead: ServicesReadRequestExecutor

  def serviceCreate(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.ServiceCreateRequest
  ): Future[TwilioMessagingService]

  /** Add a phone number to a messaging service.
    *
    * The create is only in context of messaging. So it takes an existing phone number and creates
    * it / adds it in a messaging service.
    */
  val phoneNumberCreate: PhoneNumberCreateRequestExecutor

  /** Delete a phone number from a messaging service.
    *
    * The delete is only in context of messaging. So it takes an existing phonenumber and delete it
    * / removes it from a messaging service.
    */
  val phoneNumberDelete: PhoneNumberDeleteRequestExecutor

  val messageSend: MessageSendRequestExecutor

  /** Lists the media resources from a given account and message sid.
    */
  val mediaResourceRead: MessageMediaResourceReadRequestExecutor

  /** Returns a Source of messages from a given account, can be filtered based on to and/or from
    * phone number and sent date
    */
  val messageResourceRead: MessageResourceReadRequestExecutor
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
