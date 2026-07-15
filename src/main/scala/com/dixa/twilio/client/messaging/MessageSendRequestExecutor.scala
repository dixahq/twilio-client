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
import com.dixa.twilio.client.messaging.MessageSendRequestExecutor.MessageSendException
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging._
import com.dixa.twilio.model.messaging.MessageRecipient
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.callback.CallbackUrl.MessageStatusCallback
import com.dixa.twilio.model.content.ContentTemplate

trait MessageSendRequestExecutor
    extends SingleRequestExecutor[
      MessageSendRequestExecutor.MessageSendRequest,
      MessageSendRequestExecutor.MessageSendException,
      MessageResource,
      MessageSendRequestExecutor.MessageSendRequest.BuilderStartState
    ] {

  override protected final type ApiExceptionWrapper = MessageSendException.Api

  override protected final type UnspecifiedException = MessageSendException.Unspecified

  override protected final def createBuilderStartState()
      : MessageSendRequestExecutor.MessageSendRequest.BuilderStartState =
    MessageSendRequestExecutor.MessageSendRequest.Builder.empty
}

object MessageSendRequestExecutor {

  sealed trait MessageSendRequest {
    def accountSid: TwilioAccount.Sid
    def from: MessageSender
    def to: MessageRecipient
    def body: Option[MessageBody]
    def statusCallback: MessageStatusCallback
    def mediaUrls: Seq[MediaResourceUrl]
    def contentSid: Option[ContentTemplate.Sid]
    def contentVariables: Map[String, String]
  }

  private final case class MessageSendRequestImpl(
      accountSid: TwilioAccount.Sid,
      from: MessageSender,
      to: MessageRecipient,
      body: Option[MessageBody],
      statusCallback: MessageStatusCallback,
      mediaUrls: Seq[MediaResourceUrl],
      contentSid: Option[ContentTemplate.Sid],
      contentVariables: Map[String, String]
  ) extends MessageSendRequest

  object MessageSendRequest {

    object PhantomTypes {
      sealed trait HasBodySet
      sealed trait HasBodySetTrue  extends HasBodySet
      sealed trait HasBodySetFalse extends HasBodySet

      sealed trait HasContentSidSet
      sealed trait HasContentSidSetTrue  extends HasContentSidSet
      sealed trait HasContentSidSetFalse extends HasContentSidSet

      sealed trait HasMediaUrlsSet
      sealed trait HasMediaUrlsSetTrue  extends HasMediaUrlsSet
      sealed trait HasMediaUrlsSetFalse extends HasMediaUrlsSet

      // "Advanced Strategy 2" evidence trait (see doc/client-implementation-doc.md):
      // only the two valid end states get an implicit instance, so build() rejects
      // "neither set" for free. "Both set" can never arise — see the with* guards below.
      sealed trait ExactlyOneOfBodyOrContentSid[B <: HasBodySet, C <: HasContentSidSet]
      object ExactlyOneOfBodyOrContentSid {
        implicit val bodySet: ExactlyOneOfBodyOrContentSid[HasBodySetTrue, HasContentSidSetFalse] =
          new ExactlyOneOfBodyOrContentSid[HasBodySetTrue, HasContentSidSetFalse] {}
        implicit val contentSidSet
            : ExactlyOneOfBodyOrContentSid[HasBodySetFalse, HasContentSidSetTrue] =
          new ExactlyOneOfBodyOrContentSid[HasBodySetFalse, HasContentSidSetTrue] {}
      }
    }

    import PhantomTypes._

    type BuilderStartState = Builder[HasBodySetFalse, HasContentSidSetFalse, HasMediaUrlsSetFalse]

    final class Builder[
        BodySet <: HasBodySet,
        ContentSidSet <: HasContentSidSet,
        MediaUrlsSet <: HasMediaUrlsSet
    ] private[messaging] (
        accountSid: Option[TwilioAccount.Sid],
        from: Option[MessageSender],
        to: Option[MessageRecipient],
        body: Option[MessageBody],
        statusCallback: Option[MessageStatusCallback],
        mediaUrls: Seq[MediaResourceUrl],
        contentSid: Option[ContentTemplate.Sid],
        contentVariables: Map[String, String]
    ) {
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[BodySet, ContentSidSet, MediaUrlsSet] =
        new Builder(
          Some(accountSid),
          from,
          to,
          body,
          statusCallback,
          mediaUrls,
          contentSid,
          contentVariables
        )

      def withFrom(from: MessageSender): Builder[BodySet, ContentSidSet, MediaUrlsSet] =
        new Builder(
          accountSid,
          Some(from),
          to,
          body,
          statusCallback,
          mediaUrls,
          contentSid,
          contentVariables
        )

      def withTo(to: MessageRecipient): Builder[BodySet, ContentSidSet, MediaUrlsSet] =
        new Builder(
          accountSid,
          from,
          Some(to),
          body,
          statusCallback,
          mediaUrls,
          contentSid,
          contentVariables
        )

      def withStatusCallback(
          statusCallback: MessageStatusCallback
      ): Builder[BodySet, ContentSidSet, MediaUrlsSet] =
        new Builder(
          accountSid,
          from,
          to,
          body,
          Some(statusCallback),
          mediaUrls,
          contentSid,
          contentVariables
        )

      def withBody(
          body: MessageBody
      )(
          implicit ev: ContentSidSet =:= HasContentSidSetFalse
      ): Builder[HasBodySetTrue, ContentSidSet, MediaUrlsSet] =
        new Builder(
          accountSid,
          from,
          to,
          Some(body),
          statusCallback,
          mediaUrls,
          contentSid,
          contentVariables
        )

      def withMediaUrls(
          mediaUrls: Seq[MediaResourceUrl]
      )(
          implicit ev: ContentSidSet =:= HasContentSidSetFalse
      ): Builder[BodySet, ContentSidSet, HasMediaUrlsSetTrue] =
        new Builder(
          accountSid,
          from,
          to,
          body,
          statusCallback,
          mediaUrls,
          contentSid,
          contentVariables
        )

      def withContentSid(
          contentSid: ContentTemplate.Sid
      )(
          implicit evBody: BodySet =:= HasBodySetFalse,
          evMedia: MediaUrlsSet =:= HasMediaUrlsSetFalse
      ): Builder[BodySet, HasContentSidSetTrue, MediaUrlsSet] =
        new Builder(
          accountSid,
          from,
          to,
          body,
          statusCallback,
          mediaUrls,
          Some(contentSid),
          contentVariables
        )

      def withContentVariables(
          contentVariables: Map[String, String]
      )(
          implicit ev: ContentSidSet =:= HasContentSidSetTrue
      ): Builder[BodySet, ContentSidSet, MediaUrlsSet] =
        new Builder(
          accountSid,
          from,
          to,
          body,
          statusCallback,
          mediaUrls,
          contentSid,
          contentVariables
        )

      def build()(
          implicit evValid: ExactlyOneOfBodyOrContentSid[BodySet, ContentSidSet]
      ): MessageSendRequest =
        MessageSendRequestImpl(
          accountSid.get,
          from.get,
          to.get,
          body,
          statusCallback.get,
          mediaUrls,
          contentSid,
          contentVariables
        )
    }

    object Builder {
      val empty: BuilderStartState =
        new Builder(None, None, None, None, None, Seq.empty, None, Map.empty)
    }

    def build(fun: BuilderStartState => MessageSendRequest): MessageSendRequest =
      fun(Builder.empty)
  }

  // Most common Bad Request errors: https://support.twilio.com/hc/en-us/articles/223181868-Troubleshooting-Undelivered-Twilio-SMS-Messages
  sealed trait MessageSendException extends RuntimeException
  object MessageSendException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with MessageSendException
        with ApiExceptionWrapper

    final case class ToNumberNotValid()
        extends IllegalStateException(
          "Invalid 'To' Phone Number. More info: https://www.twilio.com/docs/api/errors/21211"
        )
        with MessageSendException
    final case class FromNumberNotValid()
        extends IllegalStateException(
          "Invalid From Number. More info: https://www.twilio.com/docs/api/errors/21212"
        )
        with MessageSendException
    final case class NotMessageCapableNumber()
        extends IllegalStateException(
          "Attempt to use a 'From' number which is not capable of sending SMS messages. More info: https://www.twilio.com/docs/api/errors/21606"
        )
        with MessageSendException
    final case class MessageBodyCharLimitExceeded()
        extends IllegalStateException(
          "Concatenated message body exceeds the maximum 1600 character limit. More info: https://www.twilio.com/docs/api/errors/21617"
        )
        with MessageSendException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to send an sms"
          ),
          cause.orNull
        )
        with MessageSendException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }
}
