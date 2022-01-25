package com.dixa.twilio.client.model

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

/** Represent a Account or a Subaccount at Twilio
  */
final case class TwilioAccount(
    name: TwilioAccount.Name,
    sid: TwilioAccount.Sid,
    status: TwilioAccount.Status
)

object TwilioAccount {

  final case class Name(override val toString: String)
  final case class Sid(override val toString: String)

  sealed trait Status extends EnumEntry
  object Status extends Enum[Status] {
    override def values: immutable.IndexedSeq[Status] = findValues

    case object Active    extends Status
    case object Suspended extends Status
    case object Closed    extends Status

    private[client] def fromTwilioStringStatus(s: String): TwilioAccount.Status = s match {
      case "active"    => TwilioAccount.Status.Active
      case "closed"    => TwilioAccount.Status.Closed
      case "suspended" => TwilioAccount.Status.Suspended
    }
  }
}
