package com.dixa.twilio.client.phonenumber

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.phonenumber.ActiveNumbersReadRequestExecutor.ActiveNumbersReadException
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.phonenumber.{TwilioActivePhoneNumber, TwilioPhoneNumber}

trait ActiveNumbersReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      ActiveNumbersReadRequestExecutor.ActiveNumbersReadRequest,
      ActiveNumbersReadRequestExecutor.ActiveNumbersReadException,
      TwilioActivePhoneNumber
    ] {
  override protected final type ApiExceptionWrapper = ActiveNumbersReadException.Api

  override protected final type UnspecifiedException = ActiveNumbersReadException.Unspecified

}

object ActiveNumbersReadRequestExecutor {
  final case class ActiveNumbersReadRequest(
      phoneNumberSid: Option[TwilioPhoneNumber.Sid]
  )

  sealed trait ActiveNumbersReadException extends RuntimeException
  object ActiveNumbersReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ActiveNumbersReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read active numbers"
          ),
          cause.orNull
        )
        with ActiveNumbersReadException {}
  }
}
