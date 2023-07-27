package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{Call, Recording}

trait RecordingCreateRequestExecutor
    extends SingleRequestExecutor[
      RecordingCreateRequestExecutor.RecordingCreateRequest,
      RecordingCreateRequestExecutor.RecordingCreateException,
      Recording
    ] {

  import RecordingCreateRequestExecutor._

  override final protected type ApiExceptionWrapper = RecordingCreateException.Api

  override final protected type UnspecifiedException = RecordingCreateException.Unspecified

}

object RecordingCreateRequestExecutor {

  sealed trait RecordingCreateRequest {
    def accountSid: TwilioAccount.Sid
    def callSid: Call.Sid
    def recordingStatusCallbackEvent: Option[Set[Recording.CallbackStatus]]
    def recordingStatusCallback: Option[CallbackUrl]
    def recordingStatusCallbackMethod: Option[HttpMethod]
    def trim: Option[Recording.Trim]
    def recordingChannels: Option[Recording.RecordingChannels]
    def recordingTrack: Option[Recording.Track]
  }

  private final case class RecordingCreateRequestImpl(
      accountSid: TwilioAccount.Sid,
      callSid: Call.Sid,
      recordingStatusCallbackEvent: Option[Set[Recording.CallbackStatus]],
      recordingStatusCallback: Option[CallbackUrl],
      recordingStatusCallbackMethod: Option[HttpMethod],
      trim: Option[Recording.Trim],
      recordingChannels: Option[Recording.RecordingChannels],
      recordingTrack: Option[Recording.Track],
  ) extends RecordingCreateRequest

  object RecordingCreateRequest {

    /** Phantom type used to require account sid to be supplied before build can be called */
    sealed trait AccountSidAttributeSet
    sealed trait AccountSidAttributeSetTrue  extends AccountSidAttributeSet
    sealed trait AccountSidAttributeSetFalse extends AccountSidAttributeSet

    sealed trait CallSidAttributeSet
    sealed trait CallSidAttributeSetTrue  extends CallSidAttributeSet
    sealed trait CallSidAttributeSetFalse extends CallSidAttributeSet

    sealed trait HasUrlForCallbackSet
    sealed trait HasUrlForCallbackSetTrue  extends HasUrlForCallbackSet
    sealed trait HasUrlForCallbackSetFalse extends HasUrlForCallbackSet

    type BuilderStartState =
      Builder[
        AccountSidAttributeSetFalse,
        CallSidAttributeSetFalse,
        HasUrlForCallbackSetFalse
      ]

    final class Builder[
        AccountSidSet <: AccountSidAttributeSet,
        CallSidSet <: CallSidAttributeSet,
        UrlForCallbackSet <: HasUrlForCallbackSet
    ] private[RecordingCreateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        callSid: Option[Call.Sid],
        recordingStatusCallbackEvents: Option[Set[Recording.CallbackStatus]],
        recordingStatusCallback: Option[CallbackUrl],
        recordingStatusCallbackMethod: Option[HttpMethod],
        trim: Option[Recording.Trim],
        recordingChannels: Option[Recording.RecordingChannels],
        recordingTrack: Option[Recording.Track],
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[
        AccountSidAttributeSetTrue,
        CallSidSet,
        UrlForCallbackSet
      ] = {
        new Builder(
          Some(accountSid),
          callSid,
          recordingStatusCallbackEvents,
          recordingStatusCallback,
          recordingStatusCallbackMethod,
          trim,
          recordingChannels,
          recordingTrack
        )
      }

      def withCallSid(
          callSid: Call.Sid
      ): Builder[
        AccountSidSet,
        CallSidAttributeSetTrue,
        UrlForCallbackSet
      ] = {
        new Builder(
          accountSid,
          Some(callSid),
          recordingStatusCallbackEvents,
          recordingStatusCallback,
          recordingStatusCallbackMethod,
          trim,
          recordingChannels,
          recordingTrack
        )
      }

      def withRecordingStatusCallbackEvent(
          recordingStatusCallbackEvent: Set[Recording.CallbackStatus]
      )(
          implicit ev: UrlForCallbackSet =:= HasUrlForCallbackSetTrue
      ): Builder[
        AccountSidSet,
        CallSidSet,
        UrlForCallbackSet
      ] = {
        new Builder(
          accountSid,
          callSid,
          Some(recordingStatusCallbackEvent),
          recordingStatusCallback,
          recordingStatusCallbackMethod,
          trim,
          recordingChannels,
          recordingTrack
        )
      }

      def withRecordingStatusCallback(
          recordingStatusCallback: CallbackUrl
      ): Builder[
        AccountSidSet,
        CallSidSet,
        HasUrlForCallbackSetTrue
      ] = {
        new Builder(
          accountSid,
          callSid,
          recordingStatusCallbackEvents,
          Some(recordingStatusCallback),
          recordingStatusCallbackMethod,
          trim,
          recordingChannels,
          recordingTrack
        )
      }

      def withRecordingStatusCallbackMethod(
          recordingStatusCallbackMethod: HttpMethod
      )(
          implicit ev: UrlForCallbackSet =:= HasUrlForCallbackSetTrue
      ): Builder[
        AccountSidSet,
        CallSidSet,
        UrlForCallbackSet
      ] = {
        new Builder(
          accountSid,
          callSid,
          recordingStatusCallbackEvents,
          recordingStatusCallback,
          Some(recordingStatusCallbackMethod),
          trim,
          recordingChannels,
          recordingTrack
        )
      }

      def withTrim(
          trim: Recording.Trim
      ): Builder[
        AccountSidSet,
        CallSidSet,
        UrlForCallbackSet
      ] = {
        new Builder(
          accountSid,
          callSid,
          recordingStatusCallbackEvents,
          recordingStatusCallback,
          recordingStatusCallbackMethod,
          Some(trim),
          recordingChannels,
          recordingTrack
        )
      }

      def withRecordingChannels(
          recordingChannels: Recording.RecordingChannels
      ): Builder[
        AccountSidSet,
        CallSidSet,
        UrlForCallbackSet
      ] = {
        new Builder(
          accountSid,
          callSid,
          recordingStatusCallbackEvents,
          recordingStatusCallback,
          recordingStatusCallbackMethod,
          trim,
          Some(recordingChannels),
          recordingTrack
        )
      }

      def withRecordingTrack(
          recordingTrack: Recording.Track
      ): Builder[
        AccountSidSet,
        CallSidSet,
        UrlForCallbackSet,
      ] = {
        new Builder(
          accountSid,
          callSid,
          recordingStatusCallbackEvents,
          recordingStatusCallback,
          recordingStatusCallbackMethod,
          trim,
          recordingChannels,
          Some(recordingTrack)
        )
      }

      def build()(
          implicit ev: AccountSidSet =:= AccountSidAttributeSetTrue,
          ev2: CallSidSet =:= CallSidAttributeSetTrue,
      ): RecordingCreateRequest =
        RecordingCreateRequestImpl(
          accountSid.get,
          callSid.get,
          recordingStatusCallbackEvents,
          recordingStatusCallback,
          recordingStatusCallbackMethod,
          trim,
          recordingChannels,
          recordingTrack,
        )
    }

    def build(fun: BuilderStartState => RecordingCreateRequest): RecordingCreateRequest =
      fun(
        new BuilderStartState(
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None
        )
      )
  }

  sealed trait RecordingCreateException extends RuntimeException

  object RecordingCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with RecordingCreateException
        with ApiExceptionWrapper

    final case class CallNotFound(
        accountSid: TwilioAccount.Sid,
        callSid: Call.Sid
    ) extends RuntimeException(s"""Call $callSid not found in account: $accountSid""")
        with RecordingCreateException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse("Unspecified error happened trying to create recording"),
          cause.orNull
        )
        with RecordingCreateException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
