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

package com.dixa.twilio.client.phonenumber

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.phonenumber.{PhoneNumberE164, PhoneNumberRoutingVoice}

/** Fetch the routing region for a phone number from Twilio's Voice Routing API.
  *
  * @see
  *   https://www.twilio.com/docs/global-infrastructure/inbound-processing-region-api-phone-number#fetch-a-phonenumbers-current-inbound-processing-region-configuration
  */
trait PhoneNumberRoutingVoiceReadRequestExecutor
    extends SingleRequestExecutor[
      PhoneNumberRoutingVoiceReadRequestExecutor.PhoneNumberRoutingVoiceReadRequest,
      PhoneNumberRoutingVoiceReadRequestExecutor.PhoneNumberRoutingVoiceReadException,
      PhoneNumberRoutingVoice,
      PhoneNumberRoutingVoiceReadRequestExecutor.PhoneNumberRoutingVoiceReadRequest.BuilderStartState
    ] {

  import PhoneNumberRoutingVoiceReadRequestExecutor._

  override protected final type ApiExceptionWrapper =
    PhoneNumberRoutingVoiceReadException.Api

  override protected final type UnspecifiedException =
    PhoneNumberRoutingVoiceReadException.Unspecified

  override protected final def createBuilderStartState()
      : PhoneNumberRoutingVoiceReadRequest.BuilderStartState =
    PhoneNumberRoutingVoiceReadRequest.Builder.empty
}

object PhoneNumberRoutingVoiceReadRequestExecutor {

  sealed trait PhoneNumberRoutingVoiceReadRequest {
    def phoneNumber: PhoneNumberE164
  }

  private final case class PhoneNumberRoutingVoiceReadRequestImpl(
      phoneNumber: PhoneNumberE164
  ) extends PhoneNumberRoutingVoiceReadRequest

  object PhoneNumberRoutingVoiceReadRequest {

    object PhantomTypes {
      sealed trait RequestAttribute
      sealed trait RequestPhoneNumberAttribute extends RequestAttribute
    }

    type RequestRequiredAttributes =
      PhantomTypes.RequestAttribute with PhantomTypes.RequestPhoneNumberAttribute

    type BuilderStartState = Builder[PhantomTypes.RequestAttribute]

    final class Builder[
        Attributes <: PhantomTypes.RequestAttribute
    ] private[PhoneNumberRoutingVoiceReadRequest] (
        phoneNumber: Option[PhoneNumberE164]
    ) {

      def withPhoneNumber(
          phoneNumber: PhoneNumberE164
      ): Builder[Attributes with PhantomTypes.RequestPhoneNumberAttribute] =
        new Builder(Some(phoneNumber))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): PhoneNumberRoutingVoiceReadRequest =
        PhoneNumberRoutingVoiceReadRequestImpl(phoneNumber.get)
    }

    object Builder {
      val empty: BuilderStartState = new Builder(None)
    }

    def build(
        fun: BuilderStartState => PhoneNumberRoutingVoiceReadRequest
    ): PhoneNumberRoutingVoiceReadRequest = fun(Builder.empty)
  }

  sealed trait PhoneNumberRoutingVoiceReadException extends RuntimeException

  object PhoneNumberRoutingVoiceReadException {

    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with PhoneNumberRoutingVoiceReadException
        with ApiExceptionWrapper

    final case class PhoneNumberNotFound(phoneNumber: PhoneNumberE164)
        extends RuntimeException(
          s"Phone number $phoneNumber was not found in Twilio routing"
        )
        with PhoneNumberRoutingVoiceReadException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch phone number routing region"
          ),
          cause.orNull
        )
        with PhoneNumberRoutingVoiceReadException
  }
}
