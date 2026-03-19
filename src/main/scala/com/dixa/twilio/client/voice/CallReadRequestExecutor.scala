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
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.model.voice.Call

trait CallReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      CallReadRequestExecutor.CallReadRequest,
      CallReadRequestExecutor.CallReadException,
      Call,
      CallReadRequestExecutor.CallReadRequest.BuilderStartState
    ] {

  import CallReadRequestExecutor._

  override final protected type ApiExceptionWrapper = CallReadException.Api

  override final protected type UnspecifiedException = CallReadException.Unspecified

  override protected def createBuilderStartState()
      : CallReadRequestExecutor.CallReadRequest.BuilderStartState =
    CallReadRequestExecutor.CallReadRequest.Builder.empty
}

object CallReadRequestExecutor {

  sealed trait CallReadRequest {
    def accountSid: TwilioAccount.Sid
    def to: Option[PhoneNumberE164]
    def from: Option[PhoneNumberE164]
    def parentCallSid: Option[Call.Sid]
    def status: Option[Call.Status]
    def startTimeAfter: Option[Iso8601DateTime.After]
    def startTimeBefore: Option[Iso8601DateTime.Before]
    def endTimeAfter: Option[Iso8601DateTime.After]
    def endTimeBefore: Option[Iso8601DateTime.Before]
  }

  private final case class CallReadRequestImpl(
      accountSid: TwilioAccount.Sid,
      to: Option[PhoneNumberE164],
      from: Option[PhoneNumberE164],
      parentCallSid: Option[Call.Sid],
      status: Option[Call.Status],
      startTimeAfter: Option[Iso8601DateTime.After],
      startTimeBefore: Option[Iso8601DateTime.Before],
      endTimeAfter: Option[Iso8601DateTime.After],
      endTimeBefore: Option[Iso8601DateTime.Before]
  ) extends CallReadRequest

  object CallReadRequest {
    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute with RequestAccountSidAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[Attributes <: RequestAttribute] private[CallReadRequest] (
        accountSid: Option[TwilioAccount.Sid],
        to: Option[PhoneNumberE164],
        from: Option[PhoneNumberE164],
        parentCallSid: Option[Call.Sid],
        status: Option[Call.Status],
        startTimeAfter: Option[Iso8601DateTime.After],
        startTimeBefore: Option[Iso8601DateTime.Before],
        endTimeAfter: Option[Iso8601DateTime.After],
        endTimeBefore: Option[Iso8601DateTime.Before]
    ) {
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(
          Some(accountSid),
          to,
          from,
          parentCallSid,
          status,
          startTimeAfter,
          startTimeBefore,
          endTimeAfter,
          endTimeBefore
        )

      def withTo(to: PhoneNumberE164): Builder[Attributes] =
        new Builder(
          accountSid,
          Some(to),
          from,
          parentCallSid,
          status,
          startTimeAfter,
          startTimeBefore,
          endTimeAfter,
          endTimeBefore
        )

      def withFrom(from: PhoneNumberE164): Builder[Attributes] =
        new Builder(
          accountSid,
          to,
          Some(from),
          parentCallSid,
          status,
          startTimeAfter,
          startTimeBefore,
          endTimeAfter,
          endTimeBefore
        )

      def withParentCallSid(parentCallSid: Call.Sid): Builder[Attributes] =
        new Builder(
          accountSid,
          to,
          from,
          Some(parentCallSid),
          status,
          startTimeAfter,
          startTimeBefore,
          endTimeAfter,
          endTimeBefore
        )

      def withStatus(status: Call.Status): Builder[Attributes] =
        new Builder(
          accountSid,
          to,
          from,
          parentCallSid,
          Some(status),
          startTimeAfter,
          startTimeBefore,
          endTimeAfter,
          endTimeBefore
        )

      def withStartTimeAfter(startTimeAfter: Iso8601DateTime.After): Builder[Attributes] =
        new Builder(
          accountSid,
          to,
          from,
          parentCallSid,
          status,
          Some(startTimeAfter),
          startTimeBefore,
          endTimeAfter,
          endTimeBefore
        )

      def withStartTimeBefore(startTimeBefore: Iso8601DateTime.Before): Builder[Attributes] =
        new Builder(
          accountSid,
          to,
          from,
          parentCallSid,
          status,
          startTimeAfter,
          Some(startTimeBefore),
          endTimeAfter,
          endTimeBefore
        )

      def withEndTimeAfter(endTimeAfter: Iso8601DateTime.After): Builder[Attributes] =
        new Builder(
          accountSid,
          to,
          from,
          parentCallSid,
          status,
          startTimeAfter,
          startTimeBefore,
          Some(endTimeAfter),
          endTimeBefore
        )

      def withEndTimeBefore(endTimeBefore: Iso8601DateTime.Before): Builder[Attributes] =
        new Builder(
          accountSid,
          to,
          from,
          parentCallSid,
          status,
          startTimeAfter,
          startTimeBefore,
          endTimeAfter,
          Some(endTimeBefore)
        )

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): CallReadRequest =
        CallReadRequestImpl(
          accountSid.get,
          to,
          from,
          parentCallSid,
          status,
          startTimeAfter,
          startTimeBefore,
          endTimeAfter,
          endTimeBefore
        )
    }

    def build(fun: BuilderStartState => CallReadRequest): CallReadRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState =
        new BuilderStartState(None, None, None, None, None, None, None, None, None)
    }
  }

  sealed trait CallReadException extends RuntimeException
  object CallReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with CallReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read calls"
          ),
          cause.orNull
        )
        with CallReadException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
