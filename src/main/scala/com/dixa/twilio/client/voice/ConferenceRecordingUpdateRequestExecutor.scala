package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{Conference, Recording}

trait ConferenceRecordingUpdateRequestExecutor
    extends SingleRequestExecutor[
      ConferenceRecordingUpdateRequestExecutor.ConferenceRecordingUpdateRequest,
      ConferenceRecordingUpdateRequestExecutor.ConferenceRecordingUpdateException,
      Recording
    ] {

  import ConferenceRecordingUpdateRequestExecutor._

  override final protected type ApiExceptionWrapper = ConferenceRecordingUpdateException.Api

  override final protected type UnspecifiedException =
    ConferenceRecordingUpdateException.Unspecified
}

object ConferenceRecordingUpdateRequestExecutor {

  sealed trait ConferenceRecordingUpdateRequest {
    def accountSid: TwilioAccount.Sid
    def conferenceSid: Conference.Sid
    def sid: Option[Recording.Sid]
    def status: Recording.ConferenceStatusUpdate
    def pauseBehavior: Option[Recording.PauseBehavior]
  }

  private final case class ConferenceRecordingUpdateRequestImpl(
      accountSid: TwilioAccount.Sid,
      conferenceSid: Conference.Sid,
      sid: Option[Recording.Sid],
      status: Recording.ConferenceStatusUpdate,
      pauseBehavior: Option[Recording.PauseBehavior]
  ) extends ConferenceRecordingUpdateRequest {
    require(
      status.twilioString != Recording.Status.Stopped.twilioString,
      "status stopped not supported for conference recordings"
    )
  }

  object ConferenceRecordingUpdateRequest {

    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute    extends RequestAttribute
    sealed trait RequestConferenceSidAttribute extends RequestAttribute
    sealed trait RequestStatusAttribute        extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute
      with RequestAccountSidAttribute
      with RequestConferenceSidAttribute
      with RequestStatusAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[
        Attributes <: RequestAttribute
    ] private[ConferenceRecordingUpdateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        conferenceSid: Option[Conference.Sid],
        sid: Option[Recording.Sid],
        status: Option[Recording.ConferenceStatusUpdate],
        pauseBehavior: Option[Recording.PauseBehavior]
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(Some(accountSid), conferenceSid, sid, status, pauseBehavior)

      def withConferenceSid(
          conferenceSid: Conference.Sid
      ): Builder[Attributes with RequestConferenceSidAttribute] =
        new Builder(accountSid, Some(conferenceSid), sid, status, pauseBehavior)

      def withSid(sid: Recording.Sid): Builder[Attributes] =
        new Builder(accountSid, conferenceSid, Some(sid), status, pauseBehavior)

      def withStatus(
          status: Recording.ConferenceStatusUpdate
      ): Builder[Attributes with RequestStatusAttribute] =
        new Builder(accountSid, conferenceSid, sid, Some(status), pauseBehavior)

      def withPauseBehavior(pauseBehavior: Recording.PauseBehavior): Builder[Attributes] =
        new Builder(accountSid, conferenceSid, sid, status, Some(pauseBehavior))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): ConferenceRecordingUpdateRequest =
        ConferenceRecordingUpdateRequestImpl(
          accountSid.get,
          conferenceSid.get,
          sid,
          status.get,
          pauseBehavior
        )
    }

    def build(
        fun: BuilderStartState => ConferenceRecordingUpdateRequest
    ): ConferenceRecordingUpdateRequest =
      fun(new BuilderStartState(None, None, None, None, None))

  }

  sealed trait ConferenceRecordingUpdateException extends RuntimeException
  object ConferenceRecordingUpdateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ConferenceRecordingUpdateException
        with ApiExceptionWrapper

    final case class RecordingNotFound(
        accountSid: TwilioAccount.Sid,
        sid: Option[Recording.Sid],
        conferenceSid: Conference.Sid
    ) extends RuntimeException(
          s"""Recording ${sid
              .map(s => s"with sid $s ")
              .getOrElse("")}for conference $conferenceSid was not found in account: $accountSid"""
        )
        with ConferenceRecordingUpdateException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to update conference recording"
          ),
          cause.orNull
        )
        with ConferenceRecordingUpdateException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
