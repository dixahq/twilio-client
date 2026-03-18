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

package com.dixa.twilio.model.general

import com.dixa.twilio.model.SidAbstract.Prefix
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.{
  ConstrainedString,
  EnumWithTwilioString,
  HttpMethod,
  SidAbstract,
  TwilioStringValue
}

import java.time.Instant
import scala.collection.immutable

/** A UsageTrigger is a webhook that notifies your application of usage thresholds.
  */
final case class UsageTrigger(
    accountSid: TwilioAccount.Sid,
    callBackMethod: HttpMethod,
    callbackUrl: CallbackUrl.UsageTriggerUrl,
    currentValue: UsageTrigger.CurrentValue,
    dateCreated: Instant,
    dateFired: Option[Instant],
    dateUpdated: Instant,
    friendlyName: UsageTrigger.FriendlyName,
    recurring: Option[UsageTrigger.Recurring],
    sid: UsageTrigger.Sid,
    triggerBy: UsageTrigger.TriggerBy,
    triggerValue: UsageTrigger.TriggerValue,
    usageCategory: UsageTrigger.UsageCategory
)

object UsageTrigger {

  final case class CurrentValue private (override val toString: String)
      extends ConstrainedString
      with TwilioStringValue {

    /** Return instance as BigDecimal value.
      *
      * This operation is safe, as value can only represent valid decimal values.
      */
    def toBigDecimal: BigDecimal = BigDecimal(toString)
  }
  object CurrentValue extends ConstrainedString.ConstrainedStringCompanionObject[CurrentValue] {
    override protected def constructInstance(wrapped: String): CurrentValue = new CurrentValue(
      wrapped
    )

    override protected val decimalOnly: Boolean = true
  }

  final case class TriggerValue private (override val toString: String)
      extends ConstrainedString
      with TwilioStringValue {

    /** Return instance as BigDecimal value.
      *
      * This operation is safe, as value can only represent valid decimal values.
      */
    def toBigDecimal: BigDecimal = BigDecimal(toString)
  }
  object TriggerValue extends ConstrainedString.ConstrainedStringCompanionObject[TriggerValue] {
    override protected def constructInstance(wrapped: String): TriggerValue = new TriggerValue(
      wrapped
    )

    override protected val decimalOnly: Boolean = true
  }

  final case class FriendlyName private (override val toString: String)
      extends ConstrainedString
      with TwilioStringValue
  object FriendlyName extends ConstrainedString.ConstrainedStringCompanionObject[FriendlyName] {
    override protected def constructInstance(wrapped: String): FriendlyName = new FriendlyName(
      wrapped
    )

    override protected val maxLength: Option[Int] = Some(64)
  }

  final case class Sid private (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject[Sid](List(Prefix("UT")), new Sid(_))

  sealed abstract class Recurring(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry
  object Recurring extends EnumWithTwilioString[Recurring] {
    override val values: immutable.IndexedSeq[Recurring] = findValues

    case object Daily   extends Recurring("daily")
    case object Monthly extends Recurring("monthly")
    case object Yearly  extends Recurring("yearly")
  }

  sealed abstract class TriggerBy(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry
  object TriggerBy extends EnumWithTwilioString[TriggerBy] {
    override val values: immutable.IndexedSeq[TriggerBy] = findValues

    case object Count extends TriggerBy("count")
    case object Usage extends TriggerBy("usage")
    case object Price extends TriggerBy("price")
  }

  sealed abstract class UsageCategory(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object UsageCategory extends EnumWithTwilioString[UsageCategory] {
    override val values: immutable.IndexedSeq[UsageCategory] = findValues

    case object Calls          extends UsageCategory("calls")
    case object Sms            extends UsageCategory("sms")
    case object PfaxMinutes    extends UsageCategory("pfax-minutes")
    case object PfaxPages      extends UsageCategory("pfax-pages")
    case object Phonenumbers   extends UsageCategory("phonenumbers")
    case object Secordings     extends UsageCategory("recordings")
    case object Transcriptions extends UsageCategory("transcriptions")
    case object Pv             extends UsageCategory("pv")
    case object Totalprice     extends UsageCategory("totalprice")

    // The above is taken from the official documentation: https://www.twilio.com/docs/usage/api/usage-record#usage-categories
    // However there seem to be more valid values, and these will be added below as we find them.

    case object CallsOutbound extends UsageCategory("calls-outbound")
  }
}
