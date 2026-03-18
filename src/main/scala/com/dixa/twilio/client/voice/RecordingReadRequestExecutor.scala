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

package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.Iso8601DateTime
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{Call, Conference, Recording}

trait RecordingReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      RecordingReadRequestExecutor.RecordingReadRequest,
      RecordingReadRequestExecutor.RecordingReadException,
      Recording,
      RecordingReadRequestExecutor.RecordingReadRequest.BuilderStartState
    ] {

  import RecordingReadRequestExecutor._

  override final protected type ApiExceptionWrapper = RecordingReadException.Api

  override final protected type UnspecifiedException = RecordingReadException.Unspecified

  override protected def createBuilderStartState()
      : RecordingReadRequestExecutor.RecordingReadRequest.BuilderStartState =
    RecordingReadRequestExecutor.RecordingReadRequest.Builder.empty
}

object RecordingReadRequestExecutor {

  sealed trait RecordingReadRequest {
    def accountSid: TwilioAccount.Sid
    def callSid: Option[Call.Sid]
    def conferenceSid: Option[Conference.Sid]
    def dateCreated: Option[Iso8601DateTime]
    def includeSoftDeleted: Option[Boolean]
  }

  private final case class RecordingReadRequestImpl(
      accountSid: TwilioAccount.Sid,
      callSid: Option[Call.Sid],
      conferenceSid: Option[Conference.Sid],
      dateCreated: Option[Iso8601DateTime],
      includeSoftDeleted: Option[Boolean]
  ) extends RecordingReadRequest

  object RecordingReadRequest {
    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute with RequestAccountSidAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[Attributes <: RequestAttribute] private[RecordingReadRequest] (
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

      def withCallSid(callSid: Call.Sid): Builder[Attributes] =
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
      ): RecordingReadRequest =
        RecordingReadRequestImpl(
          accountSid.get,
          callSid,
          conferenceSid,
          dateCreated,
          includingSoftDeleted
        )
    }

    def build(fun: BuilderStartState => RecordingReadRequest): RecordingReadRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new BuilderStartState(None, None, None, None, None)
    }
  }

  sealed trait RecordingReadException extends RuntimeException
  object RecordingReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with RecordingReadException
        with ApiExceptionWrapper

    final case class ResponseParsingFailed(
        rawResponse: String,
        msg: String,
        cause: Option[Throwable]
    ) extends RuntimeException(
          s"$msg - with full raw response: $rawResponse",
          cause.orNull
        )
        with RecordingReadException

    final case class UnspecifiedWithResponseBody(
        responseBody: String,
        msg: Option[String],
        cause: Option[Throwable]
    ) extends RuntimeException(
          s"Unspecified error happened trying to read recordings: $msg - with full raw response: $responseBody",
          cause.orNull
        )
        with RecordingReadException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read recordings"
          ),
          cause.orNull
        )
        with RecordingReadException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
