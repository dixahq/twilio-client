package com.dixa.twilio.model.iam

import com.dixa.twilio.model.EnumWithApiName
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
) {
  def isActive: Boolean = status === TwilioAccount.Status.Active
}

object TwilioAccount {

  final case class Name(override val toString: String)
  final case class Sid(override val toString: String)
  final case class AuthToken(asString: String) {

    /** Will always return AuthToken(***) to not accidentially log auth tokens */
    override def toString: String = authTokenSecretValueString
  }

  sealed abstract class Status(val apiName: String) extends EnumWithApiName.EnumEntry
  object Status extends EnumWithApiName[Status] {
    override val values: immutable.IndexedSeq[Status] = findValues

    case object Active    extends Status("active")
    case object Suspended extends Status("suspended")
    case object Closed    extends Status("closed")
  }

  sealed abstract class Type(val apiName: String) extends EnumWithApiName.EnumEntry
  object Type extends EnumWithApiName[Type] {
    override val values: immutable.IndexedSeq[Type] = findValues

    case object Trail extends Type("Trial")
    case object Full  extends Type("Full")
  }

  private val authTokenSecretValueString = "AuthToken(***)"
}
