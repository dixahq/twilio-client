package com.dixa.twilio.client.phonenumber

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.phonenumber.ActiveNumbersReadRequestExecutor.ActiveNumbersReadException
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.phonenumber.{TwilioActivePhoneNumber, TwilioPhoneNumber}

trait ActiveNumbersReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      ActiveNumbersReadRequestExecutor.ActiveNumbersReadRequest,
      ActiveNumbersReadRequestExecutor.ActiveNumbersReadException,
      TwilioActivePhoneNumber,
      ActiveNumbersReadRequestExecutor.ActiveNumbersReadRequest.Builder
    ] {
  override protected final type ApiExceptionWrapper = ActiveNumbersReadException.Api

  override protected final type UnspecifiedException = ActiveNumbersReadException.Unspecified

  override protected final def createBuilderStartState()
      : ActiveNumbersReadRequestExecutor.ActiveNumbersReadRequest.Builder =
    ActiveNumbersReadRequestExecutor.ActiveNumbersReadRequest.Builder.empty
}

object ActiveNumbersReadRequestExecutor {
  final case class ActiveNumbersReadRequest(
      phoneNumberSid: Option[TwilioPhoneNumber.Sid]
  )
  object ActiveNumbersReadRequest {
    type BuilderStartState = Builder

    final class Builder private[phonenumber] (phoneNumberSid: Option[TwilioPhoneNumber.Sid]) {
      def withPhoneNumberSid(phoneNumberSid: TwilioPhoneNumber.Sid): Builder =
        new Builder(Some(phoneNumberSid))
      def build(): ActiveNumbersReadRequest = ActiveNumbersReadRequest(phoneNumberSid)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(None)
    }

    def build(
        fun: BuilderStartState => ActiveNumbersReadRequest
    ): ActiveNumbersReadRequest = fun(Builder.empty)
  }

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
