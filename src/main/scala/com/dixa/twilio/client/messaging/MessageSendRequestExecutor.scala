package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.messaging.MessageSendRequestExecutor.MessageSendException
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging._
import com.dixa.twilio.model.messaging.MessageRecipient
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.callback.CallbackUrl.MessageStatusCallback

trait MessageSendRequestExecutor
    extends SingleRequestExecutor[
      MessageSendRequestExecutor.MessageSendRequest,
      MessageSendRequestExecutor.MessageSendException,
      MessageResource,
      MessageSendRequestExecutor.MessageSendRequest.Builder
    ] {

  override protected final type ApiExceptionWrapper = MessageSendException.Api

  override protected final type UnspecifiedException = MessageSendException.Unspecified

  override protected final def createBuilderStartState()
      : MessageSendRequestExecutor.MessageSendRequest.Builder =
    MessageSendRequestExecutor.MessageSendRequest.Builder.empty
}

object MessageSendRequestExecutor {

  final case class MessageSendRequest(
      accountSid: TwilioAccount.Sid,
      from: MessageSender,
      to: MessageRecipient,
      body: MessageBody,
      statusCallback: MessageStatusCallback,
      mediaUrls: Seq[MediaResourceUrl] = Seq.empty
  )
  object MessageSendRequest {
    type BuilderStartState = Builder

    final class Builder private[messaging] (
        accountSid: Option[TwilioAccount.Sid],
        from: Option[MessageSender],
        to: Option[MessageRecipient],
        body: Option[MessageBody],
        statusCallback: Option[MessageStatusCallback],
        mediaUrls: Seq[MediaResourceUrl]
    ) {
      def withAccountSid(accountSid: TwilioAccount.Sid): Builder =
        new Builder(Some(accountSid), from, to, body, statusCallback, mediaUrls)
      def withFrom(from: MessageSender): Builder =
        new Builder(accountSid, Some(from), to, body, statusCallback, mediaUrls)
      def withTo(to: MessageRecipient): Builder =
        new Builder(accountSid, from, Some(to), body, statusCallback, mediaUrls)
      def withBody(body: MessageBody): Builder =
        new Builder(accountSid, from, to, Some(body), statusCallback, mediaUrls)
      def withStatusCallback(statusCallback: MessageStatusCallback): Builder =
        new Builder(accountSid, from, to, body, Some(statusCallback), mediaUrls)
      def withMediaUrls(mediaUrls: Seq[MediaResourceUrl]): Builder =
        new Builder(accountSid, from, to, body, statusCallback, mediaUrls)
      def build(): MessageSendRequest =
        MessageSendRequest(
          accountSid.get,
          from.get,
          to.get,
          body.get,
          statusCallback.get,
          mediaUrls
        )
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(
        None,
        None,
        None,
        None,
        None,
        Seq.empty
      )
    }

    def build(fun: BuilderStartState => MessageSendRequest): MessageSendRequest =
      fun(Builder.empty)
  }

  // Most common Bad Request errors: https://support.twilio.com/hc/en-us/articles/223181868-Troubleshooting-Undelivered-Twilio-SMS-Messages
  sealed trait MessageSendException extends RuntimeException
  object MessageSendException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with MessageSendException
        with ApiExceptionWrapper

    final case class ToNumberNotValid()
        extends IllegalStateException(
          "Invalid 'To' Phone Number. More info: https://www.twilio.com/docs/api/errors/21211"
        )
        with MessageSendException
    final case class FromNumberNotValid()
        extends IllegalStateException(
          "Invalid From Number. More info: https://www.twilio.com/docs/api/errors/21212"
        )
        with MessageSendException
    final case class NotMessageCapableNumber()
        extends IllegalStateException(
          "Attempt to use a 'From' number which is not capable of sending SMS messages. More info: https://www.twilio.com/docs/api/errors/21606"
        )
        with MessageSendException
    final case class MessageBodyCharLimitExceeded()
        extends IllegalStateException(
          "Concatenated message body exceeds the maximum 1600 character limit. More info: https://www.twilio.com/docs/api/errors/21617"
        )
        with MessageSendException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to send an sms"
          ),
          cause.orNull
        )
        with MessageSendException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }
}
