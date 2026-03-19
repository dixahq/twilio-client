// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{IpAccessControlList, SipIpAddress}

/** Update an existing IpAddress resource.
  *
  * @see
  *   https://www.twilio.com/docs/voice/sip/api/sip-ipaddress-resource#update-a-sip-ipaddress-resource
  */
trait SipIpAddressUpdateRequestExecutor
    extends SingleRequestExecutor[
      SipIpAddressUpdateRequestExecutor.SipIpAddressUpdateRequest,
      SipIpAddressUpdateRequestExecutor.SipIpAddressUpdateException,
      SipIpAddress,
      SipIpAddressUpdateRequestExecutor.SipIpAddressUpdateRequest.BuilderStartState
    ] {

  import SipIpAddressUpdateRequestExecutor._

  override final protected type ApiExceptionWrapper = SipIpAddressUpdateException.Api

  override final protected type UnspecifiedException = SipIpAddressUpdateException.Unspecified

  override protected def createBuilderStartState()
      : SipIpAddressUpdateRequestExecutor.SipIpAddressUpdateRequest.BuilderStartState =
    SipIpAddressUpdateRequestExecutor.SipIpAddressUpdateRequest.Builder.empty
}

object SipIpAddressUpdateRequestExecutor {

  sealed trait SipIpAddressUpdateRequest {
    def accountSid: TwilioAccount.Sid
    def ipAccessControlListSid: IpAccessControlList.Sid
    def sid: SipIpAddress.Sid
    def friendlyName: Option[SipIpAddress.FriendlyName]
    def ipAddress: Option[SipIpAddress.IpAddress]
    def cidrPrefixLength: Option[SipIpAddress.CidrPrefixLength]
  }

  private final case class SipIpAddressUpdateRequestImpl(
      accountSid: TwilioAccount.Sid,
      ipAccessControlListSid: IpAccessControlList.Sid,
      sid: SipIpAddress.Sid,
      friendlyName: Option[SipIpAddress.FriendlyName],
      ipAddress: Option[SipIpAddress.IpAddress],
      cidrPrefixLength: Option[SipIpAddress.CidrPrefixLength]
  ) extends SipIpAddressUpdateRequest

  object SipIpAddressUpdateRequest {

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
    ] private[SipIpAddressUpdateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        ipAccessControlListSid: Option[IpAccessControlList.Sid],
        sid: Option[SipIpAddress.Sid],
        friendlyName: Option[SipIpAddress.FriendlyName],
        ipAddress: Option[SipIpAddress.IpAddress],
        cidrPrefixLength: Option[SipIpAddress.CidrPrefixLength]
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with PhantomTypes.RequestAccountSidAttribute] =
        new Builder(
          Some(accountSid),
          ipAccessControlListSid,
          sid,
          friendlyName,
          ipAddress,
          cidrPrefixLength
        )

      def withIpAccessControlListSid(
          ipAccessControlListSid: IpAccessControlList.Sid
      ): Builder[Attributes with PhantomTypes.RequestIpAccessControlListSidAttribute] =
        new Builder(
          accountSid,
          Some(ipAccessControlListSid),
          sid,
          friendlyName,
          ipAddress,
          cidrPrefixLength
        )

      def withSid(
          sid: SipIpAddress.Sid
      ): Builder[Attributes with PhantomTypes.RequestSidAttribute] =
        new Builder(
          accountSid,
          ipAccessControlListSid,
          Some(sid),
          friendlyName,
          ipAddress,
          cidrPrefixLength
        )

      def withFriendlyName(
          friendlyName: SipIpAddress.FriendlyName
      ): Builder[Attributes] =
        new Builder(
          accountSid,
          ipAccessControlListSid,
          sid,
          Some(friendlyName),
          ipAddress,
          cidrPrefixLength
        )

      def withIpAddress(
          ipAddress: SipIpAddress.IpAddress
      ): Builder[Attributes] =
        new Builder(
          accountSid,
          ipAccessControlListSid,
          sid,
          friendlyName,
          Some(ipAddress),
          cidrPrefixLength
        )

      def withCidrPrefixLength(
          cidrPrefixLength: SipIpAddress.CidrPrefixLength
      ): Builder[Attributes] =
        new Builder(
          accountSid,
          ipAccessControlListSid,
          sid,
          friendlyName,
          ipAddress,
          Some(cidrPrefixLength)
        )

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): SipIpAddressUpdateRequest =
        SipIpAddressUpdateRequestImpl(
          accountSid.get,
          ipAccessControlListSid.get,
          sid.get,
          friendlyName,
          ipAddress,
          cidrPrefixLength
        )
    }

    def build(
        fun: BuilderStartState => SipIpAddressUpdateRequest
    ): SipIpAddressUpdateRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new BuilderStartState(None, None, None, None, None, None)
    }
  }

  sealed trait SipIpAddressUpdateException extends RuntimeException
  object SipIpAddressUpdateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with SipIpAddressUpdateException
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
        with SipIpAddressUpdateException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to update SIP IP address"
          ),
          cause.orNull
        )
        with SipIpAddressUpdateException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
