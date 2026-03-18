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

package com.dixa.twilio.client.general

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.general.UsageTrigger
import com.dixa.twilio.model.iam.TwilioAccount

trait UsageTriggerCreateRequestExecutor
    extends SingleRequestExecutor[
      UsageTriggerCreateRequestExecutor.UsageTriggerCreateRequest,
      UsageTriggerCreateRequestExecutor.UsageTriggerCreateException,
      UsageTrigger,
      UsageTriggerCreateRequestExecutor.UsageTriggerCreateRequest.BuilderStartState
    ] {

  override protected type ApiExceptionWrapper =
    UsageTriggerCreateRequestExecutor.UsageTriggerCreateException.Api

  override protected type UnspecifiedException =
    UsageTriggerCreateRequestExecutor.UsageTriggerCreateException.Unspecified

  override protected def createBuilderStartState()
      : UsageTriggerCreateRequestExecutor.UsageTriggerCreateRequest.BuilderStartState =
    UsageTriggerCreateRequestExecutor.UsageTriggerCreateRequest.Builder.empty
}

object UsageTriggerCreateRequestExecutor {

  sealed trait UsageTriggerCreateRequest {
    def accountSid: TwilioAccount.Sid
    def callbackUrl: CallbackUrl.UsageTriggerUrl
    def triggerValue: UsageTrigger.TriggerValue
    def usageCategory: UsageTrigger.UsageCategory
    def callbackMethod: Option[HttpMethod]
    def friendlyName: Option[UsageTrigger.FriendlyName]
    def recurring: Option[UsageTrigger.Recurring]
    def triggerBy: Option[UsageTrigger.TriggerBy]

  }

  object UsageTriggerCreateRequest {

    type BuilderStartState = Builder[
      PhantomTypes.AccountSidSetFalse,
      PhantomTypes.CallbackUrlSetFalse,
      PhantomTypes.TriggerValueSetFalse,
      PhantomTypes.UsageCategorySetFalse
    ]

    /** Phantom types used to enforce compile time constraints on the Builder */
    object PhantomTypes {

      sealed trait AccountSidSet
      sealed trait AccountSidSetTrue  extends AccountSidSet
      sealed trait AccountSidSetFalse extends AccountSidSet

      sealed trait CallbackUrlSet
      sealed trait CallbackUrlSetTrue  extends CallbackUrlSet
      sealed trait CallbackUrlSetFalse extends CallbackUrlSet

      sealed trait TriggerValueSet
      sealed trait TriggerValueSetTrue  extends TriggerValueSet
      sealed trait TriggerValueSetFalse extends TriggerValueSet

      sealed trait UsageCategorySet
      sealed trait UsageCategorySetTrue  extends UsageCategorySet
      sealed trait UsageCategorySetFalse extends UsageCategorySet
    }

    final class Builder[
        AccountSidSet <: PhantomTypes.AccountSidSet,
        CallbackUrlSet <: PhantomTypes.CallbackUrlSet,
        TriggerValueSet <: PhantomTypes.TriggerValueSet,
        UsageCategorySet <: PhantomTypes.UsageCategorySet
    ] private[UsageTriggerCreateRequestExecutor] (
        accountSid: Option[TwilioAccount.Sid],
        callbackUrl: Option[CallbackUrl.UsageTriggerUrl],
        triggerValue: Option[UsageTrigger.TriggerValue],
        usageCategory: Option[UsageTrigger.UsageCategory],
        callbackMethod: Option[HttpMethod],
        friendlyName: Option[UsageTrigger.FriendlyName],
        recurring: Option[UsageTrigger.Recurring],
        triggerBy: Option[UsageTrigger.TriggerBy]
    ) {

      private def copy[
          NewAccountSidSet <: PhantomTypes.AccountSidSet,
          NewVoiceUrlSet <: PhantomTypes.CallbackUrlSet,
          NewVoiceFallbackUrlSet <: PhantomTypes.TriggerValueSet,
          NewStatusCallbackSet <: PhantomTypes.UsageCategorySet
      ](
          accountSid: Option[TwilioAccount.Sid] = accountSid,
          callbackUrl: Option[CallbackUrl.UsageTriggerUrl] = callbackUrl,
          triggerValue: Option[UsageTrigger.TriggerValue] = triggerValue,
          usageCategory: Option[UsageTrigger.UsageCategory] = usageCategory,
          callbackMethod: Option[HttpMethod] = callbackMethod,
          friendlyName: Option[UsageTrigger.FriendlyName] = friendlyName,
          recurring: Option[UsageTrigger.Recurring] = recurring,
          triggerBy: Option[UsageTrigger.TriggerBy] = triggerBy
      ): Builder[
        NewAccountSidSet,
        NewVoiceUrlSet,
        NewVoiceFallbackUrlSet,
        NewStatusCallbackSet
      ] =
        new Builder(
          accountSid,
          callbackUrl,
          triggerValue,
          usageCategory,
          callbackMethod,
          friendlyName,
          recurring,
          triggerBy
        )

      private type BuilderWithSameTypes =
        Builder[
          AccountSidSet,
          CallbackUrlSet,
          TriggerValueSet,
          UsageCategorySet
        ]

      /** The SID of the Account that will create the resource. */
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[
        PhantomTypes.AccountSidSetTrue,
        CallbackUrlSet,
        TriggerValueSet,
        UsageCategorySet
      ] =
        copy(accountSid = Some(accountSid))

      /** The URL we should call using callback_method when the trigger fires. */
      def withCallbackUrl(
          callbackUrl: CallbackUrl.UsageTriggerUrl
      ): Builder[
        AccountSidSet,
        PhantomTypes.CallbackUrlSetTrue,
        TriggerValueSet,
        UsageCategorySet
      ] =
        copy(callbackUrl = Some(callbackUrl))

      /** The usage value at which the trigger should fire. For convenience, you can use an offset
        * value such as +30 to specify a trigger_value that is 30 units more than the current usage
        * value.
        */
      def withTriggerValue(
          triggerValue: UsageTrigger.TriggerValue
      ): Builder[
        AccountSidSet,
        CallbackUrlSet,
        PhantomTypes.TriggerValueSetTrue,
        UsageCategorySet
      ] =
        copy(triggerValue = Some(triggerValue))

      /** The usage category that the trigger should watch. Use one of the supported usage
        * categories for this value.
        */
      def withUsageCategory(usageCategory: UsageTrigger.UsageCategory): Builder[
        AccountSidSet,
        CallbackUrlSet,
        TriggerValueSet,
        PhantomTypes.UsageCategorySetTrue
      ] =
        copy(usageCategory = Some(usageCategory))

      /** The HTTP method we should use to call callback_url.
        *
        * Can be: GET or POST and the default is POST.
        */
      def withCallbackMethod(callbackMethod: HttpMethod): BuilderWithSameTypes =
        copy(callbackMethod = Some(callbackMethod))

      /** A descriptive string that you create to describe the new application. It can be up to 64
        * characters long.
        */
      def withFriendlyName(friendlyName: UsageTrigger.FriendlyName): BuilderWithSameTypes =
        copy(friendlyName = Some(friendlyName))

      /** The frequency of a recurring UsageTrigger.
        *
        * Can be: daily, monthly, or yearly for recurring triggers or empty for non-recurring
        * triggers. A trigger will only fire once during each period. Recurring times are in GMT.
        */
      def withRecurring(
          recurring: UsageTrigger.Recurring
      ): BuilderWithSameTypes =
        copy(recurring = Some(recurring))

      /** The field in the UsageRecord resource that should fire the trigger.
        *
        * Can be: count, usage, or price as described in the UsageRecords documentation. The default
        * is usage
        */
      def withTriggerBy(triggerBy: UsageTrigger.TriggerBy): BuilderWithSameTypes =
        copy(triggerBy = Some(triggerBy))

      def build()(
          implicit accountSidSetEv: AccountSidSet =:= PhantomTypes.AccountSidSetTrue,
          callbackUrlSetEv: CallbackUrlSet =:= PhantomTypes.CallbackUrlSetTrue,
          triggerValueSetEv: TriggerValueSet =:= PhantomTypes.TriggerValueSetTrue,
          usageCategorySetEv: UsageCategorySet =:= PhantomTypes.UsageCategorySetTrue
      ): UsageTriggerCreateRequest = RequestImpl(
        accountSid.get,
        callbackUrl.get,
        triggerValue.get,
        usageCategory.get,
        callbackMethod,
        friendlyName,
        recurring,
        triggerBy
      )
    }

    object Builder {
      val empty =
        new BuilderStartState(
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None
        )
    }

    def build(fun: BuilderStartState => UsageTriggerCreateRequest): UsageTriggerCreateRequest =
      fun(Builder.empty)
  }

  private final case class RequestImpl(
      accountSid: TwilioAccount.Sid,
      callbackUrl: CallbackUrl.UsageTriggerUrl,
      triggerValue: UsageTrigger.TriggerValue,
      usageCategory: UsageTrigger.UsageCategory,
      callbackMethod: Option[HttpMethod],
      friendlyName: Option[UsageTrigger.FriendlyName],
      recurring: Option[UsageTrigger.Recurring],
      triggerBy: Option[UsageTrigger.TriggerBy]
  ) extends UsageTriggerCreateRequest

  sealed trait UsageTriggerCreateException extends RuntimeException

  object UsageTriggerCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with UsageTriggerCreateException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to create usage trigger"
          ),
          cause.orNull
        )
        with UsageTriggerCreateException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }

}
