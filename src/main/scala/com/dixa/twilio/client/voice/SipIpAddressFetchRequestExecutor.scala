// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.voice.SipIpAddressFetchRequestExecutor.SipIpAddressFetchRequest.BuilderStartState
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{IpAccessControlList, SipIpAddress}

/** Fetch a single IpAddress resource.
  *
  * @see
  *   https://www.twilio.com/docs/voice/sip/api/sip-ipaddress-resource#fetch-a-sip-ipaddress-resource
  */
trait SipIpAddressFetchRequestExecutor
    extends SingleRequestExecutor[
      SipIpAddressFetchRequestExecutor.SipIpAddressFetchRequest,
      SipIpAddressFetchRequestExecutor.SipIpAddressFetchException,
      SipIpAddress,
      SipIpAddressFetchRequestExecutor.SipIpAddressFetchRequest.BuilderStartState
    ] {

  import SipIpAddressFetchRequestExecutor._

  override final protected type ApiExceptionWrapper = SipIpAddressFetchException.Api

  override final protected type UnspecifiedException = SipIpAddressFetchException.Unspecified

  override final protected def createBuilderStartState(): BuilderStartState =
    SipIpAddressFetchRequest.Builder.empty
}

object SipIpAddressFetchRequestExecutor {

  sealed trait SipIpAddressFetchRequest {
    def accountSid: TwilioAccount.Sid
    def ipAccessControlListSid: IpAccessControlList.Sid
    def sid: SipIpAddress.Sid
  }

  private final case class SipIpAddressFetchRequestImpl(
      accountSid: TwilioAccount.Sid,
      ipAccessControlListSid: IpAccessControlList.Sid,
      sid: SipIpAddress.Sid
  ) extends SipIpAddressFetchRequest

  object SipIpAddressFetchRequest {

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
    ] private[SipIpAddressFetchRequestExecutor] (
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
      ): SipIpAddressFetchRequest =
        SipIpAddressFetchRequestImpl(
          accountSid.get,
          ipAccessControlListSid.get,
          sid.get
        )
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(None, None, None)
    }

    def build(
        fun: BuilderStartState => SipIpAddressFetchRequest
    ): SipIpAddressFetchRequest =
      fun(Builder.empty)

  }

  sealed trait SipIpAddressFetchException extends RuntimeException
  object SipIpAddressFetchException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with SipIpAddressFetchException
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
        with SipIpAddressFetchException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch SIP IP address"
          ),
          cause.orNull
        )
        with SipIpAddressFetchException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
