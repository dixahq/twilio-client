package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{Call, Recording}

trait CallRecordingUpdateRequestExecutor
    extends SingleRequestExecutor[
      CallRecordingUpdateRequestExecutor.CallRecordingUpdateRequest,
      CallRecordingUpdateRequestExecutor.CallRecordingUpdateException,
      Recording,
      CallRecordingUpdateRequestExecutor.CallRecordingUpdateRequest.BuilderStartState
    ] {

  import CallRecordingUpdateRequestExecutor._

  override final protected type ApiExceptionWrapper = CallRecordingUpdateException.Api

  override final protected type UnspecifiedException = CallRecordingUpdateException.Unspecified

  override protected def createBuilderStartState()
      : CallRecordingUpdateRequestExecutor.CallRecordingUpdateRequest.BuilderStartState =
    CallRecordingUpdateRequestExecutor.CallRecordingUpdateRequest.Builder.empty
}

object CallRecordingUpdateRequestExecutor {

  sealed trait CallRecordingUpdateRequest {
    def accountSid: TwilioAccount.Sid
    def callSid: Call.Sid
    def sid: Option[Recording.Sid]
    def status: Recording.StatusUpdate
    def pauseBehavior: Option[Recording.PauseBehavior]
  }

  private final case class CallRecordingUpdateRequestImpl(
      accountSid: TwilioAccount.Sid,
      callSid: Call.Sid,
      sid: Option[Recording.Sid],
      status: Recording.StatusUpdate,
      pauseBehavior: Option[Recording.PauseBehavior]
  ) extends CallRecordingUpdateRequest

  object CallRecordingUpdateRequest {

    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute extends RequestAttribute
    sealed trait RequestCallSidAttribute    extends RequestAttribute
    sealed trait RequestStatusAttribute     extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute
      with RequestAccountSidAttribute
      with RequestCallSidAttribute
      with RequestStatusAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[
        Attributes <: RequestAttribute
    ] private[CallRecordingUpdateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        callSid: Option[Call.Sid],
        sid: Option[Recording.Sid],
        status: Option[Recording.StatusUpdate],
        pauseBehavior: Option[Recording.PauseBehavior]
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(Some(accountSid), callSid, sid, status, pauseBehavior)

      def withCallSid(
          callSid: Call.Sid
      ): Builder[Attributes with RequestCallSidAttribute] =
        new Builder(accountSid, Some(callSid), sid, status, pauseBehavior)

      def withSid(
          sid: Recording.Sid
      ): Builder[Attributes] =
        new Builder(accountSid, callSid, Some(sid), status, pauseBehavior)

      def withStatus(
          status: Recording.StatusUpdate
      ): Builder[Attributes with RequestStatusAttribute] =
        new Builder(accountSid, callSid, sid, Some(status), pauseBehavior)

      def withPauseBehavior(pauseBehavior: Recording.PauseBehavior): Builder[Attributes] =
        new Builder(accountSid, callSid, sid, status, Some(pauseBehavior))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): CallRecordingUpdateRequest =
        CallRecordingUpdateRequestImpl(
          accountSid.get,
          callSid.get,
          sid,
          status.get,
          pauseBehavior
        )
    }

    def build(fun: BuilderStartState => CallRecordingUpdateRequest): CallRecordingUpdateRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new BuilderStartState(None, None, None, None, None)
    }
  }

  sealed trait CallRecordingUpdateException extends RuntimeException
  object CallRecordingUpdateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with CallRecordingUpdateException
        with ApiExceptionWrapper

    final case class RecordingNotFound(
        accountSid: TwilioAccount.Sid,
        sid: Option[Recording.Sid] = None,
        callSid: Call.Sid
    ) extends RuntimeException(
          s"""Recording ${sid
              .map(s => s"with sid $s ")
              .getOrElse("")}for call $callSid was not found in account: $accountSid"""
        )
        with CallRecordingUpdateException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to update call recording"
          ),
          cause.orNull
        )
        with CallRecordingUpdateException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
