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

package com.dixa.twilio.client.content

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.content.ContentTemplateWithApproval

/** Read (list) Content templates together with their WhatsApp approval status.
  *
  * @see
  *   https://www.twilio.com/docs/content/content-api-resources#read-multiple-contentandapprovals-resources
  */
trait ContentAndApprovalsReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      ContentAndApprovalsReadRequestExecutor.ContentAndApprovalsReadRequest,
      ContentAndApprovalsReadRequestExecutor.ContentAndApprovalsReadException,
      ContentTemplateWithApproval,
      ContentAndApprovalsReadRequestExecutor.ContentAndApprovalsReadRequest.BuilderStartState
    ] {

  import ContentAndApprovalsReadRequestExecutor._

  override final protected type ApiExceptionWrapper  = ContentAndApprovalsReadException.Api
  override final protected type UnspecifiedException = ContentAndApprovalsReadException.Unspecified

  override protected def createBuilderStartState()
      : ContentAndApprovalsReadRequest.BuilderStartState =
    ContentAndApprovalsReadRequest.Builder.empty
}

object ContentAndApprovalsReadRequestExecutor {

  sealed trait ContentAndApprovalsReadRequest {
    def pageSize: Option[Int]
  }

  private final case class ContentAndApprovalsReadRequestImpl(pageSize: Option[Int])
      extends ContentAndApprovalsReadRequest

  object ContentAndApprovalsReadRequest {

    object PhantomTypes {
      sealed trait RequestAttribute
    }

    type RequestRequiredAttributes = PhantomTypes.RequestAttribute
    type BuilderStartState         = Builder[PhantomTypes.RequestAttribute]

    final class Builder[
        Attributes <: PhantomTypes.RequestAttribute
    ] private[ContentAndApprovalsReadRequest] (
        pageSize: Option[Int]
    ) {

      def withPageSize(pageSize: Int): Builder[Attributes] =
        new Builder(Some(pageSize))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): ContentAndApprovalsReadRequest =
        ContentAndApprovalsReadRequestImpl(pageSize)
    }

    def build(
        fun: BuilderStartState => ContentAndApprovalsReadRequest
    ): ContentAndApprovalsReadRequest =
      fun(Builder.empty)

    object Builder {
      val empty: BuilderStartState = new Builder(None)
    }
  }

  sealed trait ContentAndApprovalsReadException extends RuntimeException

  object ContentAndApprovalsReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ContentAndApprovalsReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse("Unspecified error reading ContentAndApprovals"),
          cause.orNull
        )
        with ContentAndApprovalsReadException

    object Unspecified {
      def apply(msg: String): Unspecified      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable): Unspecified =
        new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
