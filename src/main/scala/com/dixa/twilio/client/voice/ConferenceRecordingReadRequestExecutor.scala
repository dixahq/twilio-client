package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.Iso8601DateTime
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{Call, Conference, Recording}

trait ConferenceRecordingReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      ConferenceRecordingReadRequestExecutor.ConferenceRecordingReadRequest,
      ConferenceRecordingReadRequestExecutor.ConferenceRecordingReadException,
      Recording,
      ConferenceRecordingReadRequestExecutor.ConferenceRecordingReadRequest.BuilderStartState
    ] {

  import ConferenceRecordingReadRequestExecutor._

  override final protected type ApiExceptionWrapper = ConferenceRecordingReadException.Api

  override final protected type UnspecifiedException = ConferenceRecordingReadException.Unspecified

  override protected def createBuilderStartState()
      : ConferenceRecordingReadRequestExecutor.ConferenceRecordingReadRequest.BuilderStartState =
    ConferenceRecordingReadRequestExecutor.ConferenceRecordingReadRequest.Builder.empty
}

object ConferenceRecordingReadRequestExecutor {

  sealed trait ConferenceRecordingReadRequest {
    def accountSid: TwilioAccount.Sid
    def conferenceSid: Conference.Sid
    def callSid: Option[Call.Sid]
    def dateCreated: Option[Iso8601DateTime]
    def includeSoftDeleted: Option[Boolean]
  }

  private final case class ConferenceRecordingReadRequestImpl(
      accountSid: TwilioAccount.Sid,
      conferenceSid: Conference.Sid,
      callSid: Option[Call.Sid],
      dateCreated: Option[Iso8601DateTime],
      includeSoftDeleted: Option[Boolean]
  ) extends ConferenceRecordingReadRequest

  object ConferenceRecordingReadRequest {
    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute    extends RequestAttribute
    sealed trait RequestConferenceSidAttribute extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute
      with RequestAccountSidAttribute
      with RequestConferenceSidAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[Attributes <: RequestAttribute] private[ConferenceRecordingReadRequest] (
        accountSid: Option[TwilioAccount.Sid],
        conferenceSid: Option[Conference.Sid],
        callSid: Option[Call.Sid],
        dateCreated: Option[Iso8601DateTime],
        includingSoftDeleted: Option[Boolean]
    ) {
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(
          Some(accountSid),
          conferenceSid,
          callSid,
          dateCreated,
          includingSoftDeleted
        )

      def withCallSid(callSid: Call.Sid): Builder[Attributes] =
        new Builder(
          accountSid,
          conferenceSid,
          Some(callSid),
          dateCreated,
          includingSoftDeleted
        )

      def withConferenceSid(
          conferenceSid: Conference.Sid
      ): Builder[Attributes with RequestConferenceSidAttribute] =
        new Builder(
          accountSid,
          Some(conferenceSid),
          callSid,
          dateCreated,
          includingSoftDeleted
        )

      def withDateCreated(dateCreated: Iso8601DateTime): Builder[Attributes] =
        new Builder(
          accountSid,
          conferenceSid,
          callSid,
          Some(dateCreated),
          includingSoftDeleted
        )

      def withIncludingSoftDeleted(includingSoftDeleted: Boolean): Builder[Attributes] =
        new Builder(
          accountSid,
          conferenceSid,
          callSid,
          dateCreated,
          Some(includingSoftDeleted)
        )

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): ConferenceRecordingReadRequest =
        ConferenceRecordingReadRequestImpl(
          accountSid.get,
          conferenceSid.get,
          callSid,
          dateCreated,
          includingSoftDeleted
        )
    }

    def build(
        fun: BuilderStartState => ConferenceRecordingReadRequest
    ): ConferenceRecordingReadRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new BuilderStartState(None, None, None, None, None)
    }
  }

  sealed trait ConferenceRecordingReadException extends RuntimeException
  object ConferenceRecordingReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ConferenceRecordingReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read recordings"
          ),
          cause.orNull
        )
        with ConferenceRecordingReadException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
