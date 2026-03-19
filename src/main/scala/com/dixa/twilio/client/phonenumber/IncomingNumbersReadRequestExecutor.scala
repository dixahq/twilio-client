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
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.client.phonenumber.IncomingNumbersReadRequestExecutor.IncomingNumbersReadException
import com.dixa.twilio.model.phonenumber.TwilioIncomingPhoneNumber

trait IncomingNumbersReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      IncomingNumbersReadRequestExecutor.IncomingNumbersReadRequest,
      IncomingNumbersReadRequestExecutor.IncomingNumbersReadException,
      TwilioIncomingPhoneNumber,
      IncomingNumbersReadRequestExecutor.IncomingNumbersReadRequest.Builder
    ] {
  override protected final type ApiExceptionWrapper = IncomingNumbersReadException.Api

  override protected final type UnspecifiedException = IncomingNumbersReadException.Unspecified

  override protected final def createBuilderStartState()
      : IncomingNumbersReadRequestExecutor.IncomingNumbersReadRequest.Builder =
    IncomingNumbersReadRequestExecutor.IncomingNumbersReadRequest.Builder.empty
}

object IncomingNumbersReadRequestExecutor {
  final case class IncomingNumbersReadRequest(
      filter: Option[TwilioIncomingPhoneNumber.PhoneNumberFilter]
  )
  object IncomingNumbersReadRequest {
    type BuilderStartState = Builder

    final class Builder private[phonenumber] (
        filter: Option[TwilioIncomingPhoneNumber.PhoneNumberFilter]
    ) {
      def withFilter(filter: TwilioIncomingPhoneNumber.PhoneNumberFilter): Builder =
        new Builder(Some(filter))
      def build(): IncomingNumbersReadRequest = IncomingNumbersReadRequest(filter)
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(None)
    }

    def build(fun: BuilderStartState => IncomingNumbersReadRequest): IncomingNumbersReadRequest =
      fun(Builder.empty)
  }

  sealed trait IncomingNumbersReadException extends RuntimeException
  object IncomingNumbersReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with IncomingNumbersReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read incoming numbers"
          ),
          cause.orNull
        )
        with IncomingNumbersReadException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Exception) = this(None, Some(cause))
    }
  }
}
