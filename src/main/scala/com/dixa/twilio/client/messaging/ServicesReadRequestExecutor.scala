package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.{ApiException, MultipleResponseSource}
import com.dixa.twilio.client.messaging.ServicesReadRequestExecutor.ServicesReadException
import com.dixa.twilio.model.messaging.TwilioMessagingService

trait ServicesReadRequestExecutor
    extends MultipleResponseSource[
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
    final case class Unspecified(msg: Option[String], cause: Option[Exception])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read services"
          ),
          cause.orNull
        )
        with ServicesReadException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Exception) = this(None, Some(cause))
    }
  }
}
