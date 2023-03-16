package com.dixa.twilio.client.voice

import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Queue

import scala.annotation.nowarn

trait QueueUpdateRequestExecutor
    extends SingleRequestExecutor[
      QueueUpdateRequestExecutor.QueueUpdateRequest,
      QueueUpdateRequestExecutor.QueueUpdateException,
      Queue
    ] {

  import QueueUpdateRequestExecutor._

  override final protected type ApiExceptionWrapper = QueueUpdateException.Api

  override final protected type UnspecifiedException = QueueUpdateException.Unspecified
}

object QueueUpdateRequestExecutor {

  sealed trait QueueUpdateRequest {
    def accountSid: TwilioAccount.Sid
    def sid: Queue.Sid
    def friendlyName: Option[Queue.FriendlyName]
    def maxSize: Option[Queue.MaxSize]
  }

  private final case class QueueUpdateRequestImpl(
      accountSid: TwilioAccount.Sid,
      sid: Queue.Sid,
      friendlyName: Option[Queue.FriendlyName],
      maxSize: Option[Queue.MaxSize]
  ) extends QueueUpdateRequest

  object QueueUpdateRequest {

    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute extends RequestAttribute
    sealed trait RequestSidAttribute        extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute
      with RequestAccountSidAttribute
      with RequestSidAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[
        Attributes <: RequestAttribute
    ] private[QueueUpdateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        sid: Option[Queue.Sid],
        friendlyName: Option[Queue.FriendlyName],
        maxSize: Option[Queue.MaxSize]
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(Some(accountSid), sid, friendlyName, maxSize)

      def withSid(
          sid: Queue.Sid
      ): Builder[Attributes with RequestSidAttribute] =
        new Builder(accountSid, Some(sid), friendlyName, maxSize)

      def withFriendlyName(friendlyName: Queue.FriendlyName): Builder[Attributes] =
        new Builder(accountSid, sid, Some(friendlyName), maxSize)

      /** Set the Max size to update to.
        *
        * Not that only a specific range of values are allowed here. At time of writing this is
        * 1-5000. If you specify something outside of the allowed range, you request will fail at
        * runtime.
        */
      def withMaxSize(maxSize: Queue.MaxSize): Builder[Attributes] =
        new Builder(accountSid, sid, friendlyName, Some(maxSize))

      @nowarn
      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): QueueUpdateRequest =
        QueueUpdateRequestImpl(accountSid.get, sid.get, friendlyName, maxSize)
    }

    def build(fun: BuilderStartState => QueueUpdateRequest): QueueUpdateRequest =
      fun(new BuilderStartState(None, None, None, None))

  }

  sealed trait QueueUpdateException extends RuntimeException
  object QueueUpdateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with QueueUpdateException
    final case class QueueNotFound(accountSid: TwilioAccount.Sid, sid: Queue.Sid)
        extends RuntimeException(s"Queue with sid $sid was not found in account: $accountSid")
        with QueueUpdateException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to update queue"
          ),
          cause.orNull
        )
        with QueueUpdateException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
