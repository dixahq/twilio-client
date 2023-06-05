package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.twiml.Response
import com.dixa.twilio.model.voice.Call
import com.dixa.twilio.model.voice.Call.TimeLimit

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
    def sid: Call.Sid
    def url: Option[CallbackUrl]
    def method: Option[HttpMethod]
    def status: Option[Call.StatusUpdate]
    def fallbackUrl: Option[CallbackUrl]
    def fallbackMethod: Option[HttpMethod]
    def statusCallback: Option[CallbackUrl]
    def statusCallbackMethod: Option[HttpMethod]
    def twiml: Option[Response.Verified]
    def timeLimit: Option[Call.TimeLimit]
  }

  private final case class CallUpdateRequestImpl(
      accountSid: TwilioAccount.Sid,
      sid: Call.Sid,
      url: Option[CallbackUrl],
      method: Option[HttpMethod],
      status: Option[Call.StatusUpdate],
      fallbackUrl: Option[CallbackUrl],
      fallbackMethod: Option[HttpMethod],
      statusCallback: Option[CallbackUrl],
      statusCallbackMethod: Option[HttpMethod],
      twiml: Option[Response.Verified],
      timeLimit: Option[Call.TimeLimit]
  ) extends CallUpdateRequest

  object CallUpdateRequest {

    /** Phantom type Used to require account sid to be supplied before build can be called */
    sealed trait AccountSidAttributeSet
    sealed trait AccountSidAttributeSetTrue  extends AccountSidAttributeSet
    sealed trait AccountSidAttributeSetFalse extends AccountSidAttributeSet

    /** Phantom type Used to require call sid to be supplied before build can be called */
    sealed trait CallSidAttributeSet
    sealed trait CallSidAttributeSetTrue  extends CallSidAttributeSet
    sealed trait CallSidAttributeSetFalse extends CallSidAttributeSet

    /** Used to require tha not both of Twiml and Url is set, as those two are mutually exclusive.
      */
    sealed trait HasTwimlOrUrlSet
    sealed trait HasTwimlOrUrlSetTrue  extends HasTwimlOrUrlSet
    sealed trait HasTwimlOrUrlSetFalse extends HasTwimlOrUrlSet

    /** Used to require that the request has as minimum one update attribute set.
      *
      * By update is meant an attribute that will actually change something to the call. Without
      * that an update request doesn't really make sense to perform.
      */
    sealed trait HasAtLeastOneUpdateAttributeSet
    sealed trait HasAtLeastOneUpdateAttributeSetTrue  extends HasAtLeastOneUpdateAttributeSet
    sealed trait HasAtLeastOneUpdateAttributeSetFalse extends HasAtLeastOneUpdateAttributeSet

    sealed trait HasUrlForMethodSet
    sealed trait HasUrlForMethodSetTrue  extends HasUrlForMethodSet
    sealed trait HasUrlForMethodSetFalse extends HasUrlForMethodSet

    sealed trait HasFallbackUrlForMethodSet
    sealed trait HasFallbackUrlForMethodSetTrue  extends HasFallbackUrlForMethodSet
    sealed trait HasFallbackUrlForMethodSetFalse extends HasFallbackUrlForMethodSet

    sealed trait HasStatusCallbackUrlForMethodSet
    sealed trait HasStatusCallbackUrlForMethodTrue  extends HasStatusCallbackUrlForMethodSet
    sealed trait HasStatusCallbackUrlForMethodFalse extends HasStatusCallbackUrlForMethodSet

    type BuilderStartState =
      Builder[
        AccountSidAttributeSetFalse,
        CallSidAttributeSetFalse,
        HasAtLeastOneUpdateAttributeSetFalse,
        HasTwimlOrUrlSetFalse,
        HasUrlForMethodSetFalse,
        HasFallbackUrlForMethodSetFalse,
        HasStatusCallbackUrlForMethodFalse
      ]

    final class Builder[
        AccountSidSet <: AccountSidAttributeSet,
        CallSidSet <: CallSidAttributeSet,
        OneUpdateAttribute <: HasAtLeastOneUpdateAttributeSet,
        TwimlOrUrl <: HasTwimlOrUrlSet,
        UrlAndMethod <: HasUrlForMethodSet,
        FallbackUrlAndMethod <: HasFallbackUrlForMethodSet,
        StatusCallbackUrlForMethod <: HasStatusCallbackUrlForMethodSet
    ] private[CallUpdateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        sid: Option[Call.Sid],
        url: Option[CallbackUrl],
        method: Option[HttpMethod],
        status: Option[Call.StatusUpdate],
        fallbackUrl: Option[CallbackUrl],
        fallbackMethod: Option[HttpMethod],
        statusCallback: Option[CallbackUrl],
        statusCallbackMethod: Option[HttpMethod],
        twiml: Option[Response.Verified],
        timeLimit: Option[Call.TimeLimit]
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[
        AccountSidAttributeSetTrue,
        CallSidSet,
        OneUpdateAttribute,
        TwimlOrUrl,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlForMethod
      ] = {
        new Builder(
          Some(accountSid),
          sid,
          url,
          method,
          status,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackMethod,
          twiml,
          timeLimit
        )
      }

      def withCallSid(
          sid: Call.Sid
      ): Builder[
        AccountSidSet,
        CallSidAttributeSetTrue,
        OneUpdateAttribute,
        TwimlOrUrl,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlForMethod
      ] =
        new Builder(
          accountSid,
          Some(sid),
          url,
          method,
          status,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackMethod,
          twiml,
          timeLimit
        )

      def withUrl(url: CallbackUrl)(
          implicit ev: TwimlOrUrl =:= HasTwimlOrUrlSetFalse,
      ): Builder[
        AccountSidSet,
        CallSidSet,
        HasAtLeastOneUpdateAttributeSetTrue,
        HasTwimlOrUrlSetTrue,
        HasUrlForMethodSetTrue,
        FallbackUrlAndMethod,
        StatusCallbackUrlForMethod
      ] =
        new Builder(
          accountSid,
          sid,
          Some(url),
          method,
          status,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackMethod,
          twiml,
          timeLimit
        )

      def withMethod(method: HttpMethod)(
          implicit ev: UrlAndMethod =:= HasUrlForMethodSetTrue,
      ): Builder[
        AccountSidSet,
        CallSidSet,
        OneUpdateAttribute,
        TwimlOrUrl,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlForMethod
      ] =
        new Builder(
          accountSid,
          sid,
          url,
          Some(method),
          status,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackMethod,
          twiml,
          timeLimit
        )

      def withStatus(status: Call.StatusUpdate): Builder[
        AccountSidSet,
        CallSidSet,
        HasAtLeastOneUpdateAttributeSetTrue,
        TwimlOrUrl,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlForMethod
      ] = {
        new Builder(
          accountSid,
          sid,
          url,
          method,
          Some(status),
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackMethod,
          twiml,
          timeLimit
        )
      }

      def withFallbackUrl(fallbackUrl: CallbackUrl)(
          implicit ev: TwimlOrUrl =:= HasTwimlOrUrlSetFalse,
      ): Builder[
        AccountSidSet,
        CallSidSet,
        HasAtLeastOneUpdateAttributeSetTrue,
        HasTwimlOrUrlSetTrue,
        UrlAndMethod,
        HasFallbackUrlForMethodSetTrue,
        StatusCallbackUrlForMethod
      ] =
        new Builder(
          accountSid,
          sid,
          url,
          method,
          status,
          Some(fallbackUrl),
          fallbackMethod,
          statusCallback,
          statusCallbackMethod,
          twiml,
          timeLimit
        )

      def withFallbackMethod(fallbackMethod: HttpMethod)(
          implicit ev: FallbackUrlAndMethod =:= HasFallbackUrlForMethodSetTrue,
      ): Builder[
        AccountSidSet,
        CallSidSet,
        OneUpdateAttribute,
        TwimlOrUrl,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlForMethod
      ] =
        new Builder(
          accountSid,
          sid,
          url,
          method,
          status,
          fallbackUrl,
          Some(fallbackMethod),
          statusCallback,
          statusCallbackMethod,
          twiml,
          timeLimit
        )

      def withStatusCallBack(statusCallback: CallbackUrl): Builder[
        AccountSidSet,
        CallSidSet,
        HasAtLeastOneUpdateAttributeSetTrue,
        TwimlOrUrl,
        UrlAndMethod,
        FallbackUrlAndMethod,
        HasStatusCallbackUrlForMethodTrue
      ] =
        new Builder(
          accountSid,
          sid,
          url,
          method,
          status,
          fallbackUrl,
          fallbackMethod,
          Some(statusCallback),
          statusCallbackMethod,
          twiml,
          timeLimit
        )

      def withStatusCallBackMethod(statusCallbackMethod: HttpMethod)(
          implicit ev: StatusCallbackUrlForMethod =:= HasStatusCallbackUrlForMethodTrue,
      ): Builder[
        AccountSidSet,
        CallSidSet,
        OneUpdateAttribute,
        TwimlOrUrl,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlForMethod
      ] =
        new Builder(
          accountSid,
          sid,
          url,
          method,
          status,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          Some(statusCallbackMethod),
          twiml,
          timeLimit
        )

      def withTwiml(twiml: Response.Verified)(
          implicit ev: TwimlOrUrl =:= HasTwimlOrUrlSetFalse
      ): Builder[
        AccountSidSet,
        CallSidSet,
        HasAtLeastOneUpdateAttributeSetTrue,
        HasTwimlOrUrlSetTrue,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlForMethod
      ] =
        new Builder(
          accountSid,
          sid,
          url,
          method,
          status,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackMethod,
          Some(twiml),
          timeLimit
        )

      def withTimeLimit(timeLimit: TimeLimit): Builder[
        AccountSidSet,
        CallSidSet,
        HasAtLeastOneUpdateAttributeSetTrue,
        TwimlOrUrl,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlForMethod
      ] =
        new Builder(
          accountSid,
          sid,
          url,
          method,
          status,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackMethod,
          twiml,
          Some(timeLimit)
        )

      def build()(
          implicit ev: AccountSidSet =:= AccountSidAttributeSetTrue,
          ev2: CallSidSet =:= CallSidAttributeSetTrue,
          ev3: OneUpdateAttribute =:= HasAtLeastOneUpdateAttributeSetTrue
      ): CallUpdateRequest =
        CallUpdateRequestImpl(
          accountSid.get,
          sid.get,
          url,
          method,
          status,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackMethod,
          twiml,
          timeLimit
        )
    }

    def build(fun: BuilderStartState => CallUpdateRequest): CallUpdateRequest =
      fun(new BuilderStartState(None, None, None, None, None, None, None, None, None, None, None))

  }

  sealed trait CallUpdateException extends RuntimeException
  object CallUpdateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with CallUpdateException
        with ApiExceptionWrapper

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
