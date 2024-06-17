package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.messaging.ChannelSenderCreateRequestExecutor.ChannelSenderCreateException
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.messaging._

trait ChannelSenderCreateRequestExecutor
    extends SingleRequestExecutor[
      ChannelSenderCreateRequestExecutor.ChannelSenderCreateRequest,
      ChannelSenderCreateRequestExecutor.ChannelSenderCreateException,
      ChannelSender.Sid
    ] {

  override protected type ApiExceptionWrapper = ChannelSenderCreateException.Api

  override protected type UnspecifiedException = ChannelSenderCreateException.Unspecified
}

object ChannelSenderCreateRequestExecutor {

  final case class ChannelSenderCreateRequest(
      senderId: MessageRecipient,
      configuration: ChannelSender.Configuration,
      webhooks: ChannelSender.Webhooks,
      profile: ChannelSender.Profile,
  )

  sealed trait ChannelSenderCreateException extends RuntimeException
  object ChannelSenderCreateException {

    final case class ChannelNotSupported(channel: String)
        extends RuntimeException(s"Channel are not supported: $channel")
        with ChannelSenderCreateException
    final case class ParseFailure(msg: String)
        extends RuntimeException(msg)
        with ChannelSenderCreateException
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ChannelSenderCreateException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch channel sender resource"
          ),
          cause.orNull
        )
        with ChannelSenderCreateException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }

}
