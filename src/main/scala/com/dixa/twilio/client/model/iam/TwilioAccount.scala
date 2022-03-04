package com.dixa.twilio.client.model.iam

import enumeratum.{Enum, EnumEntry}
import org.scalactic.TypeCheckedTripleEquals._

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
    authToken: TwilioAccount.AuthToken,
    accountType: TwilioAccount.Type,
    timeCreated: Instant,
    timeUpdated: Instant
)

object TwilioAccount {

  final case class Name(override val toString: String)
  final case class Sid(override val toString: String)
  final case class AuthToken(asString: String) {

    /** Will always return AuthToken(***) to not accidentially log auth tokens */
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
      .getOrElse(throw new IllegalArgumentException(s"$s is not a valid Twilio account status."))
  }

  sealed abstract class Type(val twilioApiName: String) extends EnumEntry
  object Type extends Enum[Type] {
    override def values: immutable.IndexedSeq[Type] = findValues

    case object Trail extends Type("Trial")
    case object Full  extends Type("Full")

    def fromTwilioApiName(s: String): TwilioAccount.Type = findValues
      .find(_.twilioApiName === s)
      .getOrElse(throw new IllegalArgumentException(s"$s is not a valid TwilioAccount.Type"))
  }

  private val authTokenSecretValueString = "AuthToken(***)"
}
