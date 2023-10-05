package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Call

trait CallFetchRequestExecutor
    extends SingleRequestExecutor[
      CallFetchRequestExecutor.CallFetchRequest,
      CallFetchRequestExecutor.CallFetchException,
      Call
    ] {

  import CallFetchRequestExecutor._

  override final protected type ApiExceptionWrapper = CallFetchException.Api

  override final protected type UnspecifiedException = CallFetchException.Unspecified
}

object CallFetchRequestExecutor {

  sealed trait CallFetchRequest {
    def accountSid: TwilioAccount.Sid
    def sid: Call.Sid
  }

  private final case class CallFetchRequestImpl(
      accountSid: TwilioAccount.Sid,
      sid: Call.Sid
  ) extends CallFetchRequest

  object CallFetchRequest {

    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute extends RequestAttribute
    sealed trait RequestSidAttribute        extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute
      with RequestAccountSidAttribute
      with RequestSidAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[
        Attributes <: RequestAttribute
    ] private[CallFetchRequest] (
        accountSid: Option[TwilioAccount.Sid],
        sid: Option[Call.Sid]
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(Some(accountSid), sid)

      def withSid(
          sid: Call.Sid
      ): Builder[Attributes with RequestSidAttribute] =
        new Builder(accountSid, Some(sid))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): CallFetchRequest =
        CallFetchRequestImpl(accountSid.get, sid.get)
    }

    def build(fun: BuilderStartState => CallFetchRequest): CallFetchRequest =
      fun(new BuilderStartState(None, None))

  }

  sealed trait CallFetchException extends RuntimeException
  object CallFetchException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with CallFetchException
        with ApiExceptionWrapper

    final case class CallNotFound(accountSid: TwilioAccount.Sid, sid: Call.Sid)
        extends RuntimeException(s"Call with sid $sid was not found in account: $accountSid")
        with CallFetchException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch call"
          ),
          cause.orNull
        )
        with CallFetchException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
