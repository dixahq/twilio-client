package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.messaging.ChannelsSendersVerificationRequestExecutor.ChannelSenderVerificationException
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.messaging._

trait ChannelsSendersVerificationRequestExecutor
    extends SingleRequestExecutor[
      ChannelsSendersVerificationRequestExecutor.ChannelSenderVerificationRequest,
      ChannelsSendersVerificationRequestExecutor.ChannelSenderVerificationException,
      Unit
    ] {

  override protected type ApiExceptionWrapper = ChannelSenderVerificationException.Api

  override protected type UnspecifiedException = ChannelSenderVerificationException.Unspecified
}

object ChannelsSendersVerificationRequestExecutor {

  final case class ChannelSenderVerificationRequest(
      senderSid: ChannelSender.Sid,
      verificationCode: ChannelSender.VerificationCodeConfiguration
  )

  sealed trait ChannelSenderVerificationException extends RuntimeException
  object ChannelSenderVerificationException {

    final case class ChannelNotSupported(channel: String)
        extends RuntimeException(s"Channel are not supported: $channel")
        with ChannelSenderVerificationException
    final case class ParseFailure(msg: String)
        extends RuntimeException(msg)
        with ChannelSenderVerificationException
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ChannelSenderVerificationException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch channel sender resource"
          ),
          cause.orNull
        )
        with ChannelSenderVerificationException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }

}
