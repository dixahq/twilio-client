package com.dixa.twilio.client.messaging

import akka.NotUsed
import akka.http.scaladsl.model.DateTime
import akka.http.scaladsl.model.Uri.Query
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging.{
  MediaResourceReference,
  MessageResource,
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
import com.dixa.twilio.model.phonenumber.PhoneNumberE164

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
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
  def mediaResourceRead(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.MediaResourceReadRequest
  ): Source[MediaResourceReference, NotUsed]

  /** Returns a Source of messages from a given account, can be filtered based on to and/or from
    * phone number and sent date
    */
  def messageResourceRead(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.MessageResourceReadRequest
  ): Source[MessageResource, NotUsed]

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

  final case class MessageResourceReadRequest(
      accountSid: TwilioAccount.Sid,
      filter: MessageResourcesReadRequestFilter = MessageResourcesReadRequestFilter()
  )

  final case class MessageResourcesReadRequestFilter(
      to: Option[PhoneNumberE164] = None,
      from: Option[PhoneNumberE164] = None,
      dateSent: Option[DateTime] = None,
      pageSize: Int = 20
  ) {
    def buildFilterQuery: Query = {
      val dateSentParameter: Option[(String, String)] = dateSent.map { date =>
        "DateSent" -> date.toString
      }
      val toParameter: Option[(String, String)] = to.map { number => "To" -> number.toString }
      val fromParameter: Option[(String, String)] = from.map { number =>
        "From=" -> number.toString
      }

      Query(
        Map("PageSize" -> pageSize.toString) ++
          List(dateSentParameter, toParameter, fromParameter).flatten.toMap
      )
    }
  }
}
