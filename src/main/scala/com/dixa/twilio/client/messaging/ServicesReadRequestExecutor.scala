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
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.client.messaging.ServicesReadRequestExecutor.ServicesReadException
import com.dixa.twilio.model.messaging.TwilioMessagingService

trait ServicesReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      ServicesReadRequestExecutor.ServicesReadRequest,
      ServicesReadRequestExecutor.ServicesReadException,
      TwilioMessagingService,
      ServicesReadRequestExecutor.ServicesReadRequest.Builder
    ] {
  override protected final type ApiExceptionWrapper = ServicesReadException.Api

  override protected final type UnspecifiedException = ServicesReadException.Unspecified

  override protected final def createBuilderStartState()
      : ServicesReadRequestExecutor.ServicesReadRequest.Builder =
    ServicesReadRequestExecutor.ServicesReadRequest.Builder.empty
}

object ServicesReadRequestExecutor {
  final case class ServicesReadRequest()
  object ServicesReadRequest {
    type BuilderStartState = Builder

    final class Builder private[messaging] () {
      def build(): ServicesReadRequest = ServicesReadRequest()
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState()
    }

    def build(fun: BuilderStartState => ServicesReadRequest): ServicesReadRequest =
      fun(Builder.empty)
  }

  sealed trait ServicesReadException extends RuntimeException
  object ServicesReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ServicesReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read services"
          ),
          cause.orNull
        )
        with ServicesReadException
  }
}
