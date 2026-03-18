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

package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.messaging.PhoneNumberCreateRequestExecutor.PhoneNumberCreateException
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.messaging.{TwilioMessagingPhoneNumber, TwilioMessagingService}
import com.dixa.twilio.model.phonenumber.TwilioPhoneNumber

trait PhoneNumberCreateRequestExecutor
    extends SingleRequestExecutor[
      PhoneNumberCreateRequestExecutor.PhoneNumberCreateRequest,
      PhoneNumberCreateRequestExecutor.PhoneNumberCreateException,
      TwilioMessagingPhoneNumber,
      PhoneNumberCreateRequestExecutor.PhoneNumberCreateRequest.Builder
    ] {

  override protected final type ApiExceptionWrapper = PhoneNumberCreateException.Api

  override protected final type UnspecifiedException = PhoneNumberCreateException.Unspecified

  override protected final def createBuilderStartState()
      : PhoneNumberCreateRequestExecutor.PhoneNumberCreateRequest.Builder =
    PhoneNumberCreateRequestExecutor.PhoneNumberCreateRequest.Builder.empty
}

object PhoneNumberCreateRequestExecutor {

  final case class PhoneNumberCreateRequest(
      serviceSid: TwilioMessagingService.Sid,
      phoneNumberSid: TwilioPhoneNumber.Sid
  )
  object PhoneNumberCreateRequest {
    type BuilderStartState = Builder

    final class Builder private[messaging] (
        serviceSid: Option[TwilioMessagingService.Sid],
        phoneNumberSid: Option[TwilioPhoneNumber.Sid]
    ) {
      def withServiceSid(serviceSid: TwilioMessagingService.Sid): Builder =
        new Builder(Some(serviceSid), phoneNumberSid)
      def withPhoneNumberSid(phoneNumberSid: TwilioPhoneNumber.Sid): Builder =
        new Builder(serviceSid, Some(phoneNumberSid))
      def build(): PhoneNumberCreateRequest =
        PhoneNumberCreateRequest(serviceSid.get, phoneNumberSid.get)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(None, None)
    }

    def build(fun: BuilderStartState => PhoneNumberCreateRequest): PhoneNumberCreateRequest =
      fun(Builder.empty)
  }

  sealed trait PhoneNumberCreateException extends RuntimeException
  object PhoneNumberCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with PhoneNumberCreateException
        with ApiExceptionWrapper

    final case class PhoneNumberAlreadyInMessagingService()
        extends IllegalStateException(
          "Phone Number or Short Code is already in the Messaging Service. More info: https://www.twilio.com/docs/errors/21710"
        )
        with PhoneNumberCreateException
    final case class PhoneNumberAssociatedWithOtherMessagingService()
        extends IllegalStateException(
          "Phone Number or Short Code is associated with another Messaging Service. More info: https://www.twilio.com/docs/errors/21712"
        )
        with PhoneNumberCreateException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to add phone number to Messaging Service"
          ),
          cause.orNull
        )
        with PhoneNumberCreateException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }
}
