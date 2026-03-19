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

package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.Iso8601DateTime
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{Call, Conference, Recording}

trait CallRecordingReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      CallRecordingReadRequestExecutor.CallRecordingReadRequest,
      CallRecordingReadRequestExecutor.CallRecordingReadException,
      Recording,
      CallRecordingReadRequestExecutor.CallRecordingReadRequest.BuilderStartState
    ] {

  import CallRecordingReadRequestExecutor._

  override final protected type ApiExceptionWrapper = CallRecordingReadException.Api

  override final protected type UnspecifiedException = CallRecordingReadException.Unspecified

  override protected def createBuilderStartState()
      : CallRecordingReadRequestExecutor.CallRecordingReadRequest.BuilderStartState =
    CallRecordingReadRequestExecutor.CallRecordingReadRequest.Builder.empty
}

object CallRecordingReadRequestExecutor {

  sealed trait CallRecordingReadRequest {
    def accountSid: TwilioAccount.Sid
    def callSid: Call.Sid
    def conferenceSid: Option[Conference.Sid]
    def dateCreated: Option[Iso8601DateTime]
    def includeSoftDeleted: Option[Boolean]
  }

  private final case class CallRecordingReadRequestImpl(
      accountSid: TwilioAccount.Sid,
      callSid: Call.Sid,
      conferenceSid: Option[Conference.Sid],
      dateCreated: Option[Iso8601DateTime],
      includeSoftDeleted: Option[Boolean]
  ) extends CallRecordingReadRequest

  object CallRecordingReadRequest {
    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute extends RequestAttribute
    sealed trait RequestCallSidAttribute    extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute
      with RequestAccountSidAttribute
      with RequestCallSidAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[Attributes <: RequestAttribute] private[CallRecordingReadRequest] (
        accountSid: Option[TwilioAccount.Sid],
        callSid: Option[Call.Sid],
        conferenceSid: Option[Conference.Sid],
        dateCreated: Option[Iso8601DateTime],
        includingSoftDeleted: Option[Boolean]
    ) {
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(
          Some(accountSid),
          callSid,
          conferenceSid,
          dateCreated,
          includingSoftDeleted
        )

      def withCallSid(callSid: Call.Sid): Builder[Attributes with RequestCallSidAttribute] =
        new Builder(
          accountSid,
          Some(callSid),
          conferenceSid,
          dateCreated,
          includingSoftDeleted
        )

      def withConferenceSid(conferenceSid: Conference.Sid): Builder[Attributes] =
        new Builder(
          accountSid,
          callSid,
          Some(conferenceSid),
          dateCreated,
          includingSoftDeleted
        )

      def withDateCreated(dateCreated: Iso8601DateTime): Builder[Attributes] =
        new Builder(
          accountSid,
          callSid,
          conferenceSid,
          Some(dateCreated),
          includingSoftDeleted
        )

      def withIncludingSoftDeleted(includingSoftDeleted: Boolean): Builder[Attributes] =
        new Builder(
          accountSid,
          callSid,
          conferenceSid,
          dateCreated,
          Some(includingSoftDeleted)
        )

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): CallRecordingReadRequest =
        CallRecordingReadRequestImpl(
          accountSid.get,
          callSid.get,
          conferenceSid,
          dateCreated,
          includingSoftDeleted
        )
    }

    def build(fun: BuilderStartState => CallRecordingReadRequest): CallRecordingReadRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new BuilderStartState(None, None, None, None, None)
    }
  }

  sealed trait CallRecordingReadException extends RuntimeException
  object CallRecordingReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with CallRecordingReadException
        with ApiExceptionWrapper

    final case class ResponseParsingFailed(
        rawResponse: String,
        msg: String,
        cause: Option[Throwable]
    ) extends RuntimeException(
          s"$msg - with full raw response: $rawResponse",
          cause.orNull
        )
        with CallRecordingReadException

    final case class UnspecifiedWithResponseBody(
        responseBody: String,
        msg: Option[String],
        cause: Option[Throwable]
    ) extends RuntimeException(
          s"Unspecified error happened trying to read recordings: $msg - with full raw response: $responseBody",
          cause.orNull
        )
        with CallRecordingReadException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read recordings"
          ),
          cause.orNull
        )
        with CallRecordingReadException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
