package com.dixa.twilio.client.messaging

import akka.Done
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.client.model.messaging.TwilioMessagingService
import com.dixa.twilio.client.model.phonenumber.TwilioPhoneNumberSid

trait PhoneNumberDeleteRequestExecutor
    extends SingleRequestExecutor[
      PhoneNumberDeleteRequestExecutor.PhoneNumberDeleteRequest,
      PhoneNumberDeleteRequestExecutor.PhoneNumberDeleteException,
      Done
    ]

object PhoneNumberDeleteRequestExecutor {

  final case class PhoneNumberDeleteRequest(
      serviceSid: TwilioMessagingService.Sid,
      phoneNumberSid: TwilioPhoneNumberSid
  )

  sealed trait PhoneNumberDeleteException extends RuntimeException
  object PhoneNumberDeleteException {
    final case class API(cause: ApiException)
        extends RuntimeException(cause)
        with PhoneNumberDeleteException

    final case class NotFound(msg: String)
        extends IllegalStateException(msg)
        with PhoneNumberDeleteException

    final case class UnspecifiedError(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to add phone number to Messaging Service"
          ),
          cause.orNull
        )
        with PhoneNumberDeleteException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }
}
