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

  final case class CurrentValue(override val toString: String) extends TwilioStringValue

  // TODO PR: Make more safe
  final case class TriggerValue(override val toString: String) extends TwilioStringValue
  final case class FriendlyName private (override val toString: String)
      extends ConstrainedString
      with TwilioStringValue
  object FriendlyName
      extends ConstrainedString.ConstrainedStringCompanionObject[FriendlyName](maxLength = 64) {
    override protected def constructInstance(wrapped: String): FriendlyName = new FriendlyName(
      wrapped
    )
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

  }
}
