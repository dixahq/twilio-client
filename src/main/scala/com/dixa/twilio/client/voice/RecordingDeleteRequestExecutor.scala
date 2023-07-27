package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Recording

trait RecordingDeleteRequestExecutor
    extends SingleRequestExecutor[
      RecordingDeleteRequestExecutor.RecordingDeleteRequest,
      RecordingDeleteRequestExecutor.RecordingDeleteRequestException,
      Unit
    ] {

  import RecordingDeleteRequestExecutor._

  override final protected type ApiExceptionWrapper = RecordingDeleteRequestException.Api

  override final protected type UnspecifiedException = RecordingDeleteRequestException.Unspecified
}

object RecordingDeleteRequestExecutor {

  sealed trait RecordingDeleteRequest {
    def accountSid: TwilioAccount.Sid
    def sid: Recording.Sid
  }

  private final case class RecordingDeleteRequestImpl(
      accountSid: TwilioAccount.Sid,
      sid: Recording.Sid
  ) extends RecordingDeleteRequest

  object RecordingDeleteRequest {

    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute extends RequestAttribute
    sealed trait RequestSidAttribute        extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute
      with RequestAccountSidAttribute
      with RequestSidAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[
        Attributes <: RequestAttribute
    ] private[RecordingDeleteRequest] (
        accountSid: Option[TwilioAccount.Sid],
        sid: Option[Recording.Sid],
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(Some(accountSid), sid)

      def withSid(
          sid: Recording.Sid
      ): Builder[Attributes with RequestSidAttribute] =
        new Builder(accountSid, Some(sid))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): RecordingDeleteRequest =
        RecordingDeleteRequestImpl(accountSid.get, sid.get)
    }

    def build(fun: BuilderStartState => RecordingDeleteRequest): RecordingDeleteRequest =
      fun(new BuilderStartState(None, None))

  }

  sealed trait RecordingDeleteRequestException extends RuntimeException
  object RecordingDeleteRequestException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with RecordingDeleteRequestException
        with ApiExceptionWrapper

    final case class RecordingNotFound(
        accountSid: TwilioAccount.Sid,
        sid: Recording.Sid,
    ) extends RuntimeException(
          s"""Recording with sid $sid was not found in account: $accountSid"""
        )
        with RecordingDeleteRequestException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to delete recording"
          ),
          cause.orNull
        )
        with RecordingDeleteRequestException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
