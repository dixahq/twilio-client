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
import com.dixa.twilio.model.content.{ContentTemplate, ContentType}

/** Create a Content template.
  *
  * @see
  *   https://www.twilio.com/docs/content/content-api-resources#create-a-content-resource
  */
trait ContentCreateRequestExecutor
    extends SingleRequestExecutor[
      ContentCreateRequestExecutor.ContentCreateRequest,
      ContentCreateRequestExecutor.ContentCreateException,
      ContentTemplate,
      ContentCreateRequestExecutor.ContentCreateRequest.BuilderStartState
    ] {

  import ContentCreateRequestExecutor._

  override final protected type ApiExceptionWrapper  = ContentCreateException.Api
  override final protected type UnspecifiedException = ContentCreateException.Unspecified

  override protected def createBuilderStartState(): ContentCreateRequest.BuilderStartState =
    ContentCreateRequest.Builder.empty
}

object ContentCreateRequestExecutor {

  sealed trait ContentCreateRequest {
    def friendlyName: String
    def language: String
    def variables: Map[String, String]
    def types: Map[String, ContentType]
  }

  private final case class ContentCreateRequestImpl(
      friendlyName: String,
      language: String,
      variables: Map[String, String],
      types: Map[String, ContentType]
  ) extends ContentCreateRequest

  object ContentCreateRequest {

    object PhantomTypes {
      sealed trait RequestAttribute
      sealed trait FriendlyNameSet extends RequestAttribute
      sealed trait LanguageSet     extends RequestAttribute
      sealed trait TypesSet        extends RequestAttribute
    }

    type RequestRequiredAttributes =
      PhantomTypes.RequestAttribute
        with PhantomTypes.FriendlyNameSet
        with PhantomTypes.LanguageSet
        with PhantomTypes.TypesSet

    type BuilderStartState = Builder[PhantomTypes.RequestAttribute]

    final class Builder[Attributes <: PhantomTypes.RequestAttribute] private[ContentCreateRequest] (
        friendlyName: Option[String],
        language: Option[String],
        variables: Map[String, String],
        types: Option[Map[String, ContentType]]
    ) {

      def withFriendlyName(
          friendlyName: String
      ): Builder[Attributes with PhantomTypes.FriendlyNameSet] =
        new Builder(Some(friendlyName), language, variables, types)

      def withLanguage(language: String): Builder[Attributes with PhantomTypes.LanguageSet] =
        new Builder(friendlyName, Some(language), variables, types)

      def withVariables(variables: Map[String, String]): Builder[Attributes] =
        new Builder(friendlyName, language, variables, types)

      def withTypes(
          types: Map[String, ContentType]
      ): Builder[Attributes with PhantomTypes.TypesSet] =
        new Builder(friendlyName, language, variables, Some(types))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): ContentCreateRequest =
        ContentCreateRequestImpl(friendlyName.get, language.get, variables, types.get)
    }

    def build(fun: BuilderStartState => ContentCreateRequest): ContentCreateRequest =
      fun(Builder.empty)

    object Builder {
      val empty: BuilderStartState = new Builder(None, None, Map.empty, None)
    }
  }

  sealed trait ContentCreateException extends RuntimeException

  object ContentCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ContentCreateException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse("Unspecified error creating Content template"),
          cause.orNull
        )
        with ContentCreateException

    object Unspecified {
      def apply(msg: String): Unspecified      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable): Unspecified =
        new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
