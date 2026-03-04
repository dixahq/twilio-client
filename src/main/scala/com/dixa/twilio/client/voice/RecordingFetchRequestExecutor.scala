package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Recording

trait RecordingFetchRequestExecutor
    extends SingleRequestExecutor[
      RecordingFetchRequestExecutor.RecordingFetchRequest,
      RecordingFetchRequestExecutor.RecordingFetchException,
      Recording,
      RecordingFetchRequestExecutor.RecordingFetchRequest.BuilderStartState
    ] {

  import RecordingFetchRequestExecutor._

  override final protected type ApiExceptionWrapper = RecordingFetchException.Api

  override final protected type UnspecifiedException = RecordingFetchException.Unspecified

  override protected def createBuilderStartState()
      : RecordingFetchRequestExecutor.RecordingFetchRequest.BuilderStartState =
    RecordingFetchRequestExecutor.RecordingFetchRequest.Builder.empty
}

object RecordingFetchRequestExecutor {

  sealed trait RecordingFetchRequest {
    def accountSid: TwilioAccount.Sid
    def sid: Recording.Sid
    def includeSoftDeleted: Option[Boolean]
  }

  private final case class RecordingFetchRequestImpl(
      accountSid: TwilioAccount.Sid,
      sid: Recording.Sid,
      includeSoftDeleted: Option[Boolean]
  ) extends RecordingFetchRequest

  object RecordingFetchRequest {

    /** Phantom type used to require account sid to be supplied before build can be called */
    sealed trait AccountSidAttributeSet
    sealed trait AccountSidAttributeSetTrue  extends AccountSidAttributeSet
    sealed trait AccountSidAttributeSetFalse extends AccountSidAttributeSet

    sealed trait SidAttributeSet
    sealed trait SidAttributeSetTrue  extends SidAttributeSet
    sealed trait SidAttributeSetFalse extends SidAttributeSet

    type BuilderStartState =
      Builder[
        AccountSidAttributeSetFalse,
        SidAttributeSetFalse
      ]

    final class Builder[
        AccountSidSet <: AccountSidAttributeSet,
        CallSidSet <: SidAttributeSet
    ] private[RecordingFetchRequest] (
        accountSid: Option[TwilioAccount.Sid],
        sid: Option[Recording.Sid],
        includeSoftDeleted: Option[Boolean]
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[
        AccountSidAttributeSetTrue,
        CallSidSet
      ] = {
        new Builder(
          Some(accountSid),
          sid,
          includeSoftDeleted
        )
      }

      def withSid(
          sid: Recording.Sid
      ): Builder[
        AccountSidSet,
        SidAttributeSetTrue
      ] = {
        new Builder(
          accountSid,
          Some(sid),
          includeSoftDeleted
        )
      }

      def withIncludeSoftDeleted(
          includeSoftDeleted: Boolean
      ): Builder[
        AccountSidSet,
        CallSidSet
      ] = {
        new Builder(
          accountSid,
          sid,
          Some(includeSoftDeleted),
        )
      }

      def build()(
          implicit ev: AccountSidSet =:= AccountSidAttributeSetTrue,
          ev2: CallSidSet =:= SidAttributeSetTrue,
      ): RecordingFetchRequest =
        RecordingFetchRequestImpl(
          accountSid.get,
          sid.get,
          includeSoftDeleted
        )
    }

    def build(fun: BuilderStartState => RecordingFetchRequest): RecordingFetchRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState =
        new BuilderStartState(
          None,
          None,
          None
        )
    }
  }

  sealed trait RecordingFetchException extends RuntimeException

  object RecordingFetchException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with RecordingFetchException
        with ApiExceptionWrapper

    final case class RecordingNotFound(
        accountSid: TwilioAccount.Sid,
        recordingSid: Recording.Sid
    ) extends RuntimeException(s"""Recording $recordingSid not found in account: $accountSid""")
        with RecordingFetchException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse("Unspecified error happened trying to fetch recording"),
          cause.orNull
        )
        with RecordingFetchException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
