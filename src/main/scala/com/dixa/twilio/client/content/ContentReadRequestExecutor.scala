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
import com.dixa.twilio.model.content.ContentTemplate

/** Read (list) Content templates with automatic pagination.
  *
  * @see
  *   https://www.twilio.com/docs/content/content-api-resources#read-multiple-content-resources
  */
trait ContentReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      ContentReadRequestExecutor.ContentReadRequest,
      ContentReadRequestExecutor.ContentReadException,
      ContentTemplate,
      ContentReadRequestExecutor.ContentReadRequest.BuilderStartState
    ] {

  import ContentReadRequestExecutor._

  override final protected type ApiExceptionWrapper  = ContentReadException.Api
  override final protected type UnspecifiedException = ContentReadException.Unspecified

  override protected def createBuilderStartState(): ContentReadRequest.BuilderStartState =
    ContentReadRequest.Builder.empty
}

object ContentReadRequestExecutor {

  sealed trait ContentReadRequest {
    def pageSize: Option[Int]
  }

  private final case class ContentReadRequestImpl(pageSize: Option[Int]) extends ContentReadRequest

  object ContentReadRequest {

    object PhantomTypes {
      sealed trait RequestAttribute
    }

    type RequestRequiredAttributes = PhantomTypes.RequestAttribute
    type BuilderStartState         = Builder[PhantomTypes.RequestAttribute]

    final class Builder[Attributes <: PhantomTypes.RequestAttribute] private[ContentReadRequest] (
        pageSize: Option[Int]
    ) {

      def withPageSize(pageSize: Int): Builder[Attributes] =
        new Builder(Some(pageSize))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): ContentReadRequest =
        ContentReadRequestImpl(pageSize)
    }

    def build(fun: BuilderStartState => ContentReadRequest): ContentReadRequest =
      fun(Builder.empty)

    object Builder {
      val empty: BuilderStartState = new Builder(None)
    }
  }

  sealed trait ContentReadException extends RuntimeException

  object ContentReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ContentReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse("Unspecified error reading Content templates"),
          cause.orNull
        )
        with ContentReadException

    object Unspecified {
      def apply(msg: String): Unspecified      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable): Unspecified =
        new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
