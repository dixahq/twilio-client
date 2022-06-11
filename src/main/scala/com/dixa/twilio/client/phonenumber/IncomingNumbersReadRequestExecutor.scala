package com.dixa.twilio.client.phonenumber

import com.dixa.twilio.client.{ApiException, MultipleResponseSource}
import com.dixa.twilio.client.phonenumber.IncomingNumbersReadRequestExecutor.IncomingNumbersReadException
import com.dixa.twilio.model.phonenumber.TwilioIncomingPhoneNumber

trait IncomingNumbersReadRequestExecutor
    extends MultipleResponseSource[
      IncomingNumbersReadRequestExecutor.IncomingNumbersReadRequest,
      IncomingNumbersReadRequestExecutor.IncomingNumbersReadException,
      TwilioIncomingPhoneNumber
    ] {
  override protected final type ApiExceptionWrapper = IncomingNumbersReadException.Api

  override protected final type UnspecifiedException = IncomingNumbersReadException.Unspecified

}

object IncomingNumbersReadRequestExecutor {
  final case class IncomingNumbersReadRequest(
      filter: Option[TwilioIncomingPhoneNumber.PhoneNumberFilter]
  )

  sealed trait IncomingNumbersReadException extends RuntimeException
  object IncomingNumbersReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with IncomingNumbersReadException
    final case class Unspecified(msg: Option[String], cause: Option[Exception])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read incoming numbers"
          ),
          cause.orNull
        )
        with IncomingNumbersReadException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Exception) = this(None, Some(cause))
    }
  }
}
