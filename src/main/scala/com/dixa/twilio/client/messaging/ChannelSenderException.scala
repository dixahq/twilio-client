package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.ApiException
import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper

sealed trait ChannelSenderException extends RuntimeException

object ChannelSenderException {

  final case class ChannelNotSupported(channel: String)
      extends RuntimeException(s"Channel is not supported: $channel")
      with ChannelSenderException

  final case class ParseFailure(msg: String)
      extends RuntimeException(msg)
      with ChannelSenderException

  final case class Api(cause: ApiException)
      extends RuntimeException(cause)
      with ChannelSenderException
      with ApiExceptionWrapper

  final case class SenderIdAlreadyExists(senderId: String, apiMsg: String, apiLink: String)
      extends RuntimeException(
        s"SenderId already exists: $senderId - ${apiMsg} - ${apiLink}"
      )
      with ChannelSenderException

  final case class CouldNotExtendCreditLine(
      wabaId: Option[String],
      apiMsg: String,
      apiLink: String
  ) extends RuntimeException(
        s"Could not extend credit line for WABA: ${wabaId.getOrElse("WABA ID Not found")} - ${apiMsg} - ${apiLink}"
      )
      with ChannelSenderException

  final case class Unspecified(msg: Option[String], cause: Option[Throwable])
      extends RuntimeException(
        msg.getOrElse(
          "Unspecified error for ChannelSender resource"
        ),
        cause.orNull
      )
      with ChannelSenderException {
    def this(msg: String) = this(Some(msg), None)

    def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
  }

  final case class Unexpected(msg: Option[String], cause: Option[Throwable])
      extends RuntimeException(
        msg.getOrElse(
          "Unexpected error from Senders API"
        ),
        cause.orNull
      )
      with ChannelSenderException {
    def this(msg: String) = this(Some(msg), None)

    def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
  }

  final case class TwilioInternalError(
      errorCode: Option[Long],
      errorMessage: Option[String],
      moreInfo: Option[String],
      rawResponse: String
  ) extends RuntimeException(
        s"Twilio internal error (${errorCode.getOrElse("unknown")}): ${errorMessage.getOrElse("unknown")} - ${moreInfo.getOrElse("")}"
      )
      with ChannelSenderException
}
