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

package com.dixa.twilio.client.general

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.general.UsageTrigger
import com.dixa.twilio.model.iam.TwilioAccount

trait UsageTriggerReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      UsageTriggerReadRequestExecutor.UsageTriggerReadRequest,
      UsageTriggerReadRequestExecutor.UsageTriggerReadException,
      UsageTrigger,
      UsageTriggerReadRequestExecutor.UsageTriggerReadRequest.BuilderStartState
    ] {

  import UsageTriggerReadRequestExecutor._

  override final protected type ApiExceptionWrapper = UsageTriggerReadException.Api

  override final protected type UnspecifiedException = UsageTriggerReadException.Unspecified

  override final protected def createBuilderStartState()
      : UsageTriggerReadRequest.BuilderStartState =
    UsageTriggerReadRequest.Builder.empty
}

object UsageTriggerReadRequestExecutor {

  sealed trait UsageTriggerReadRequest {
    def accountSid: TwilioAccount.Sid
    def recurring: Option[UsageTrigger.Recurring]
    def triggerBy: Option[UsageTrigger.TriggerBy]
    def usageCategory: Option[UsageTrigger.UsageCategory]
  }

  private final case class UsageTriggerReadRequestImpl(
      accountSid: TwilioAccount.Sid,
      recurring: Option[UsageTrigger.Recurring],
      triggerBy: Option[UsageTrigger.TriggerBy],
      usageCategory: Option[UsageTrigger.UsageCategory],
  ) extends UsageTriggerReadRequest

  object UsageTriggerReadRequest {
    sealed trait RequestAttribute
    sealed trait RequestAccountSidAttribute extends RequestAttribute

    type RequestRequiredAttributes = RequestAttribute with RequestAccountSidAttribute

    type BuilderStartState = Builder[RequestAttribute]

    final class Builder[Attributes <: RequestAttribute] private[UsageTriggerReadRequestExecutor] (
        accountSid: Option[TwilioAccount.Sid],
        recurring: Option[UsageTrigger.Recurring],
        triggerBy: Option[UsageTrigger.TriggerBy],
        usageCategory: Option[UsageTrigger.UsageCategory],
    ) {
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[Attributes with RequestAccountSidAttribute] =
        new Builder(
          Some(accountSid),
          recurring,
          triggerBy,
          usageCategory,
        )

      def withRecurring(recurring: UsageTrigger.Recurring): Builder[Attributes] =
        new Builder(
          accountSid,
          Some(recurring),
          triggerBy,
          usageCategory,
        )

      def withTriggerBy(triggerBy: UsageTrigger.TriggerBy): Builder[Attributes] =
        new Builder(
          accountSid,
          recurring,
          Some(triggerBy),
          usageCategory
        )

      def withUsageCategory(usageCategory: UsageTrigger.UsageCategory): Builder[Attributes] =
        new Builder(
          accountSid,
          recurring,
          triggerBy,
          Some(usageCategory)
        )

      def build()(
          implicit ev: Attributes =:= RequestRequiredAttributes
      ): UsageTriggerReadRequest =
        UsageTriggerReadRequestImpl(
          accountSid.get,
          recurring,
          triggerBy,
          usageCategory
        )
    }

    def build(fun: BuilderStartState => UsageTriggerReadRequest): UsageTriggerReadRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new BuilderStartState(None, None, None, None)
    }
  }

  sealed trait UsageTriggerReadException extends RuntimeException
  object UsageTriggerReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with UsageTriggerReadException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read usage triggers"
          ),
          cause.orNull
        )
        with UsageTriggerReadException
    object Unspecified {
      def apply(msg: String)      = new Unspecified(Some(msg), None)
      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
