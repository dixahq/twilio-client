package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Conference

trait ConferenceParticipantReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      ConferenceParticipantReadRequestExecutor.ConferenceParticipantsReadRequest,
      ConferenceParticipantReadRequestExecutor.ConferenceParticipantsReadException,
      Conference.Participant,
      ConferenceParticipantReadRequestExecutor.ConferenceParticipantsReadRequest.BuilderStartState
    ] {

  import ConferenceParticipantReadRequestExecutor._

  override final protected type ApiExceptionWrapper = ConferenceParticipantsReadException.Api

  override final protected type UnspecifiedException =
    ConferenceParticipantsReadException.Unspecified

  override protected def createBuilderStartState()
      : ConferenceParticipantReadRequestExecutor.ConferenceParticipantsReadRequest.BuilderStartState =
    ConferenceParticipantReadRequestExecutor.ConferenceParticipantsReadRequest.Builder.empty
}

object ConferenceParticipantReadRequestExecutor {

  sealed trait ConferenceParticipantsReadRequest {
    def accountSid: TwilioAccount.Sid
    def conferenceSid: Conference.Sid
    def muted: Option[Boolean]
    def hold: Option[Boolean]
    def coaching: Option[Boolean]
  }

  private final case class ConferenceParticipantsReadRequestImpl(
      accountSid: TwilioAccount.Sid,
      conferenceSid: Conference.Sid,
      muted: Option[Boolean],
      hold: Option[Boolean],
      coaching: Option[Boolean]
  ) extends ConferenceParticipantsReadRequest

  object ConferenceParticipantsReadRequest {
    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute extends RequestAttribute

    sealed trait RequestConferenceSidAttribute extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute
      with RequestAccountSidAttribute
      with RequestConferenceSidAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[Attributes <: RequestAttribute] private[ConferenceParticipantsReadRequest] (
        accountSid: Option[TwilioAccount.Sid],
        conferenceSid: Option[Conference.Sid],
        muted: Option[Boolean],
        hold: Option[Boolean],
        coaching: Option[Boolean]
    ) {
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(Some(accountSid), conferenceSid, muted, hold, coaching)

      def withConferenceSid(
          conferenceSid: Conference.Sid
      ): Builder[Attributes with RequestConferenceSidAttribute] =
        new Builder(accountSid, Some(conferenceSid), muted, hold, coaching)

      def withMuted(
          muted: Boolean
      ): Builder[Attributes] =
        new Builder(accountSid, conferenceSid, Some(muted), hold, coaching)

      def withHold(
          hold: Boolean
      ): Builder[Attributes] =
        new Builder(accountSid, conferenceSid, muted, Some(hold), coaching)

      def withCoaching(
          coaching: Boolean
      ): Builder[Attributes] =
        new Builder(accountSid, conferenceSid, muted, hold, Some(coaching))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): ConferenceParticipantsReadRequest =
        ConferenceParticipantsReadRequestImpl(
          accountSid.get,
          conferenceSid.get,
          muted,
          hold,
          coaching
        )
    }

    def build(
        fun: BuilderStartState => ConferenceParticipantsReadRequest
    ): ConferenceParticipantsReadRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new BuilderStartState(None, None, None, None, None)
    }
  }

  sealed trait ConferenceParticipantsReadException extends RuntimeException
  object ConferenceParticipantsReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ConferenceParticipantsReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch conferences"
          ),
          cause.orNull
        )
        with ConferenceParticipantsReadException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
