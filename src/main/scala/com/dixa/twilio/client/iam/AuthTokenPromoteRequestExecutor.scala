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

package com.dixa.twilio.client.iam

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.AuthToken

trait AuthTokenPromoteRequestExecutor
    extends SingleRequestExecutor[
      AuthTokenPromoteRequestExecutor.AuthTokenPromoteRequest,
      AuthTokenPromoteRequestExecutor.AuthTokenPromoteException,
      AuthToken.AuthTokenAndMetaData[AuthToken.Primary],
      AuthTokenPromoteRequestExecutor.AuthTokenPromoteRequest.Builder
    ] {

  import AuthTokenPromoteRequestExecutor._

  override final protected type ApiExceptionWrapper = AuthTokenPromoteException.Api

  override final protected type UnspecifiedException =
    AuthTokenPromoteException.UnspecifiedError

  override final protected def createBuilderStartState(): AuthTokenPromoteRequest.Builder =
    AuthTokenPromoteRequest.Builder.empty
}

object AuthTokenPromoteRequestExecutor {

  final case class AuthTokenPromoteRequest()
  object AuthTokenPromoteRequest {
    type BuilderStartState = Builder

    final class Builder private[iam] () {
      def build(): AuthTokenPromoteRequest = AuthTokenPromoteRequest()
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState()
    }

    def build(fun: BuilderStartState => AuthTokenPromoteRequest): AuthTokenPromoteRequest =
      fun(Builder.empty)
  }

  sealed trait AuthTokenPromoteException extends RuntimeException
  object AuthTokenPromoteException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with AuthTokenPromoteException
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
        with AuthTokenPromoteException

    final case class UnspecifiedError(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to promote auth token."
          ),
          cause.orNull
        )
        with AuthTokenPromoteException
  }
}
