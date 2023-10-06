package com.dixa.twilio.client.general

import akka.Done
import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.usage.UsageTrigger

trait UsageTriggerDeleteRequestExecutor
    extends SingleRequestExecutor[
      UsageTriggerDeleteRequestExecutor.UsageTriggerDeleteRequest,
      UsageTriggerDeleteRequestExecutor.UsageTriggerDeleteException,
      Done
    ] {

  import UsageTriggerDeleteRequestExecutor._

  override final protected type ApiExceptionWrapper = UsageTriggerDeleteException.Api

  override final protected type UnspecifiedException =
    UsageTriggerDeleteException.UnspecifiedError
}

object UsageTriggerDeleteRequestExecutor {

  sealed trait UsageTriggerDeleteRequest {
    def accountSid: TwilioAccount.Sid
    def usageTriggerSid: UsageTrigger.Sid
  }

  private final case class UsageTriggerDeleteRequestImpl(
      accountSid: TwilioAccount.Sid,
      usageTriggerSid: UsageTrigger.Sid
  ) extends UsageTriggerDeleteRequest

  object UsageTriggerDeleteRequest {
    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute      extends RequestAttribute
    sealed trait RequestUsageTriggerSidAttribute extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute
      with RequestAccountSidAttribute
      with RequestUsageTriggerSidAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[Attributes <: RequestAttribute] private[UsageTriggerDeleteRequest] (
        accountSid: Option[TwilioAccount.Sid],
        usageTriggerSid: Option[UsageTrigger.Sid]
    ) {
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(
          Some(accountSid),
          usageTriggerSid,
        )

      def withUsageTriggerSid(
          usageTriggerSid: UsageTrigger.Sid
      ): Builder[Attributes with RequestUsageTriggerSidAttribute] =
        new Builder(
          accountSid,
          Some(usageTriggerSid),
        )
      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): UsageTriggerDeleteRequest =
        UsageTriggerDeleteRequestImpl(
          accountSid.get,
          usageTriggerSid.get
        )
    }

    def builder(fun: BuilderStartState => UsageTriggerDeleteRequest): UsageTriggerDeleteRequest =
      fun(new BuilderStartState(None, None))
  }

  sealed trait UsageTriggerDeleteException extends RuntimeException
  object UsageTriggerDeleteException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with UsageTriggerDeleteException
        with ApiExceptionWrapper

    /** Exceptions for when the account has no usage trigger. */
    final case class UsageTriggerNotFoundOnAccountException(
        accountSid: TwilioAccount.Sid,
        usageTriggerSid: UsageTrigger.Sid
    ) extends RuntimeException(
          s"Account ${accountSid.twilioString} has no usage trigger(${usageTriggerSid.twilioString}) to delete."
        )
        with UsageTriggerDeleteException

    final case class UnspecifiedError(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to delete usage trigger."
          ),
          cause.orNull
        )
        with UsageTriggerDeleteException
  }
}
