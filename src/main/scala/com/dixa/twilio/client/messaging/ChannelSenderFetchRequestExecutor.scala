package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor, SingleRequestExecutor}
import com.dixa.twilio.client.messaging.MessageResourceReadRequestExecutor.MessageResourceReadException
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging._

import java.time.Instant

trait ChannelSenderFetchRequestExecutor
    extends SingleRequestExecutor[
      ChannelSenderFetchRequestExecutor.ChannelSenderRequest,
      ChannelSenderFetchRequestExecutor.ChannelSenderReadException,
      ChannelSender
    ] {

  override protected type ApiExceptionWrapper = MessageResourceReadException.Api

  override protected type UnspecifiedException = MessageResourceReadException.Unspecified
}

object ChannelSenderFetchRequestExecutor {

  final case class ChannelSenderRequest(
      accountSid: ChannelSender.Sid,
  )

  sealed trait ChannelSenderReadException extends RuntimeException
  object ChannelSenderReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ChannelSenderReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch channel sender resource"
          ),
          cause.orNull
        )
        with ChannelSenderReadException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }

}
