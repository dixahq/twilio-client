package com.dixa.twilio.client.stunTurn

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.stunTurn.Token

/** Create a Network Traversal Service Token.
  *
  * @see
  *   https://www.twilio.com/docs/stun-turn/api
  */
trait TokenCreateRequestExecutor
    extends SingleRequestExecutor[
      TokenCreateRequestExecutor.TokenCreateRequest,
      TokenCreateRequestExecutor.TokenCreateException,
      Token
    ] {

  override protected type ApiExceptionWrapper =
    TokenCreateRequestExecutor.TokenCreateException.Api

  override protected type UnspecifiedException =
    TokenCreateRequestExecutor.TokenCreateException.Unspecified
}

object TokenCreateRequestExecutor {

  sealed trait TokenCreateRequest {
    def accountSid: TwilioAccount.Sid
  }

  object TokenCreateRequest {

    type BuilderStartState = Builder[PhantomTypes.AccountSidSetFalse]

    /** Phantom types used to enforce compile time constraints on the Builder */
    object PhantomTypes {

      sealed trait AccountSidSet
      sealed trait AccountSidSetTrue  extends AccountSidSet
      sealed trait AccountSidSetFalse extends AccountSidSet
    }

    final class Builder[AccountSidSet <: PhantomTypes.AccountSidSet] private[TokenCreateRequest] (
        accountSid: Option[TwilioAccount.Sid]
    ) {

      /** The SID of the Account that will create the resource. */
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[PhantomTypes.AccountSidSetTrue] =
        new Builder[PhantomTypes.AccountSidSetTrue](Some(accountSid))

      def build()(
          implicit accountSidSetEv: AccountSidSet =:= PhantomTypes.AccountSidSetTrue
      ): TokenCreateRequest = RequestImpl(accountSid.get)
    }

    object Builder {
      val empty = new BuilderStartState(None)
    }

    def build(fun: BuilderStartState => TokenCreateRequest): TokenCreateRequest =
      fun(Builder.empty)
  }

  private final case class RequestImpl(
      accountSid: TwilioAccount.Sid
  ) extends TokenCreateRequest

  sealed trait TokenCreateException extends RuntimeException

  object TokenCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with TokenCreateException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to create application"
          ),
          cause.orNull
        )
        with TokenCreateException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }

}
