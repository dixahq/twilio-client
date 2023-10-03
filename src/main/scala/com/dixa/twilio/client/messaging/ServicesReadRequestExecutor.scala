package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.client.messaging.ServicesReadRequestExecutor.ServicesReadException
import com.dixa.twilio.model.messaging.TwilioMessagingService

trait ServicesReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      ServicesReadRequestExecutor.ServicesReadRequest,
      ServicesReadRequestExecutor.ServicesReadException,
      TwilioMessagingService
    ] {
  override protected final type ApiExceptionWrapper = ServicesReadException.Api

  override protected final type UnspecifiedException = ServicesReadException.Unspecified

}

object ServicesReadRequestExecutor {
  final case class ServicesReadRequest()

  sealed trait ServicesReadException extends RuntimeException
  object ServicesReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ServicesReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read services"
          ),
          cause.orNull
        )
        with ServicesReadException
  }
}
