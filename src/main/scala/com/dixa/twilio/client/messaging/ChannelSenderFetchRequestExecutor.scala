package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.messaging.ChannelSenderFetchRequestExecutor.ChannelSenderFetchException
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.messaging._

trait ChannelSenderFetchRequestExecutor
    extends SingleRequestExecutor[
      ChannelSenderFetchRequestExecutor.ChannelSenderFetchRequest,
      ChannelSenderFetchRequestExecutor.ChannelSenderFetchException,
      ChannelSender
    ] {

  override protected type ApiExceptionWrapper = ChannelSenderFetchException.Api

  override protected type UnspecifiedException = ChannelSenderFetchException.Unspecified
}

object ChannelSenderFetchRequestExecutor {

  final case class ChannelSenderFetchRequest(
      channelSenderSid: ChannelSender.Sid,
  )

  sealed trait ChannelSenderFetchException extends RuntimeException
  object ChannelSenderFetchException {

    final case class ParseFailure(msg: String)
        extends RuntimeException(msg)
        with ChannelSenderFetchException
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ChannelSenderFetchException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened when trying to fetch channel sender resource"
          ),
          cause.orNull
        )
        with ChannelSenderFetchException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }
}
