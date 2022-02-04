package com.dixa.twilio.client.model.iam

import enumeratum.{Enum, EnumEntry}
import org.scalactic.TypeCheckedTripleEquals._

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
  final case class AuthToken(asString: String) {

    /** Wil always return *** to not accidentially log auth tokens */
    override def toString: String = authTokenSecretValueString
  }

  sealed abstract class Status(private[client] val apiName: String) extends EnumEntry
  object Status extends Enum[Status] {
    override val values: immutable.IndexedSeq[Status] = findValues

    case object Active    extends Status("active")
    case object Suspended extends Status("suspended")
    case object Closed    extends Status("closed")

    private[client] def fromApiName(s: String): TwilioAccount.Status = findValues
      .find(_.apiName === s)
      .getOrElse(throw new IllegalArgumentException(s"$s is not a valiid Twilio account status."))
  }

  private val authTokenSecretValueString = "AuthToken(***)"
}
