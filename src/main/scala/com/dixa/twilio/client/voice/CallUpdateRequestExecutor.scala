package com.dixa.twilio.client.voice

import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.twiml.Response
import com.dixa.twilio.model.voice.Call

trait CallUpdateRequestExecutor
    extends SingleRequestExecutor[
      CallUpdateRequestExecutor.CallUpdateRequest,
      CallUpdateRequestExecutor.CallUpdateException,
      Call
    ] {

  import CallUpdateRequestExecutor._

  override final protected type ApiExceptionWrapper = CallUpdateException.Api

  override final protected type UnspecifiedException = CallUpdateException.Unspecified
}

object CallUpdateRequestExecutor {

  sealed trait CallUpdateRequest {
    def accountSid: TwilioAccount.Sid
    def callSid: Call.Sid
    def twiml: Option[Response.Verified]
    def url: Option[CallbackUrl]
    // API support a lot more fields, that could be added when needed.
  }

  private final case class CallUpdateRequestImpl(
      accountSid: TwilioAccount.Sid,
      callSid: Call.Sid,
      twiml: Option[Response.Verified],
      url: Option[CallbackUrl]
      // API support a lot more fields, that could be added when needed.
  ) extends CallUpdateRequest

  object CallUpdateRequest {

    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute extends RequestAttribute
    sealed trait RequestCallSidAttribute    extends RequestAttribute

    sealed trait HasTwimlOrUrlSet      extends RequestAttribute
    sealed trait HasTwimlOrUrlSetTrue  extends HasTwimlOrUrlSet
    sealed trait HasTwimlOrUrlSetFalse extends HasTwimlOrUrlSet

    type RequestRequiredAttributes = RequestAttribute
      with RequestAccountSidAttribute
      with RequestCallSidAttribute
      with HasTwimlOrUrlSetTrue

    type BuilderStartState = Builder[RequestAttribute, HasTwimlOrUrlSetFalse]

    final class Builder[
        Attributes <: RequestAttribute,
        TwimlOrUrl <: HasTwimlOrUrlSet
    ] private[CallUpdateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        callSid: Option[Call.Sid],
        twiml: Option[Response.Verified],
        url: Option[CallbackUrl]
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute, TwimlOrUrl] =
        new Builder(Some(accountSid), callSid, twiml, url)

      def withCallSid(
          callSid: Call.Sid
      ): Builder[Attributes with RequestCallSidAttribute, TwimlOrUrl] =
        new Builder(accountSid, Some(callSid), twiml, url)

      def withTwiml(twiml: Response.Verified)(
          implicit ev: TwimlOrUrl =:= HasTwimlOrUrlSetFalse
      ): Builder[Attributes with HasTwimlOrUrlSetTrue, HasTwimlOrUrlSetTrue] =
        new Builder(accountSid, callSid, Some(twiml), url)

      def withUrl(url: CallbackUrl)(
          implicit ev: TwimlOrUrl =:= HasTwimlOrUrlSetFalse
      ): Builder[Attributes with HasTwimlOrUrlSetTrue, HasTwimlOrUrlSetTrue] =
        new Builder(accountSid, callSid, twiml, Some(url))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): CallUpdateRequest =
        CallUpdateRequestImpl(accountSid.get, callSid.get, twiml, url)
    }

    def build(fun: BuilderStartState => CallUpdateRequest): CallUpdateRequest =
      fun(new BuilderStartState(None, None, None, None))

  }

  sealed trait CallUpdateException extends RuntimeException
  object CallUpdateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with CallUpdateException
    final case class CallNotFound(accountSid: TwilioAccount.Sid, callSid: Call.Sid)
        extends RuntimeException(s"Call with sid $callSid was not found in account: $accountSid")
        with CallUpdateException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to update call"
          ),
          cause.orNull
        )
        with CallUpdateException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
