package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{IpAccessControlList, IpAccessControlListMapping, SipDomain}

/** Create a mapping between a SipDomain and a IpAccessControlList
  *
  * @see
  *   [[com.dixa.twilio.model.voice.SipDomain]]
  * @see
  *   [[com.dixa.twilio.model.voice.IpAccessControlList]]
  * @see
  *   https://www.twilio.com/docs/voice/sip/api/sip-ipaccesscontrollistmapping-resource#create-a-sip-ipaccesscontrollistmapping-resource
  */
trait IpAccessControlListMappingCreateRequestExecutor
    extends SingleRequestExecutor[
      IpAccessControlListMappingCreateRequestExecutor.IpAccessControlListMappingCreateRequest,
      IpAccessControlListMappingCreateRequestExecutor.IpAccessControlListMappingCreateException,
      IpAccessControlListMapping,
      IpAccessControlListMappingCreateRequestExecutor.IpAccessControlListMappingCreateRequest.BuilderStartState
    ] {

  override protected type ApiExceptionWrapper =
    IpAccessControlListMappingCreateRequestExecutor.IpAccessControlListMappingCreateException.Api

  override protected type UnspecifiedException =
    IpAccessControlListMappingCreateRequestExecutor.IpAccessControlListMappingCreateException.Unspecified

  override protected def createBuilderStartState()
      : IpAccessControlListMappingCreateRequestExecutor.IpAccessControlListMappingCreateRequest.BuilderStartState =
    IpAccessControlListMappingCreateRequestExecutor.IpAccessControlListMappingCreateRequest.Builder.empty
}

object IpAccessControlListMappingCreateRequestExecutor {

  sealed trait IpAccessControlListMappingCreateRequest {
    def accountSid: TwilioAccount.Sid
    def domainSid: SipDomain.Sid
    def ipAccessControlListSid: IpAccessControlList.Sid

  }

  object IpAccessControlListMappingCreateRequest {

    type BuilderStartState = Builder[
      PhantomTypes.AccountSidSetFalse,
      PhantomTypes.DomainSidSetFalse,
      PhantomTypes.IpAccessControlListSidSetFalse
    ]

    /** Phantom types used to enforce compile time constraints on the Builder */
    object PhantomTypes {

      sealed trait AccountSidSet
      sealed trait AccountSidSetTrue  extends AccountSidSet
      sealed trait AccountSidSetFalse extends AccountSidSet

      sealed trait DomainSidSet
      sealed trait DomainSidSetTrue  extends DomainSidSet
      sealed trait DomainSidSetFalse extends DomainSidSet

      sealed trait IpAccessControlListSidSet
      sealed trait IpAccessControlListSidSetTrue  extends IpAccessControlListSidSet
      sealed trait IpAccessControlListSidSetFalse extends IpAccessControlListSidSet
    }

    final class Builder[
        AccountSidSet <: PhantomTypes.AccountSidSet,
        DomainSidSet <: PhantomTypes.DomainSidSet,
        IpAccessControlListSidSet <: PhantomTypes.IpAccessControlListSidSet
    ] private[IpAccessControlListMappingCreateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        domainSid: Option[SipDomain.Sid],
        ipAccessControlListSid: Option[IpAccessControlList.Sid]
    ) {

      private def copy[
          NewAccountSidSet <: PhantomTypes.AccountSidSet,
          NewDomainSidSet <: PhantomTypes.DomainSidSet,
          NewIpAccessControlListSidSet <: PhantomTypes.IpAccessControlListSidSet
      ](
          accountSid: Option[TwilioAccount.Sid] = accountSid,
          domainSid: Option[SipDomain.Sid] = domainSid,
          ipAccessControlListSid: Option[IpAccessControlList.Sid] = ipAccessControlListSid
      ): Builder[
        NewAccountSidSet,
        NewDomainSidSet,
        NewIpAccessControlListSidSet
      ] =
        new Builder(
          accountSid,
          domainSid,
          ipAccessControlListSid
        )

      /** The SID of the Account that will create the resource. */
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[
        PhantomTypes.AccountSidSetTrue,
        DomainSidSet,
        IpAccessControlListSidSet
      ] =
        copy(accountSid = Some(accountSid))

      def withDomainSid(
          domainSid: SipDomain.Sid
      ): Builder[AccountSidSet, PhantomTypes.DomainSidSetTrue, IpAccessControlListSidSet] =
        copy(domainSid = Some(domainSid))

      /** The SID of the IpAccessControlList resource to map to the SIP domain. */
      def withIpAccessControlListSid(ipAccessControlListSid: IpAccessControlList.Sid): Builder[
        AccountSidSet,
        DomainSidSet,
        PhantomTypes.IpAccessControlListSidSetTrue
      ] = copy(ipAccessControlListSid = Some(ipAccessControlListSid))

      def build()(
          implicit accountSidSetEv: AccountSidSet =:= PhantomTypes.AccountSidSetTrue,
          domainSidSetEv: DomainSidSet =:= PhantomTypes.DomainSidSetTrue,
          ipAccessControlListSetEv: IpAccessControlListSidSet =:= PhantomTypes.IpAccessControlListSidSetTrue
      ): IpAccessControlListMappingCreateRequest = RequestImpl(
        accountSid.get,
        domainSid.get,
        ipAccessControlListSid.get
      )
    }

    object Builder {
      val empty =
        new BuilderStartState(None, None, None)
    }

    def build(
        fun: BuilderStartState => IpAccessControlListMappingCreateRequest
    ): IpAccessControlListMappingCreateRequest =
      fun(Builder.empty)
  }

  private final case class RequestImpl(
      accountSid: TwilioAccount.Sid,
      domainSid: SipDomain.Sid,
      ipAccessControlListSid: IpAccessControlList.Sid
  ) extends IpAccessControlListMappingCreateRequest

  sealed trait IpAccessControlListMappingCreateException extends RuntimeException

  object IpAccessControlListMappingCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with IpAccessControlListMappingCreateException
        with ApiExceptionWrapper

    final case class IpAccessControlListMappingAlreadyExists(
        domainSid: SipDomain.Sid,
        ipAccessControlListSid: IpAccessControlList.Sid
    ) extends RuntimeException(
          s"IpAccessControlListMapping between $domainSid and $ipAccessControlListSid already exists"
        )
        with IpAccessControlListMappingCreateException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to create application"
          ),
          cause.orNull
        )
        with IpAccessControlListMappingCreateException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }

}
