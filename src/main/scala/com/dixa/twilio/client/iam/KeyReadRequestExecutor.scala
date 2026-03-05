package com.dixa.twilio.client.iam

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.iam.KeyReadRequestExecutor.{KeyReadException, KeyReadRequest}
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.iam.{ApiKey, TwilioAccount}

/** List API keys for a given account.
  *
  * Returns all keys for the account. The secret is not included in list responses — use
  * [[KeyCreateRequestExecutor]] to obtain a new key including its secret.
  *
  * @see
  *   https://www.twilio.com/docs/iam/api-keys/key-resource-v1
  */
trait KeyReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      KeyReadRequest,
      KeyReadException,
      ApiKey,
      KeyReadRequest.BuilderStartState
    ] {

  override protected final type ApiExceptionWrapper  = KeyReadException.Api
  override protected final type UnspecifiedException = KeyReadException.Unspecified

  override protected def createBuilderStartState(): KeyReadRequest.BuilderStartState =
    KeyReadRequest.Builder.empty
}

object KeyReadRequestExecutor {

  sealed trait KeyReadRequest {
    def accountSid: TwilioAccount.Sid
  }

  private final case class KeyReadRequestImpl(
      accountSid: TwilioAccount.Sid
  ) extends KeyReadRequest

  object KeyReadRequest {

    object PhantomTypes {
      sealed trait RequestAttribute
      sealed trait RequestAccountSidAttribute extends RequestAttribute
    }

    type RequestRequiredAttributes =
      PhantomTypes.RequestAttribute with PhantomTypes.RequestAccountSidAttribute

    type BuilderStartState = Builder[PhantomTypes.RequestAttribute]

    final class Builder[Attributes <: PhantomTypes.RequestAttribute] private[KeyReadRequest] (
        accountSid: Option[TwilioAccount.Sid]
    ) {
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with PhantomTypes.RequestAccountSidAttribute] =
        new Builder(Some(accountSid))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): KeyReadRequest =
        KeyReadRequestImpl(accountSid.get)
    }

    def build(fun: BuilderStartState => KeyReadRequest): KeyReadRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new BuilderStartState(None)
    }
  }

  sealed trait KeyReadException extends RuntimeException

  object KeyReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with KeyReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse("Unspecified error happened trying to list API keys"),
          cause.orNull
        )
        with KeyReadException

    object Unspecified {
      def apply(msg: String): Unspecified      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable): Unspecified =
        new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
