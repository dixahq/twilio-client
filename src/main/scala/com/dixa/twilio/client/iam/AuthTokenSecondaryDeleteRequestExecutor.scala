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

package com.dixa.twilio.client.iam

import org.apache.pekko.Done
import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}

trait AuthTokenSecondaryDeleteRequestExecutor
    extends SingleRequestExecutor[
      AuthTokenSecondaryDeleteRequestExecutor.AuthTokenSecondaryDeleteRequest,
      AuthTokenSecondaryDeleteRequestExecutor.AuthTokenSecondaryDeleteException,
      Done,
      AuthTokenSecondaryDeleteRequestExecutor.AuthTokenSecondaryDeleteRequest.Builder
    ] {

  import AuthTokenSecondaryDeleteRequestExecutor._

  override final protected type ApiExceptionWrapper = AuthTokenSecondaryDeleteException.Api

  override final protected type UnspecifiedException =
    AuthTokenSecondaryDeleteException.UnspecifiedError

  override final protected def createBuilderStartState(): AuthTokenSecondaryDeleteRequest.Builder =
    AuthTokenSecondaryDeleteRequest.Builder.empty
}

object AuthTokenSecondaryDeleteRequestExecutor {

  final case class AuthTokenSecondaryDeleteRequest()
  object AuthTokenSecondaryDeleteRequest {
    type BuilderStartState = Builder

    final class Builder private[iam] () {
      def build(): AuthTokenSecondaryDeleteRequest = AuthTokenSecondaryDeleteRequest()
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState()
    }

    def build(
        fun: BuilderStartState => AuthTokenSecondaryDeleteRequest
    ): AuthTokenSecondaryDeleteRequest = fun(Builder.empty)
  }

  sealed trait AuthTokenSecondaryDeleteException extends RuntimeException
  object AuthTokenSecondaryDeleteException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with AuthTokenSecondaryDeleteException
        with ApiExceptionWrapper

    /** Exceptions for when the account has no secondary token.
      *
      * Note that this can also mean that you just don't have access to using this API on the
      * account.
      */
    final case class SecondaryAuthTokenNotFoundOnAccountException()
        extends RuntimeException(
          s"Account has no secondary token to delete (or the API is not enabled on this account)."
        )
        with AuthTokenSecondaryDeleteException

    final case class UnspecifiedError(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to delete secondary auth token."
          ),
          cause.orNull
        )
        with AuthTokenSecondaryDeleteException
  }
}
