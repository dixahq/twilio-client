package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{IpAccessControlList, SipIpAddress}

/** Read all IpAddress resources within an IpAccessControlList.
  *
  * @see
  *   https://www.twilio.com/docs/voice/sip/api/sip-ipaddress-resource#read-multiple-sip-ipaddress-resources
  */
trait SipIpAddressReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      SipIpAddressReadRequestExecutor.SipIpAddressReadRequest,
      SipIpAddressReadRequestExecutor.SipIpAddressReadException,
      SipIpAddress
    ] {

  override protected type ApiExceptionWrapper =
    SipIpAddressReadRequestExecutor.SipIpAddressReadException.Api

  override protected type UnspecifiedException =
    SipIpAddressReadRequestExecutor.SipIpAddressReadException.Unspecified
}

object SipIpAddressReadRequestExecutor {

  sealed trait SipIpAddressReadRequest {
    def accountSid: TwilioAccount.Sid
    def ipAccessControlListSid: IpAccessControlList.Sid
  }

  private final case class SipIpAddressReadRequestImpl(
      accountSid: TwilioAccount.Sid,
      ipAccessControlListSid: IpAccessControlList.Sid
  ) extends SipIpAddressReadRequest

  object SipIpAddressReadRequest {

    object PhantomTypes {
      sealed trait RequestAttribute
      sealed trait RequestAccountSidAttribute             extends RequestAttribute
      sealed trait RequestIpAccessControlListSidAttribute extends RequestAttribute
    }

    type RequestRequiredAttributes = PhantomTypes.RequestAttribute
      with PhantomTypes.RequestAccountSidAttribute
      with PhantomTypes.RequestIpAccessControlListSidAttribute

    type BuilderStartState = Builder[PhantomTypes.RequestAttribute]

    final class Builder[
        Attributes <: PhantomTypes.RequestAttribute
    ] private[SipIpAddressReadRequest] (
        accountSid: Option[TwilioAccount.Sid],
        ipAccessControlListSid: Option[IpAccessControlList.Sid]
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with PhantomTypes.RequestAccountSidAttribute] =
        new Builder(Some(accountSid), ipAccessControlListSid)

      def withIpAccessControlListSid(
          ipAccessControlListSid: IpAccessControlList.Sid
      ): Builder[Attributes with PhantomTypes.RequestIpAccessControlListSidAttribute] =
        new Builder(accountSid, Some(ipAccessControlListSid))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): SipIpAddressReadRequest =
        SipIpAddressReadRequestImpl(accountSid.get, ipAccessControlListSid.get)
    }

    def build(
        fun: BuilderStartState => SipIpAddressReadRequest
    ): SipIpAddressReadRequest =
      fun(new BuilderStartState(None, None))

  }

  sealed trait SipIpAddressReadException extends RuntimeException
  object SipIpAddressReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with SipIpAddressReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read SIP IP addresses"
          ),
          cause.orNull
        )
        with SipIpAddressReadException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
