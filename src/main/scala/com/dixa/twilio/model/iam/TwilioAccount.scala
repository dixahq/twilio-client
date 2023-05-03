package com.dixa.twilio.model.iam

import com.dixa.twilio.model.SidAbstract.Prefix
import com.dixa.twilio.model.{EnumWithTwilioString, SidAbstract, TwilioStringValue}

import java.time.Instant
import scala.collection.immutable

/** Represent a Account or a Subaccount at Twilio
  */
final case class TwilioAccount(
    name: TwilioAccount.Name,
    sid: TwilioAccount.Sid,
    status: TwilioAccount.Status,
    /** Sid of the owning account. In case of a root account, value will be same as sid */
    ownerAccountSid: TwilioAccount.Sid,
    authToken: AuthToken.Primary,
    accountType: TwilioAccount.Type,
    timeCreated: Instant,
    timeUpdated: Instant
) {
  def isActive: Boolean = status == TwilioAccount.Status.Active
}

object TwilioAccount {

  final case class Name(override val toString: String)         extends TwilioStringValue
  final case class Sid private (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject[Sid](Prefix("AC"), new Sid(_))

  sealed abstract class Status(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry
  object Status extends EnumWithTwilioString[Status] {
    override val values: immutable.IndexedSeq[Status] = findValues

    case object Active    extends Status("active")
    case object Suspended extends Status("suspended")
    case object Closed    extends Status("closed")
  }

  sealed abstract class Type(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry
  object Type extends EnumWithTwilioString[Type] {
    override val values: immutable.IndexedSeq[Type] = findValues

    case object Trail extends Type("Trial")
    case object Full  extends Type("Full")
  }
}
