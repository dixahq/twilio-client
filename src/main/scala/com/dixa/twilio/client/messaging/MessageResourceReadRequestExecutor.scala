package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.messaging.MessageResourceReadRequestExecutor.MessageResourceReadException
import com.dixa.twilio.client.{ApiException, MultipleResponseSource}
import com.dixa.twilio.model.messaging.{MediaResourceReference, MessageSid}

trait MessageResourceReadRequestExecutor
    extends MultipleResponseSource[
      MessageResourceReadRequestExecutor.MessageResourceReadRequest,
      MessageResourceReadRequestExecutor.MessageResourceReadException,
      MediaResourceReference
    ] {
  override protected final type ApiExceptionWrapper = MessageResourceReadException.Api

  override protected final type UnspecifiedException = MessageResourceReadException.Unspecified

}

object MessageResourceReadRequestExecutor {
  final case class MessageResourceReadRequest(
      messageSid: MessageSid
  )

  sealed trait MessageResourceReadException extends RuntimeException
  object MessageResourceReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with MessageResourceReadException
    final case class Unspecified(msg: Option[String], cause: Option[Exception])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read message resources"
          ),
          cause.orNull
        )
        with MessageResourceReadException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Exception) = this(None, Some(cause))
    }
  }
}
