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
import com.dixa.twilio.model.content.{ContentApproval, ContentTemplate}

/** Submit a WhatsApp template approval request.
  *
  * @see
  *   https://www.twilio.com/docs/content/content-api-resources#submit-a-whatsapp-template-approval-request
  */
trait ContentApprovalCreateRequestExecutor
    extends SingleRequestExecutor[
      ContentApprovalCreateRequestExecutor.ContentApprovalCreateRequest,
      ContentApprovalCreateRequestExecutor.ContentApprovalCreateException,
      ContentApproval,
      ContentApprovalCreateRequestExecutor.ContentApprovalCreateRequest.BuilderStartState
    ] {

  import ContentApprovalCreateRequestExecutor._

  override final protected type ApiExceptionWrapper  = ContentApprovalCreateException.Api
  override final protected type UnspecifiedException = ContentApprovalCreateException.Unspecified

  override protected def createBuilderStartState(): ContentApprovalCreateRequest.BuilderStartState =
    ContentApprovalCreateRequest.Builder.empty
}

object ContentApprovalCreateRequestExecutor {

  sealed trait ContentApprovalCreateRequest {
    def contentSid: ContentTemplate.Sid
    def name: String
    def category: ContentApproval.WhatsappCategory
  }

  private final case class ContentApprovalCreateRequestImpl(
      contentSid: ContentTemplate.Sid,
      name: String,
      category: ContentApproval.WhatsappCategory
  ) extends ContentApprovalCreateRequest

  object ContentApprovalCreateRequest {

    object PhantomTypes {
      sealed trait RequestAttribute
      sealed trait ContentSidSet extends RequestAttribute
      sealed trait NameSet       extends RequestAttribute
      sealed trait CategorySet   extends RequestAttribute
    }

    type RequestRequiredAttributes =
      PhantomTypes.RequestAttribute
        with PhantomTypes.ContentSidSet
        with PhantomTypes.NameSet
        with PhantomTypes.CategorySet

    type BuilderStartState = Builder[PhantomTypes.RequestAttribute]

    final class Builder[
        Attributes <: PhantomTypes.RequestAttribute
    ] private[ContentApprovalCreateRequest] (
        contentSid: Option[ContentTemplate.Sid],
        name: Option[String],
        category: Option[ContentApproval.WhatsappCategory]
    ) {

      def withContentSid(
          contentSid: ContentTemplate.Sid
      ): Builder[Attributes with PhantomTypes.ContentSidSet] =
        new Builder(Some(contentSid), name, category)

      def withName(name: String): Builder[Attributes with PhantomTypes.NameSet] = {
        require(
          name.matches("[a-z0-9_]+"),
          s"WhatsApp template name must contain only lowercase letters, digits, and underscores, but got: $name"
        )
        new Builder(contentSid, Some(name), category)
      }

      def withCategory(
          category: ContentApproval.WhatsappCategory
      ): Builder[Attributes with PhantomTypes.CategorySet] =
        new Builder(contentSid, name, Some(category))

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): ContentApprovalCreateRequest =
        ContentApprovalCreateRequestImpl(contentSid.get, name.get, category.get)
    }

    def build(
        fun: BuilderStartState => ContentApprovalCreateRequest
    ): ContentApprovalCreateRequest =
      fun(Builder.empty)

    object Builder {
      val empty: BuilderStartState = new Builder(None, None, None)
    }
  }

  sealed trait ContentApprovalCreateException extends RuntimeException

  object ContentApprovalCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ContentApprovalCreateException
        with ApiExceptionWrapper

    final case class ContentNotFound(sid: ContentTemplate.Sid)
        extends RuntimeException(s"Content template with sid $sid was not found")
        with ContentApprovalCreateException

    case object TemplateTooLong
        extends RuntimeException(
          "Template body cannot exceed 1024 characters for WhatsApp approval"
        )
        with ContentApprovalCreateException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse("Unspecified error submitting WhatsApp approval"),
          cause.orNull
        )
        with ContentApprovalCreateException

    object Unspecified {
      def apply(msg: String): Unspecified      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable): Unspecified =
        new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
