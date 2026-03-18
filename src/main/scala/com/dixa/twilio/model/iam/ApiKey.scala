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

package com.dixa.twilio.model.iam

import com.dixa.twilio.model.{EnumWithTwilioString, TwilioStringValue}
import java.time.Instant

/** Represent a Twilio Standard API key, as returned when the key is created.
  *
  * The [[ApiKey.Secret]] is only returned at creation time and cannot be retrieved afterwards.
  * Store it securely as soon as it is received.
  *
  * @see
  *   [[ApiKeyPolicy]]
  */
sealed trait ApiKey {
  def sid: ApiKey.Sid
  def secretOpt: Option[ApiKey.Secret]
  def friendlyName: ApiKey.FriendlyName
  def flagsOpt: Option[Set[ApiKey.Flag]]
  def policyAllowOpt: Option[Set[ApiKeyPolicy]]
  def dateCreated: Instant
  def dateUpdated: Instant

  def withFlags(flags: Set[ApiKey.Flag]): ApiKey with ApiKey.HasFlags
  def withSecret(secret: ApiKey.Secret): ApiKey with ApiKey.HasSecret
  def withPolicyAllow(policyAllow: Set[ApiKeyPolicy]): ApiKey with ApiKey.HasPolicyAllow

  override def equals(obj: Any): Boolean = obj match {
    case other: ApiKey =>
      sid == other.sid &&
      secretOpt == other.secretOpt &&
      friendlyName == other.friendlyName &&
      flagsOpt == other.flagsOpt &&
      policyAllowOpt == other.policyAllowOpt &&
      dateCreated == other.dateCreated &&
      dateUpdated == other.dateUpdated
    case _ => false
  }

  override def hashCode(): Int =
    (
      sid,
      secretOpt,
      friendlyName,
      flagsOpt,
      policyAllowOpt,
      dateCreated,
      dateUpdated
    ).hashCode()

  override def toString: String =
    s"ApiKey(sid=$sid, secretOpt=$secretOpt, friendlyName=$friendlyName, flagsOpt=$flagsOpt, policyAllowOpt=$policyAllowOpt, dateCreated=$dateCreated, dateUpdated=$dateUpdated)"
}

object ApiKey {

  sealed trait HasFlags { self: ApiKey =>
    def flags: Set[ApiKey.Flag]
    def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlags
    def withSecret(secret: ApiKey.Secret): ApiKey with HasFlagsAndSecret
    def withPolicyAllow(policyAllow: Set[ApiKeyPolicy]): ApiKey with HasFlagsAndPolicyAllow
  }

  sealed trait HasSecret { self: ApiKey =>
    def secret: ApiKey.Secret
    def withSecret(secret: ApiKey.Secret): ApiKey with HasSecret
    def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndSecret
    def withPolicyAllow(policyAllow: Set[ApiKeyPolicy]): ApiKey with HasSecretAndPolicyAllow
  }

  sealed trait HasPolicyAllow { self: ApiKey =>
    def policyAllow: Set[ApiKeyPolicy]
    def withPolicyAllow(policyAllow: Set[ApiKeyPolicy]): ApiKey with HasPolicyAllow
    def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndPolicyAllow
    def withSecret(secret: ApiKey.Secret): ApiKey with HasSecretAndPolicyAllow
  }

  sealed trait HasFlagsAndSecret extends HasFlags with HasSecret { self: ApiKey =>
    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndSecret
    override def withSecret(secret: ApiKey.Secret): ApiKey with HasFlagsAndSecret
    override def withPolicyAllow(policyAllow: Set[ApiKeyPolicy]): ApiKey with HasAll
  }

  sealed trait HasFlagsAndPolicyAllow extends HasFlags with HasPolicyAllow { self: ApiKey =>
    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndPolicyAllow
    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasFlagsAndPolicyAllow
    override def withSecret(secret: ApiKey.Secret): ApiKey with HasAll
  }

  sealed trait HasSecretAndPolicyAllow extends HasSecret with HasPolicyAllow { self: ApiKey =>
    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecretAndPolicyAllow
    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasSecretAndPolicyAllow
    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasAll
  }

  sealed trait HasAll
      extends HasFlagsAndSecret
      with HasFlagsAndPolicyAllow
      with HasSecretAndPolicyAllow {
    self: ApiKey =>
    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasAll
    override def withSecret(secret: ApiKey.Secret): ApiKey with HasAll
    override def withPolicyAllow(policyAllow: Set[ApiKeyPolicy]): ApiKey with HasAll
  }

  private final class ApiKeyBase(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val dateCreated: Instant,
      val dateUpdated: Instant
  ) extends ApiKey {
    override val secretOpt: Option[ApiKey.Secret]          = None
    override val flagsOpt: Option[Set[ApiKey.Flag]]        = None
    override val policyAllowOpt: Option[Set[ApiKeyPolicy]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlags =
      new ApiKeyWithFlags(sid, friendlyName, flags, dateCreated, dateUpdated)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecret =
      new ApiKeyWithSecret(sid, friendlyName, secret, dateCreated, dateUpdated)

    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasPolicyAllow =
      new ApiKeyWithPolicyAllow(sid, friendlyName, policyAllow, dateCreated, dateUpdated)
  }

  private final class ApiKeyWithSecret(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val secret: ApiKey.Secret,
      val dateCreated: Instant,
      val dateUpdated: Instant
  ) extends ApiKey
      with HasSecret {
    override val secretOpt: Option[ApiKey.Secret]          = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]]        = None
    override val policyAllowOpt: Option[Set[ApiKeyPolicy]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags, dateCreated, dateUpdated)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecret =
      new ApiKeyWithSecret(sid, friendlyName, secret, dateCreated, dateUpdated)

    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasSecretAndPolicyAllow =
      new ApiKeyWithSecretAndPolicyAllow(
        sid,
        friendlyName,
        secret,
        policyAllow,
        dateCreated,
        dateUpdated
      )
  }

  private final class ApiKeyWithFlags(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val flags: Set[ApiKey.Flag],
      val dateCreated: Instant,
      val dateUpdated: Instant
  ) extends ApiKey
      with HasFlags {
    override val secretOpt: Option[ApiKey.Secret]          = None
    override val flagsOpt: Option[Set[ApiKey.Flag]]        = Some(flags)
    override val policyAllowOpt: Option[Set[ApiKeyPolicy]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlags =
      new ApiKeyWithFlags(sid, friendlyName, flags, dateCreated, dateUpdated)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasFlagsAndSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags, dateCreated, dateUpdated)

    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasFlagsAndPolicyAllow =
      new ApiKeyWithFlagsAndPolicyAllow(
        sid,
        friendlyName,
        flags,
        policyAllow,
        dateCreated,
        dateUpdated
      )
  }

  private final class ApiKeyWithPolicyAllow(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val policyAllow: Set[ApiKeyPolicy],
      val dateCreated: Instant,
      val dateUpdated: Instant
  ) extends ApiKey
      with HasPolicyAllow {
    override val secretOpt: Option[ApiKey.Secret]          = None
    override val flagsOpt: Option[Set[ApiKey.Flag]]        = None
    override val policyAllowOpt: Option[Set[ApiKeyPolicy]] = Some(policyAllow)

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndPolicyAllow =
      new ApiKeyWithFlagsAndPolicyAllow(
        sid,
        friendlyName,
        flags,
        policyAllow,
        dateCreated,
        dateUpdated
      )

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecretAndPolicyAllow =
      new ApiKeyWithSecretAndPolicyAllow(
        sid,
        friendlyName,
        secret,
        policyAllow,
        dateCreated,
        dateUpdated
      )

    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasPolicyAllow =
      new ApiKeyWithPolicyAllow(sid, friendlyName, policyAllow, dateCreated, dateUpdated)
  }

  private final class ApiKeyWithSecretAndFlags(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val secret: ApiKey.Secret,
      val flags: Set[ApiKey.Flag],
      val dateCreated: Instant,
      val dateUpdated: Instant
  ) extends ApiKey
      with HasFlagsAndSecret {
    override val secretOpt: Option[ApiKey.Secret]          = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]]        = Some(flags)
    override val policyAllowOpt: Option[Set[ApiKeyPolicy]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags, dateCreated, dateUpdated)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasFlagsAndSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags, dateCreated, dateUpdated)

    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(
        sid,
        friendlyName,
        secret,
        flags,
        policyAllow,
        dateCreated,
        dateUpdated
      )
  }

  private final class ApiKeyWithSecretAndPolicyAllow(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val secret: ApiKey.Secret,
      val policyAllow: Set[ApiKeyPolicy],
      val dateCreated: Instant,
      val dateUpdated: Instant
  ) extends ApiKey
      with HasSecretAndPolicyAllow {
    override val secretOpt: Option[ApiKey.Secret]          = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]]        = None
    override val policyAllowOpt: Option[Set[ApiKeyPolicy]] = Some(policyAllow)

    override def withFlags(
        flags: Set[ApiKey.Flag]
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(
        sid,
        friendlyName,
        secret,
        flags,
        policyAllow,
        dateCreated,
        dateUpdated
      )

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecretAndPolicyAllow =
      new ApiKeyWithSecretAndPolicyAllow(
        sid,
        friendlyName,
        secret,
        policyAllow,
        dateCreated,
        dateUpdated
      )

    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasSecretAndPolicyAllow =
      new ApiKeyWithSecretAndPolicyAllow(
        sid,
        friendlyName,
        secret,
        policyAllow,
        dateCreated,
        dateUpdated
      )
  }

  private final class ApiKeyWithFlagsAndPolicyAllow(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val flags: Set[ApiKey.Flag],
      val policyAllow: Set[ApiKeyPolicy],
      val dateCreated: Instant,
      val dateUpdated: Instant
  ) extends ApiKey
      with HasFlagsAndPolicyAllow {
    override val secretOpt: Option[ApiKey.Secret]          = None
    override val flagsOpt: Option[Set[ApiKey.Flag]]        = Some(flags)
    override val policyAllowOpt: Option[Set[ApiKeyPolicy]] = Some(policyAllow)

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndPolicyAllow =
      new ApiKeyWithFlagsAndPolicyAllow(
        sid,
        friendlyName,
        flags,
        policyAllow,
        dateCreated,
        dateUpdated
      )

    override def withSecret(
        secret: ApiKey.Secret
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(
        sid,
        friendlyName,
        secret,
        flags,
        policyAllow,
        dateCreated,
        dateUpdated
      )

    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasFlagsAndPolicyAllow =
      new ApiKeyWithFlagsAndPolicyAllow(
        sid,
        friendlyName,
        flags,
        policyAllow,
        dateCreated,
        dateUpdated
      )
  }

  private final class ApiKeyWithSecretAndFlagsAndPolicyAllow(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val secret: ApiKey.Secret,
      val flags: Set[ApiKey.Flag],
      val policyAllow: Set[ApiKeyPolicy],
      val dateCreated: Instant,
      val dateUpdated: Instant
  ) extends ApiKey
      with HasAll {
    override val secretOpt: Option[ApiKey.Secret]          = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]]        = Some(flags)
    override val policyAllowOpt: Option[Set[ApiKeyPolicy]] = Some(policyAllow)

    override def withFlags(
        flags: Set[ApiKey.Flag]
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(
        sid,
        friendlyName,
        secret,
        flags,
        policyAllow,
        dateCreated,
        dateUpdated
      )

    override def withSecret(
        secret: ApiKey.Secret
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(
        sid,
        friendlyName,
        secret,
        flags,
        policyAllow,
        dateCreated,
        dateUpdated
      )

    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(
        sid,
        friendlyName,
        secret,
        flags,
        policyAllow,
        dateCreated,
        dateUpdated
      )
  }

  def apply(
      sid: ApiKey.Sid,
      friendlyName: ApiKey.FriendlyName,
      dateCreated: Instant,
      dateUpdated: Instant
  ): ApiKey = new ApiKeyBase(sid, friendlyName, dateCreated, dateUpdated)

  final case class Sid(val value: String) extends TwilioStringValue {
    override val toString: String = value
  }

  /** The secret of the API key.
    *
    * Only available at creation time — [[toString]] always redacts the value to prevent accidental
    * logging.
    */
  final case class Secret(value: String) {

    override val toString: String = "ApiKey.Secret(***)"
  }

  final case class FriendlyName(override val twilioString: String) extends TwilioStringValue

  sealed abstract class Flag(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object Flag extends EnumWithTwilioString[Flag] {
    case object Restricted     extends Flag("restricted")
    case object RestApi        extends Flag("rest_api")
    case object Signing        extends Flag("signing")
    case object ManageAccounts extends Flag("manage_accounts")
    case object ManageKeys     extends Flag("manage_keys")

    override val values: IndexedSeq[Flag] = findValues
  }

}
