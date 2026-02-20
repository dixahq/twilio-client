package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.IpAccessControlList

/** The ACL that is created is empty and has no IP addresses.
  *
  * You will need to add IpAddress resources to the list for it to have any effect.
  *
  * @see
  *   https://www.twilio.com/docs/voice/sip/api/sip-ipaccesscontrollist-resource#create-a-sip-ipaccesscontrollist-resource
  */
trait IpAccessControlListCreateRequestExecutor
    extends SingleRequestExecutor[
      IpAccessControlListCreateRequestExecutor.IpAccessControlListCreateRequest,
      IpAccessControlListCreateRequestExecutor.IpAccessControlListCreateException,
      IpAccessControlList,
      IpAccessControlListCreateRequestExecutor.IpAccessControlListCreateRequest.BuilderStartState
    ] {

  override protected type ApiExceptionWrapper =
    IpAccessControlListCreateRequestExecutor.IpAccessControlListCreateException.Api

  override protected type UnspecifiedException =
    IpAccessControlListCreateRequestExecutor.IpAccessControlListCreateException.Unspecified

  override protected def createBuilderStartState()
      : IpAccessControlListCreateRequestExecutor.IpAccessControlListCreateRequest.BuilderStartState =
    IpAccessControlListCreateRequestExecutor.IpAccessControlListCreateRequest.Builder.empty
}

object IpAccessControlListCreateRequestExecutor {

  sealed trait IpAccessControlListCreateRequest {
    def accountSid: TwilioAccount.Sid
    def friendlyName: Option[IpAccessControlList.FriendlyName]
  }

  object IpAccessControlListCreateRequest {

    type BuilderStartState = Builder[PhantomTypes.AccountSidSetFalse]

    /** Phantom types used to enforce compile time constraints on the Builder */
    object PhantomTypes {

      sealed trait AccountSidSet
      sealed trait AccountSidSetTrue  extends AccountSidSet
      sealed trait AccountSidSetFalse extends AccountSidSet
    }

    final class Builder[
        AccountSidSet <: PhantomTypes.AccountSidSet
    ] private[IpAccessControlListCreateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        friendlyName: Option[IpAccessControlList.FriendlyName]
    ) {

      private def copy[NewAccountSidSet <: PhantomTypes.AccountSidSet](
          accountSid: Option[TwilioAccount.Sid] = accountSid,
          friendlyName: Option[IpAccessControlList.FriendlyName] = friendlyName
      ): Builder[NewAccountSidSet] =
        new Builder(accountSid, friendlyName)

      private type BuilderWithSameTypes = Builder[AccountSidSet]

      /** The unique id of the Account responsible for this resource. */
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[PhantomTypes.AccountSidSetTrue] =
        copy(accountSid = Some(accountSid))

      /** A human readable descriptive text that describes the IpAccessControlList, up to 255
        * characters long.
        */
      def withFriendlyName(friendlyName: IpAccessControlList.FriendlyName): BuilderWithSameTypes =
        copy(friendlyName = Some(friendlyName))

      def build()(
          implicit accountSidSetEv: AccountSidSet =:= PhantomTypes.AccountSidSetTrue
      ): IpAccessControlListCreateRequest = RequestImpl(accountSid.get, friendlyName)
    }

    object Builder {
      val empty = new BuilderStartState(None, None)
    }

    def build(
        fun: BuilderStartState => IpAccessControlListCreateRequest
    ): IpAccessControlListCreateRequest =
      fun(Builder.empty)
  }

  private final case class RequestImpl(
      accountSid: TwilioAccount.Sid,
      friendlyName: Option[IpAccessControlList.FriendlyName]
  ) extends IpAccessControlListCreateRequest

  sealed trait IpAccessControlListCreateException extends RuntimeException

  object IpAccessControlListCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with IpAccessControlListCreateException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to create application"
          ),
          cause.orNull
        )
        with IpAccessControlListCreateException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }

}
