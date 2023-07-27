package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.Iso8601DateTime
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{Call, Conference, Recording}

trait RecordingReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      RecordingReadRequestExecutor.RecordingReadRequest,
      RecordingReadRequestExecutor.RecordingReadException,
      Recording
    ] {

  import RecordingReadRequestExecutor._

  override final protected type ApiExceptionWrapper = RecordingReadException.Api

  override final protected type UnspecifiedException = RecordingReadException.Unspecified
}

object RecordingReadRequestExecutor {

  sealed trait RecordingReadRequest {
    def accountSid: TwilioAccount.Sid
    def callSid: Option[Call.Sid]
    def conferenceSid: Option[Conference.Sid]
    def dateCreated: Option[Iso8601DateTime]
    def includeSoftDeleted: Option[Boolean]
  }

  private final case class RecordingReadRequestImpl(
      accountSid: TwilioAccount.Sid,
      callSid: Option[Call.Sid],
      conferenceSid: Option[Conference.Sid],
      dateCreated: Option[Iso8601DateTime],
      includeSoftDeleted: Option[Boolean]
  ) extends RecordingReadRequest

  object RecordingReadRequest {
    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute with RequestAccountSidAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[Attributes <: RequestAttribute] private[RecordingReadRequest] (
        accountSid: Option[TwilioAccount.Sid],
        callSid: Option[Call.Sid],
        conferenceSid: Option[Conference.Sid],
        dateCreated: Option[Iso8601DateTime],
        includingSoftDeleted: Option[Boolean]
    ) {
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(
          Some(accountSid),
          callSid,
          conferenceSid,
          dateCreated,
          includingSoftDeleted
        )

      def withCallSid(callSid: Call.Sid): Builder[Attributes] =
        new Builder(
          accountSid,
          Some(callSid),
          conferenceSid,
          dateCreated,
          includingSoftDeleted
        )

      def withConferenceSid(conferenceSid: Conference.Sid): Builder[Attributes] =
        new Builder(
          accountSid,
          callSid,
          Some(conferenceSid),
          dateCreated,
          includingSoftDeleted
        )

      def withDateCreated(dateCreated: Iso8601DateTime): Builder[Attributes] =
        new Builder(
          accountSid,
          callSid,
          conferenceSid,
          Some(dateCreated),
          includingSoftDeleted
        )

      def withIncludingSoftDeleted(includingSoftDeleted: Boolean): Builder[Attributes] =
        new Builder(
          accountSid,
          callSid,
          conferenceSid,
          dateCreated,
          Some(includingSoftDeleted)
        )

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): RecordingReadRequest =
        RecordingReadRequestImpl(
          accountSid.get,
          callSid,
          conferenceSid,
          dateCreated,
          includingSoftDeleted
        )
    }

    def builder(fun: BuilderStartState => RecordingReadRequest): RecordingReadRequest =
      fun(new BuilderStartState(None, None, None, None, None))
  }

  sealed trait RecordingReadException extends RuntimeException
  object RecordingReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with RecordingReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read recordings"
          ),
          cause.orNull
        )
        with RecordingReadException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
