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

  import TwilioClientMessaging._

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
    *
    * This method will always return a successfully Future, with an Either having errors on its left
    * side.
    */
  def phoneNumberCreate(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.PhoneNumberCreateRequest
  ): Future[Either[PhoneNumberCreateException, TwilioMessagingPhoneNumber]]

  /** Add a phone number to a messaging service.
    *
    * The create is only in context of messaging. So it takes an existing phonenumber and creates it
    * / adds it in a messaging service.
    *
    * This method will return a Failed future in case of errors.
    */
  def phoneNumberCreateUnsafe(
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

  sealed trait PhoneNumberCreateException extends RuntimeException
  object PhoneNumberCreateException {
    final class PhoneNumberAlreadyInMessagingService
        extends IllegalStateException(
          "Phone Number or Short Code is already in the Messaging Service. More info: https://www.twilio.com/docs/errors/21710"
        )
        with PhoneNumberCreateException
    final class PhoneNumberAssociatedWithOtherMessagingService
        extends IllegalStateException(
          "Phone Number or Short Code is associated with another Messaging Service. More info: https://www.twilio.com/docs/errors/21712"
        )
        with PhoneNumberCreateException
    final class UnspecifiedError(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to add phone number to Messaging Service"
          ),
          cause.orNull
        )
        with PhoneNumberCreateException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(None, Some(cause))
    }
  }

  final case class PhoneNumberDeleteRequest(
      serviceSid: TwilioMessagingService.Sid,
      activeNumberSid: ActiveNumber.Sid
  )
}
