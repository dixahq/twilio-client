package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.Iso8601DateTime
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Conference

trait ConferenceReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      ConferenceReadRequestExecutor.ConferenceReadRequest,
      ConferenceReadRequestExecutor.ConferenceReadException,
      Conference,
      ConferenceReadRequestExecutor.ConferenceReadRequest.BuilderStartState
    ] {

  import ConferenceReadRequestExecutor._

  override final protected type ApiExceptionWrapper = ConferenceReadException.Api

  override final protected type UnspecifiedException = ConferenceReadException.Unspecified

  override protected def createBuilderStartState()
      : ConferenceReadRequestExecutor.ConferenceReadRequest.BuilderStartState =
    ConferenceReadRequestExecutor.ConferenceReadRequest.Builder.empty
}

object ConferenceReadRequestExecutor {

  sealed trait ConferenceReadRequest {
    def accountSid: TwilioAccount.Sid
    def dateCreated: Option[Iso8601DateTime]
    def dateUpdated: Option[Iso8601DateTime]
    def friendlyName: Option[Conference.FriendlyName]
    def status: Option[Conference.Status]
  }

  private final case class ConferenceReadRequestImpl(
      accountSid: TwilioAccount.Sid,
      dateCreated: Option[Iso8601DateTime],
      dateUpdated: Option[Iso8601DateTime],
      friendlyName: Option[Conference.FriendlyName],
      status: Option[Conference.Status]
  ) extends ConferenceReadRequest

  object ConferenceReadRequest {
    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute with RequestAccountSidAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[Attributes <: RequestAttribute] private[ConferenceReadRequest] (
        accountSid: Option[TwilioAccount.Sid],
        dateCreated: Option[Iso8601DateTime],
        dateUpdated: Option[Iso8601DateTime],
        friendlyName: Option[Conference.FriendlyName],
        status: Option[Conference.Status]
    ) {
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(Some(accountSid), dateCreated, dateUpdated, friendlyName, status)

      def withDateCreated(dateCreated: Iso8601DateTime): Builder[Attributes] =
        new Builder(accountSid, Some(dateCreated), dateUpdated, friendlyName, status)

      def withDateUpdated(dateUpdated: Iso8601DateTime): Builder[Attributes] =
        new Builder(accountSid, dateCreated, Some(dateUpdated), friendlyName, status)

      def withFriendlyName(friendlyName: Conference.FriendlyName): Builder[Attributes] =
        new Builder(accountSid, dateCreated, dateUpdated, Some(friendlyName), status)

      def withStatus(status: Conference.Status): Builder[Attributes] =
        new Builder(accountSid, dateCreated, dateUpdated, friendlyName, Some(status))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): ConferenceReadRequest =
        ConferenceReadRequestImpl(accountSid.get, dateCreated, dateUpdated, friendlyName, status)
    }

    def build(fun: BuilderStartState => ConferenceReadRequest): ConferenceReadRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new BuilderStartState(None, None, None, None, None)
    }
  }

  sealed trait ConferenceReadException extends RuntimeException
  object ConferenceReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ConferenceReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch conferences"
          ),
          cause.orNull
        )
        with ConferenceReadException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
