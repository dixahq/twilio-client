package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{IpAccessControlList, SipIpAddress}

/** You can add up to 100 IP addresses to an IpAccessControlList.
  *
  * ip_address must be a complete IP address; wildcards are not supported.
  *
  * @see
  *   https://www.twilio.com/docs/voice/sip/api/sip-ipaddress-resource#create-a-sip-ipaddress-resource
  */
trait SipIpAddressCreateRequestExecutor
    extends SingleRequestExecutor[
      SipIpAddressCreateRequestExecutor.SipIpAddressCreateRequest,
      SipIpAddressCreateRequestExecutor.SipIpAddressCreateException,
      SipIpAddress
    ] {

  override protected type ApiExceptionWrapper =
    SipIpAddressCreateRequestExecutor.SipIpAddressCreateException.Api

  override protected type UnspecifiedException =
    SipIpAddressCreateRequestExecutor.SipIpAddressCreateException.Unspecified
}

object SipIpAddressCreateRequestExecutor {

  sealed trait SipIpAddressCreateRequest {
    def accountSid: TwilioAccount.Sid
    def ipAccessControlListSid: IpAccessControlList.Sid
    def friendlyName: SipIpAddress.FriendlyName
    def ipAddress: SipIpAddress.IpAddress
    def cidrPrefixLength: Option[SipIpAddress.CidrPrefixLength]
  }

  object SipIpAddressCreateRequest {

    type BuilderStartState = Builder[
      PhantomTypes.AccountSidSetFalse,
      PhantomTypes.IpAccessControlListSidSetFalse,
      PhantomTypes.FriendlyNameSetFalse,
      PhantomTypes.IpAddressFalse
    ]

    /** Phantom types used to enforce compile time constraints on the Builder */
    object PhantomTypes {

      sealed trait AccountSidSet
      sealed trait AccountSidSetTrue  extends AccountSidSet
      sealed trait AccountSidSetFalse extends AccountSidSet

      sealed trait IpAccessControlListSidSet
      sealed trait IpAccessControlListSidSetTrue  extends IpAccessControlListSidSet
      sealed trait IpAccessControlListSidSetFalse extends IpAccessControlListSidSet

      sealed trait FriendlyNameSet
      sealed trait FriendlyNameSetTrue  extends FriendlyNameSet
      sealed trait FriendlyNameSetFalse extends FriendlyNameSet

      sealed trait IpAddressSet
      sealed trait IpAddressSetTrue extends IpAddressSet
      sealed trait IpAddressFalse   extends IpAddressSet
    }

    final class Builder[
        AccountSidSet <: PhantomTypes.AccountSidSet,
        IpAccessControlListSidSet <: PhantomTypes.IpAccessControlListSidSet,
        FriendlyNameSet <: PhantomTypes.FriendlyNameSet,
        IpAddressSet <: PhantomTypes.IpAddressSet
    ] private[SipIpAddressCreateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        ipAccessControlListSid: Option[IpAccessControlList.Sid],
        friendlyName: Option[SipIpAddress.FriendlyName],
        ipAddress: Option[SipIpAddress.IpAddress],
        cidrPrefixLength: Option[SipIpAddress.CidrPrefixLength]
    ) {

      private def copy[
          NewAccountSidSet <: PhantomTypes.AccountSidSet,
          NewIpAccessControlListSidSet <: PhantomTypes.IpAccessControlListSidSet,
          NewFriendlyNameSet <: PhantomTypes.FriendlyNameSet,
          NewIpAddressSet <: PhantomTypes.IpAddressSet
      ](
          accountSid: Option[TwilioAccount.Sid] = accountSid,
          ipAccessControlListSid: Option[IpAccessControlList.Sid] = ipAccessControlListSid,
          friendlyName: Option[SipIpAddress.FriendlyName] = friendlyName,
          ipAddress: Option[SipIpAddress.IpAddress] = ipAddress,
          cidrPrefixLength: Option[SipIpAddress.CidrPrefixLength] = cidrPrefixLength
      ): Builder[
        NewAccountSidSet,
        NewIpAccessControlListSidSet,
        NewFriendlyNameSet,
        NewIpAddressSet
      ] =
        new Builder(
          accountSid,
          ipAccessControlListSid,
          friendlyName,
          ipAddress,
          cidrPrefixLength
        )

      private type BuilderWithSameTypes =
        Builder[
          AccountSidSet,
          IpAccessControlListSidSet,
          FriendlyNameSet,
          IpAddressSet
        ]

      /** The SID of the Account that will create the resource. */
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[
        PhantomTypes.AccountSidSetTrue,
        IpAccessControlListSidSet,
        FriendlyNameSet,
        IpAddressSet
      ] =
        copy(accountSid = Some(accountSid))

      /** The IpAccessControlList Sid with which to associate the created IpAddress resource. */
      def withIpAccessControlListSid(ipAccessControlListSid: IpAccessControlList.Sid): Builder[
        AccountSidSet,
        PhantomTypes.IpAccessControlListSidSetTrue,
        FriendlyNameSet,
        IpAddressSet
      ] = copy(ipAccessControlListSid = Some(ipAccessControlListSid))

      /** A human readable descriptive text for this resource, up to 255 characters long. */
      def withFriendlyName(
          friendlyName: SipIpAddress.FriendlyName
      ): Builder[
        AccountSidSet,
        IpAccessControlListSidSet,
        PhantomTypes.FriendlyNameSetTrue,
        IpAddressSet
      ] = copy(friendlyName = Some(friendlyName))

      /** An IP address in dotted decimal notation from which you want to accept traffic. Any SIP
        * requests from this IP address will be allowed by Twilio. IPv4 only supported today.
        */
      def withIpAddress(
          ipAddress: SipIpAddress.IpAddress
      ): Builder[
        AccountSidSet,
        IpAccessControlListSidSet,
        FriendlyNameSet,
        PhantomTypes.IpAddressSetTrue
      ] = copy(ipAddress = Some(ipAddress))

      /** An integer representing the length of the CIDR prefix to use with this IP address when
        * accepting traffic. By default the entire IP address is used.
        */
      def withCidrPrefixLength(
          cidrPrefixLength: SipIpAddress.CidrPrefixLength
      ): BuilderWithSameTypes =
        copy(cidrPrefixLength = Some(cidrPrefixLength))

      def build()(
          implicit accountSidSetEv: AccountSidSet =:= PhantomTypes.AccountSidSetTrue,
          ipAccessControlListSetEv: IpAccessControlListSidSet =:= PhantomTypes.IpAccessControlListSidSetTrue,
          friendlyNameSetEv: FriendlyNameSet =:= PhantomTypes.FriendlyNameSetTrue,
          ipAddressSetEv: IpAccessControlListSidSet =:= PhantomTypes.IpAccessControlListSidSetTrue
      ): SipIpAddressCreateRequest = RequestImpl(
        accountSid.get,
        ipAccessControlListSid.get,
        friendlyName.get,
        ipAddress.get,
        cidrPrefixLength
      )
    }

    object Builder {
      val empty =
        new BuilderStartState(None, None, None, None, None)
    }

    def build(fun: BuilderStartState => SipIpAddressCreateRequest): SipIpAddressCreateRequest =
      fun(Builder.empty)
  }

  private final case class RequestImpl(
      accountSid: TwilioAccount.Sid,
      ipAccessControlListSid: IpAccessControlList.Sid,
      friendlyName: SipIpAddress.FriendlyName,
      ipAddress: SipIpAddress.IpAddress,
      cidrPrefixLength: Option[SipIpAddress.CidrPrefixLength]
  ) extends SipIpAddressCreateRequest

  sealed trait SipIpAddressCreateException extends RuntimeException

  object SipIpAddressCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with SipIpAddressCreateException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to create application"
          ),
          cause.orNull
        )
        with SipIpAddressCreateException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }

}
