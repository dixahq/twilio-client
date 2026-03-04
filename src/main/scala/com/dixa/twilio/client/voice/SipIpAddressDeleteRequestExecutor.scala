package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.FUnit
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{IpAccessControlList, SipIpAddress}

/** Delete an IpAddress resource.
  *
  * @see
  *   https://www.twilio.com/docs/voice/sip/api/sip-ipaddress-resource#delete-a-sip-ipaddress-resource
  */
trait SipIpAddressDeleteRequestExecutor
    extends SingleRequestExecutor[
      SipIpAddressDeleteRequestExecutor.SipIpAddressDeleteRequest,
      SipIpAddressDeleteRequestExecutor.SipIpAddressDeleteException,
      FUnit,
      SipIpAddressDeleteRequestExecutor.SipIpAddressDeleteRequest.BuilderStartState
    ] {

  import SipIpAddressDeleteRequestExecutor._

  override final protected type ApiExceptionWrapper = SipIpAddressDeleteException.Api

  override final protected type UnspecifiedException = SipIpAddressDeleteException.Unspecified

  override protected def createBuilderStartState()
      : SipIpAddressDeleteRequestExecutor.SipIpAddressDeleteRequest.BuilderStartState =
    SipIpAddressDeleteRequestExecutor.SipIpAddressDeleteRequest.Builder.empty
}

object SipIpAddressDeleteRequestExecutor {

  sealed trait SipIpAddressDeleteRequest {
    def accountSid: TwilioAccount.Sid
    def ipAccessControlListSid: IpAccessControlList.Sid
    def sid: SipIpAddress.Sid
  }

  private final case class SipIpAddressDeleteRequestImpl(
      accountSid: TwilioAccount.Sid,
      ipAccessControlListSid: IpAccessControlList.Sid,
      sid: SipIpAddress.Sid
  ) extends SipIpAddressDeleteRequest

  object SipIpAddressDeleteRequest {

    object PhantomTypes {
      sealed trait RequestAttribute
      sealed trait RequestAccountSidAttribute             extends RequestAttribute
      sealed trait RequestIpAccessControlListSidAttribute extends RequestAttribute
      sealed trait RequestSidAttribute                    extends RequestAttribute
    }

    type RequestRequiredAttributes = PhantomTypes.RequestAttribute
      with PhantomTypes.RequestAccountSidAttribute
      with PhantomTypes.RequestIpAccessControlListSidAttribute
      with PhantomTypes.RequestSidAttribute

    type BuilderStartState = Builder[PhantomTypes.RequestAttribute]

    final class Builder[
        Attributes <: PhantomTypes.RequestAttribute
    ] private[SipIpAddressDeleteRequest] (
        accountSid: Option[TwilioAccount.Sid],
        ipAccessControlListSid: Option[IpAccessControlList.Sid],
        sid: Option[SipIpAddress.Sid]
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with PhantomTypes.RequestAccountSidAttribute] =
        new Builder(Some(accountSid), ipAccessControlListSid, sid)

      def withIpAccessControlListSid(
          ipAccessControlListSid: IpAccessControlList.Sid
      ): Builder[Attributes with PhantomTypes.RequestIpAccessControlListSidAttribute] =
        new Builder(accountSid, Some(ipAccessControlListSid), sid)

      def withSid(
          sid: SipIpAddress.Sid
      ): Builder[Attributes with PhantomTypes.RequestSidAttribute] =
        new Builder(accountSid, ipAccessControlListSid, Some(sid))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): SipIpAddressDeleteRequest =
        SipIpAddressDeleteRequestImpl(
          accountSid.get,
          ipAccessControlListSid.get,
          sid.get
        )
    }

    def build(
        fun: BuilderStartState => SipIpAddressDeleteRequest
    ): SipIpAddressDeleteRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new BuilderStartState(None, None, None)
    }
  }

  sealed trait SipIpAddressDeleteException extends RuntimeException
  object SipIpAddressDeleteException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with SipIpAddressDeleteException
        with ApiExceptionWrapper

    final case class SipIpAddressNotFound(
        accountSid: TwilioAccount.Sid,
        ipAccessControlListSid: IpAccessControlList.Sid,
        sid: SipIpAddress.Sid
    ) extends RuntimeException(
          s"SipIpAddress with sid $sid was not found in " +
            s"IpAccessControlList $ipAccessControlListSid " +
            s"of account: $accountSid"
        )
        with SipIpAddressDeleteException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to delete SIP IP address"
          ),
          cause.orNull
        )
        with SipIpAddressDeleteException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
