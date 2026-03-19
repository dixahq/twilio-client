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

import org.apache.pekko.Done
import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.messaging.PhoneNumberDeleteRequestExecutor.PhoneNumberDeleteException
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.messaging.TwilioMessagingService
import com.dixa.twilio.model.phonenumber.TwilioPhoneNumber

trait PhoneNumberDeleteRequestExecutor
    extends SingleRequestExecutor[
      PhoneNumberDeleteRequestExecutor.PhoneNumberDeleteRequest,
      PhoneNumberDeleteRequestExecutor.PhoneNumberDeleteException,
      Done,
      PhoneNumberDeleteRequestExecutor.PhoneNumberDeleteRequest.Builder
    ] {

  override protected final type ApiExceptionWrapper = PhoneNumberDeleteException.Api

  override protected final type UnspecifiedException = PhoneNumberDeleteException.UnspecifiedError

  override protected final def createBuilderStartState()
      : PhoneNumberDeleteRequestExecutor.PhoneNumberDeleteRequest.Builder =
    PhoneNumberDeleteRequestExecutor.PhoneNumberDeleteRequest.Builder.empty
}

object PhoneNumberDeleteRequestExecutor {

  final case class PhoneNumberDeleteRequest(
      serviceSid: TwilioMessagingService.Sid,
      phoneNumberSid: TwilioPhoneNumber.Sid
  )
  object PhoneNumberDeleteRequest {
    type BuilderStartState = Builder

    final class Builder private[messaging] (
        serviceSid: Option[TwilioMessagingService.Sid],
        phoneNumberSid: Option[TwilioPhoneNumber.Sid]
    ) {
      def withServiceSid(serviceSid: TwilioMessagingService.Sid): Builder =
        new Builder(Some(serviceSid), phoneNumberSid)
      def withPhoneNumberSid(phoneNumberSid: TwilioPhoneNumber.Sid): Builder =
        new Builder(serviceSid, Some(phoneNumberSid))
      def build(): PhoneNumberDeleteRequest =
        PhoneNumberDeleteRequest(serviceSid.get, phoneNumberSid.get)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(None, None)
    }

    def build(fun: BuilderStartState => PhoneNumberDeleteRequest): PhoneNumberDeleteRequest =
      fun(Builder.empty)
  }

  sealed trait PhoneNumberDeleteException extends RuntimeException
  object PhoneNumberDeleteException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with PhoneNumberDeleteException
        with ApiExceptionWrapper

    final case class NotFound(msg: String)
        extends IllegalStateException(msg)
        with PhoneNumberDeleteException

    final case class UnspecifiedError(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to add phone number to Messaging Service"
          ),
          cause.orNull
        )
        with PhoneNumberDeleteException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }
}
