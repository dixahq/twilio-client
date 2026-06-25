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
import com.dixa.twilio.model.content.ContentTemplate

/** Fetch a Content template by SID.
  *
  * @see
  *   https://www.twilio.com/docs/content/content-api-resources#fetch-a-content-resource
  */
trait ContentFetchRequestExecutor
    extends SingleRequestExecutor[
      ContentFetchRequestExecutor.ContentFetchRequest,
      ContentFetchRequestExecutor.ContentFetchException,
      ContentTemplate,
      ContentFetchRequestExecutor.ContentFetchRequest.BuilderStartState
    ] {

  import ContentFetchRequestExecutor._

  override final protected type ApiExceptionWrapper  = ContentFetchException.Api
  override final protected type UnspecifiedException = ContentFetchException.Unspecified

  override protected def createBuilderStartState(): ContentFetchRequest.BuilderStartState =
    ContentFetchRequest.Builder.empty
}

object ContentFetchRequestExecutor {

  sealed trait ContentFetchRequest {
    def contentSid: ContentTemplate.Sid
  }

  private final case class ContentFetchRequestImpl(contentSid: ContentTemplate.Sid)
      extends ContentFetchRequest

  object ContentFetchRequest {

    object PhantomTypes {
      sealed trait RequestAttribute
      sealed trait ContentSidSet extends RequestAttribute
    }

    type RequestRequiredAttributes =
      PhantomTypes.RequestAttribute with PhantomTypes.ContentSidSet

    type BuilderStartState = Builder[PhantomTypes.RequestAttribute]

    final class Builder[Attributes <: PhantomTypes.RequestAttribute] private[ContentFetchRequest] (
        contentSid: Option[ContentTemplate.Sid]
    ) {

      def withContentSid(
          contentSid: ContentTemplate.Sid
      ): Builder[Attributes with PhantomTypes.ContentSidSet] =
        new Builder(Some(contentSid))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): ContentFetchRequest =
        ContentFetchRequestImpl(contentSid.get)
    }

    def build(fun: BuilderStartState => ContentFetchRequest): ContentFetchRequest =
      fun(Builder.empty)

    object Builder {
      val empty: BuilderStartState = new Builder(None)
    }
  }

  sealed trait ContentFetchException extends RuntimeException

  object ContentFetchException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ContentFetchException
        with ApiExceptionWrapper

    final case class ContentNotFound(sid: ContentTemplate.Sid)
        extends RuntimeException(s"Content template with sid $sid was not found")
        with ContentFetchException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse("Unspecified error fetching Content template"),
          cause.orNull
        )
        with ContentFetchException

    object Unspecified {
      def apply(msg: String): Unspecified      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable): Unspecified =
        new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
