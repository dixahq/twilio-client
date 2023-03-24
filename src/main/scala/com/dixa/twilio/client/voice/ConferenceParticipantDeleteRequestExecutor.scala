package com.dixa.twilio.client.voice

import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.Funit
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{Call, Conference}

import scala.annotation.nowarn

trait ConferenceParticipantDeleteRequestExecutor
    extends SingleRequestExecutor[
      ConferenceParticipantDeleteRequestExecutor.ConferenceParticipantDeleteRequest,
      ConferenceParticipantDeleteRequestExecutor.ConferenceParticipantDeleteException,
      Funit
    ] {

  import ConferenceParticipantDeleteRequestExecutor._

  override final protected type ApiExceptionWrapper = ConferenceParticipantDeleteException.Api

  override final protected type UnspecifiedException =
    ConferenceParticipantDeleteException.Unspecified
}

object ConferenceParticipantDeleteRequestExecutor {

  sealed trait ConferenceParticipantDeleteRequest {
    def accountSid: TwilioAccount.Sid
    def conferenceSid: Conference.Sid
    def callSid: Call.Sid
  }

  private final case class ConferenceParticipantDeleteRequestImpl(
      accountSid: TwilioAccount.Sid,
      conferenceSid: Conference.Sid,
      callSid: Call.Sid,
  ) extends ConferenceParticipantDeleteRequest

  object ConferenceParticipantDeleteRequest {

    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute    extends RequestAttribute
    sealed trait RequestConferenceSidAttribute extends RequestAttribute
    sealed trait RequestCallSidAttribute       extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute
      with RequestAccountSidAttribute
      with RequestConferenceSidAttribute
      with RequestCallSidAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[
        Attributes <: RequestAttribute,
    ] private[ConferenceParticipantDeleteRequest] (
        accountSid: Option[TwilioAccount.Sid],
        conferenceSid: Option[Conference.Sid],
        callSid: Option[Call.Sid],
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[
        Attributes with RequestAccountSidAttribute
      ] =
        new Builder(Some(accountSid), conferenceSid, callSid)

      def withConferenceSid(
          conferenceSid: Conference.Sid
      ): Builder[
        Attributes with RequestConferenceSidAttribute
      ] =
        new Builder(accountSid, Some(conferenceSid), callSid)

      def withCallSid(
          callSid: Call.Sid
      ): Builder[
        Attributes with RequestCallSidAttribute
      ] =
        new Builder(accountSid, conferenceSid, Some(callSid))

      @nowarn
      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): ConferenceParticipantDeleteRequest =
        ConferenceParticipantDeleteRequestImpl(accountSid.get, conferenceSid.get, callSid.get)
    }

    def build(
        fun: BuilderStartState => ConferenceParticipantDeleteRequest
    ): ConferenceParticipantDeleteRequest =
      fun(new BuilderStartState(None, None, None))

  }

  sealed trait ConferenceParticipantDeleteException extends RuntimeException
  object ConferenceParticipantDeleteException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ConferenceParticipantDeleteException
    final case class CallNotFound(conferenceSid: Conference.Sid, callSid: Call.Sid)
        extends RuntimeException(
          s"Call with sid $callSid was not found in conference: $conferenceSid"
        )
        with ConferenceParticipantDeleteException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to delete call"
          ),
          cause.orNull
        )
        with ConferenceParticipantDeleteException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
