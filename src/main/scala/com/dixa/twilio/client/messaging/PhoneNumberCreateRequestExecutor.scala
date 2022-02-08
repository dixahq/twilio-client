package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.SingleRequestExecutor
import com.dixa.twilio.client.model.messaging.{TwilioMessagingPhoneNumber, TwilioMessagingService}
import com.dixa.twilio.client.model.phonenumber.TwilioPhoneNumberSid

trait PhoneNumberCreateRequestExecutor
    extends SingleRequestExecutor[
      PhoneNumberCreateRequestExecutor.PhoneNumberCreateRequest,
      PhoneNumberCreateRequestExecutor.PhoneNumberCreateException,
      TwilioMessagingPhoneNumber
    ]

object PhoneNumberCreateRequestExecutor {

  final case class PhoneNumberCreateRequest(
      serviceSid: TwilioMessagingService.Sid,
      phoneNumberSid: TwilioPhoneNumberSid
  )

  sealed trait PhoneNumberCreateException extends RuntimeException
  object PhoneNumberCreateException {
    final case class PhoneNumberAlreadyInMessagingService()
        extends IllegalStateException(
          "Phone Number or Short Code is already in the Messaging Service. More info: https://www.twilio.com/docs/errors/21710"
        )
        with PhoneNumberCreateException
    final case class PhoneNumberAssociatedWithOtherMessagingService()
        extends IllegalStateException(
          "Phone Number or Short Code is associated with another Messaging Service. More info: https://www.twilio.com/docs/errors/21712"
        )
        with PhoneNumberCreateException
    final case class UnspecifiedError(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to add phone number to Messaging Service"
          ),
          cause.orNull
        )
        with PhoneNumberCreateException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }
}
