package com.dixa.twilio.client.voice

import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Conference

trait ConferenceUpdateRequestExecutor
    extends SingleRequestExecutor[
      ConferenceUpdateRequestExecutor.ConferenceUpdateRequest,
      ConferenceUpdateRequestExecutor.ConferenceUpdateException,
      Conference
    ] {

  import ConferenceUpdateRequestExecutor._

  override final protected type ApiExceptionWrapper = ConferenceUpdateException.Api

  override final protected type UnspecifiedException = ConferenceUpdateException.Unspecified
}

object ConferenceUpdateRequestExecutor {

  sealed trait ConferenceUpdateRequest {
    def accountSid: TwilioAccount.Sid
    def conferenceSid: Conference.Sid
    def status: Option[Conference.Status]
    def announceUrl: Option[CallbackUrl]
    def announceMethod: Option[HttpMethod]
  }

  private final case class ConferenceUpdateRequestImpl(
      accountSid: TwilioAccount.Sid,
      conferenceSid: Conference.Sid,
      status: Option[Conference.Status],
      announceUrl: Option[CallbackUrl],
      announceMethod: Option[HttpMethod]
  ) extends ConferenceUpdateRequest

  object ConferenceUpdateRequest {
    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute    extends RequestAttribute
    sealed trait RequestConferenceSidAttribute extends RequestAttribute

    sealed trait HasUrlForMethodSet

    sealed trait HasUrlForMethodSetTrue extends HasUrlForMethodSet

    sealed trait HasUrlForMethodSetFalse extends HasUrlForMethodSet

    type RequestRequiredAttributes = RequestAttribute
      with RequestAccountSidAttribute
      with RequestConferenceSidAttribute

    type BuilderStartState = Builder[RequestAttribute, HasUrlForMethodSetFalse]

    final class Builder[
        Attributes <: RequestAttribute,
        UrlForMethod <: HasUrlForMethodSet
    ] private[ConferenceUpdateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        conferenceSid: Option[Conference.Sid],
        status: Option[Conference.Status],
        announceUrl: Option[CallbackUrl],
        announceMethod: Option[HttpMethod]
    ) {
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute, UrlForMethod] =
        new Builder(Some(accountSid), conferenceSid, status, announceUrl, announceMethod)

      def withConferenceSid(
          conferenceSid: Conference.Sid
      ): Builder[Attributes with RequestConferenceSidAttribute, UrlForMethod] =
        new Builder(accountSid, Some(conferenceSid), status, announceUrl, announceMethod)

      def withStatus(status: Conference.Status): Builder[Attributes, UrlForMethod] =
        new Builder(accountSid, conferenceSid, Some(status), announceUrl, announceMethod)

      def withAnnounceUrl(
          announceUrl: CallbackUrl
      ): Builder[Attributes, HasUrlForMethodSetTrue] =
        new Builder(accountSid, conferenceSid, status, Some(announceUrl), announceMethod)

      def withAnnounceMethod(announceMethod: HttpMethod)(
          implicit ev: Attributes =:= HasUrlForMethodSetTrue
      ): Builder[Attributes, UrlForMethod] =
        new Builder(accountSid, conferenceSid, status, announceUrl, Some(announceMethod))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): ConferenceUpdateRequest =
        ConferenceUpdateRequestImpl(
          accountSid.get,
          conferenceSid.get,
          status,
          announceUrl,
          announceMethod
        )
    }

    def builder(fun: BuilderStartState => ConferenceUpdateRequest): ConferenceUpdateRequest =
      fun(new BuilderStartState(None, None, None, None, None))
  }

  sealed trait ConferenceUpdateException extends RuntimeException
  object ConferenceUpdateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ConferenceUpdateException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch conferences"
          ),
          cause.orNull
        )
        with ConferenceUpdateException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
