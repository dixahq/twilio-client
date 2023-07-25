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
    def dateCreatedAfter: Option[Iso8601DateTime.After]
    def dateCreatedBefore: Option[Iso8601DateTime.Before]
    def includeSoftDeleted: Option[Boolean]
  }

  private final case class RecordingReadRequestImpl(
      accountSid: TwilioAccount.Sid,
      callSid: Option[Call.Sid],
      conferenceSid: Option[Conference.Sid],
      dateCreatedAfter: Option[Iso8601DateTime.After],
      dateCreatedBefore: Option[Iso8601DateTime.Before],
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
        dateCreatedAfter: Option[Iso8601DateTime.After],
        dateCreatedBefore: Option[Iso8601DateTime.Before],
        includingSoftDeleted: Option[Boolean]
    ) {
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(
          Some(accountSid),
          callSid,
          conferenceSid,
          dateCreatedAfter,
          dateCreatedBefore,
          includingSoftDeleted
        )

      def withCallSid(callSid: Call.Sid): Builder[Attributes] =
        new Builder(
          accountSid,
          Some(callSid),
          conferenceSid,
          dateCreatedAfter,
          dateCreatedBefore,
          includingSoftDeleted
        )

      def withConferenceSid(conferenceSid: Conference.Sid): Builder[Attributes] =
        new Builder(
          accountSid,
          callSid,
          Some(conferenceSid),
          dateCreatedAfter,
          dateCreatedBefore,
          includingSoftDeleted
        )

      def withDateCreatedAfter(dateCreatedAfter: Iso8601DateTime.After): Builder[Attributes] =
        new Builder(
          accountSid,
          callSid,
          conferenceSid,
          Some(dateCreatedAfter),
          dateCreatedBefore,
          includingSoftDeleted
        )

      def withDateCreatedBefore(dateCreatedBefore: Iso8601DateTime.Before): Builder[Attributes] =
        new Builder(
          accountSid,
          callSid,
          conferenceSid,
          dateCreatedAfter,
          Some(dateCreatedBefore),
          includingSoftDeleted
        )

      def withIncludingSoftDeleted(includingSoftDeleted: Boolean): Builder[Attributes] =
        new Builder(
          accountSid,
          callSid,
          conferenceSid,
          dateCreatedAfter,
          dateCreatedBefore,
          Some(includingSoftDeleted)
        )

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): RecordingReadRequest =
        RecordingReadRequestImpl(
          accountSid.get,
          callSid,
          conferenceSid,
          dateCreatedAfter,
          dateCreatedBefore,
          includingSoftDeleted
        )
    }

    def builder(fun: BuilderStartState => RecordingReadRequest): RecordingReadRequest =
      fun(new BuilderStartState(None, None, None, None, None, None))
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
            "Unspecified error happened trying to fetch conferences"
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
