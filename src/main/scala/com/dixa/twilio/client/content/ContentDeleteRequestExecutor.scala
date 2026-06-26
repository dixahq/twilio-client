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
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.FUnit
import com.dixa.twilio.model.content.ContentTemplate

/** Delete a Content template.
  *
  * @see
  *   https://www.twilio.com/docs/content/content-api-resources#delete-a-content-resource
  */
trait ContentDeleteRequestExecutor
    extends SingleRequestExecutor[
      ContentDeleteRequestExecutor.ContentDeleteRequest,
      ContentDeleteRequestExecutor.ContentDeleteException,
      FUnit,
      ContentDeleteRequestExecutor.ContentDeleteRequest.BuilderStartState
    ] {

  import ContentDeleteRequestExecutor._

  override final protected type ApiExceptionWrapper  = ContentDeleteException.Api
  override final protected type UnspecifiedException = ContentDeleteException.Unspecified

  override protected def createBuilderStartState(): ContentDeleteRequest.BuilderStartState =
    ContentDeleteRequest.Builder.empty
}

object ContentDeleteRequestExecutor {

  sealed trait ContentDeleteRequest {
    def contentSid: ContentTemplate.Sid
    def deleteInWaba: Option[Boolean]
  }

  private final case class ContentDeleteRequestImpl(
      contentSid: ContentTemplate.Sid,
      deleteInWaba: Option[Boolean]
  ) extends ContentDeleteRequest

  object ContentDeleteRequest {

    object PhantomTypes {
      sealed trait RequestAttribute
      sealed trait ContentSidSet extends RequestAttribute
    }

    type RequestRequiredAttributes =
      PhantomTypes.RequestAttribute with PhantomTypes.ContentSidSet

    type BuilderStartState = Builder[PhantomTypes.RequestAttribute]

    final class Builder[Attributes <: PhantomTypes.RequestAttribute] private[ContentDeleteRequest] (
        contentSid: Option[ContentTemplate.Sid],
        deleteInWaba: Option[Boolean]
    ) {

      def withContentSid(
          contentSid: ContentTemplate.Sid
      ): Builder[Attributes with PhantomTypes.ContentSidSet] =
        new Builder(Some(contentSid), deleteInWaba)

      def withDeleteInWaba(deleteInWaba: Boolean): Builder[Attributes] =
        new Builder(contentSid, Some(deleteInWaba))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): ContentDeleteRequest =
        ContentDeleteRequestImpl(contentSid.get, deleteInWaba)
    }

    def build(fun: BuilderStartState => ContentDeleteRequest): ContentDeleteRequest =
      fun(Builder.empty)

    object Builder {
      val empty: BuilderStartState = new Builder(None, None)
    }
  }

  sealed trait ContentDeleteException extends RuntimeException

  object ContentDeleteException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ContentDeleteException
        with ApiExceptionWrapper

    final case class ContentNotFound(sid: ContentTemplate.Sid)
        extends RuntimeException(s"Content template with sid $sid was not found")
        with ContentDeleteException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse("Unspecified error deleting Content template"),
          cause.orNull
        )
        with ContentDeleteException

    object Unspecified {
      def apply(msg: String): Unspecified      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable): Unspecified =
        new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
