package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.IpAccessControlList

/** Read all Ip Access control lists for an account.
  *
  * @see
  *   https://www.twilio.com/docs/voice/sip/api/sip-ipaccesscontrollist-resource#read-multiple-sip-ipaccesscontrollist-resources
  */
trait IpAccessControlListReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      IpAccessControlListReadRequestExecutor.IpAccessControlListReadRequest,
      IpAccessControlListReadRequestExecutor.IpAccessControlListReadException,
      IpAccessControlList,
      IpAccessControlListReadRequestExecutor.IpAccessControlListReadRequest.BuilderStartState
    ] {

  override protected type ApiExceptionWrapper =
    IpAccessControlListReadRequestExecutor.IpAccessControlListReadException.Api

  override protected type UnspecifiedException =
    IpAccessControlListReadRequestExecutor.IpAccessControlListReadException.Unspecified

  override protected def createBuilderStartState()
      : IpAccessControlListReadRequestExecutor.IpAccessControlListReadRequest.BuilderStartState =
    IpAccessControlListReadRequestExecutor.IpAccessControlListReadRequest.Builder.empty
}

object IpAccessControlListReadRequestExecutor {

  sealed trait IpAccessControlListReadRequest {
    def accountSid: TwilioAccount.Sid
  }

  object IpAccessControlListReadRequest {

    type BuilderStartState = Builder[PhantomTypes.AccountSidSetFalse]

    /** Phantom types used to enforce compile time constraints on the Builder */
    object PhantomTypes {

      sealed trait AccountSidSet
      sealed trait AccountSidSetTrue  extends AccountSidSet
      sealed trait AccountSidSetFalse extends AccountSidSet
    }

    final class Builder[
        AccountSidSet <: PhantomTypes.AccountSidSet
    ] private[IpAccessControlListReadRequest] (
        accountSid: Option[TwilioAccount.Sid]
    ) {

      /** The SID of the Account that will read applications from. */
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[PhantomTypes.AccountSidSetTrue] =
        new Builder[PhantomTypes.AccountSidSetTrue](Some(accountSid))

      def build()(
          implicit accountSidSetEv: AccountSidSet =:= PhantomTypes.AccountSidSetTrue
      ): IpAccessControlListReadRequest = RequestImpl(accountSid.get)
    }

    object Builder {
      val empty = new BuilderStartState(None)
    }

    def build(
        fun: BuilderStartState => IpAccessControlListReadRequest
    ): IpAccessControlListReadRequest =
      fun(Builder.empty)
  }

  private final case class RequestImpl(accountSid: TwilioAccount.Sid)
      extends IpAccessControlListReadRequest

  sealed trait IpAccessControlListReadException extends RuntimeException

  object IpAccessControlListReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with IpAccessControlListReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to riad applications"
          ),
          cause.orNull
        )
        with IpAccessControlListReadException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }

}
