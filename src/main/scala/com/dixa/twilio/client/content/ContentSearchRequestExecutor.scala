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

import java.time.Instant

/** Search Content templates using the v2 endpoint with optional filters.
  *
  * @see
  *   https://www.twilio.com/docs/content/content-api-resources#template-search-v2
  */
trait ContentSearchRequestExecutor
    extends MultipleResponseRequestExecutor[
      ContentSearchRequestExecutor.ContentSearchRequest,
      ContentSearchRequestExecutor.ContentSearchException,
      ContentTemplate,
      ContentSearchRequestExecutor.ContentSearchRequest.BuilderStartState
    ] {

  import ContentSearchRequestExecutor._

  override final protected type ApiExceptionWrapper  = ContentSearchException.Api
  override final protected type UnspecifiedException = ContentSearchException.Unspecified

  override protected def createBuilderStartState(): ContentSearchRequest.BuilderStartState =
    ContentSearchRequest.Builder.empty
}

object ContentSearchRequestExecutor {

  /** Filter for channel-specific approval status, e.g. `whatsapp:approved`. */
  final case class ChannelEligibility(channel: String, templateStatus: String)

  sealed trait ContentSearchRequest {
    def languages: List[String]
    def contentTypes: List[String]
    def channelEligibilities: List[ChannelEligibility]
    def content: Option[String]
    def contentName: Option[String]
    def dateCreatedBefore: Option[Instant]
    def dateCreatedAfter: Option[Instant]
    def pageSize: Option[Int]
  }

  private final case class ContentSearchRequestImpl(
      languages: List[String],
      contentTypes: List[String],
      channelEligibilities: List[ChannelEligibility],
      content: Option[String],
      contentName: Option[String],
      dateCreatedBefore: Option[Instant],
      dateCreatedAfter: Option[Instant],
      pageSize: Option[Int]
  ) extends ContentSearchRequest

  object ContentSearchRequest {

    object PhantomTypes {
      sealed trait RequestAttribute
    }

    type RequestRequiredAttributes = PhantomTypes.RequestAttribute
    type BuilderStartState         = Builder[PhantomTypes.RequestAttribute]

    final class Builder[Attributes <: PhantomTypes.RequestAttribute] private[ContentSearchRequest] (
        languages: List[String],
        contentTypes: List[String],
        channelEligibilities: List[ChannelEligibility],
        content: Option[String],
        contentName: Option[String],
        dateCreatedBefore: Option[Instant],
        dateCreatedAfter: Option[Instant],
        pageSize: Option[Int]
    ) {

      def withLanguage(language: String): Builder[Attributes] =
        new Builder(
          languages :+ language,
          contentTypes,
          channelEligibilities,
          content,
          contentName,
          dateCreatedBefore,
          dateCreatedAfter,
          pageSize
        )

      def withContentType(contentType: String): Builder[Attributes] =
        new Builder(
          languages,
          contentTypes :+ contentType,
          channelEligibilities,
          content,
          contentName,
          dateCreatedBefore,
          dateCreatedAfter,
          pageSize
        )

      def withChannelEligibility(channelEligibility: ChannelEligibility): Builder[Attributes] =
        new Builder(
          languages,
          contentTypes,
          channelEligibilities :+ channelEligibility,
          content,
          contentName,
          dateCreatedBefore,
          dateCreatedAfter,
          pageSize
        )

      def withContent(content: String): Builder[Attributes] =
        new Builder(
          languages,
          contentTypes,
          channelEligibilities,
          Some(content),
          contentName,
          dateCreatedBefore,
          dateCreatedAfter,
          pageSize
        )

      def withContentName(contentName: String): Builder[Attributes] =
        new Builder(
          languages,
          contentTypes,
          channelEligibilities,
          content,
          Some(contentName),
          dateCreatedBefore,
          dateCreatedAfter,
          pageSize
        )

      def withDateCreatedBefore(dateCreatedBefore: Instant): Builder[Attributes] =
        new Builder(
          languages,
          contentTypes,
          channelEligibilities,
          content,
          contentName,
          Some(dateCreatedBefore),
          dateCreatedAfter,
          pageSize
        )

      def withDateCreatedAfter(dateCreatedAfter: Instant): Builder[Attributes] =
        new Builder(
          languages,
          contentTypes,
          channelEligibilities,
          content,
          contentName,
          dateCreatedBefore,
          Some(dateCreatedAfter),
          pageSize
        )

      def withPageSize(pageSize: Int): Builder[Attributes] =
        new Builder(
          languages,
          contentTypes,
          channelEligibilities,
          content,
          contentName,
          dateCreatedBefore,
          dateCreatedAfter,
          Some(pageSize)
        )

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): ContentSearchRequest =
        ContentSearchRequestImpl(
          languages,
          contentTypes,
          channelEligibilities,
          content,
          contentName,
          dateCreatedBefore,
          dateCreatedAfter,
          pageSize
        )
    }

    def build(fun: BuilderStartState => ContentSearchRequest): ContentSearchRequest =
      fun(Builder.empty)

    object Builder {
      val empty: BuilderStartState =
        new Builder(Nil, Nil, Nil, None, None, None, None, None)
    }
  }

  sealed trait ContentSearchException extends RuntimeException

  object ContentSearchException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ContentSearchException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse("Unspecified error searching Content templates"),
          cause.orNull
        )
        with ContentSearchException

    object Unspecified {
      def apply(msg: String): Unspecified      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable): Unspecified =
        new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
