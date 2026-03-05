package com.dixa.twilio.model.iam

import com.dixa.twilio.model.{EnumWithTwilioString, TwilioStringValue}

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

  def withFlags(flags: Set[ApiKey.Flag]): ApiKey with ApiKey.HasFlags
  def withSecret(secret: ApiKey.Secret): ApiKey with ApiKey.HasSecret
  def withPolicyAllow(policyAllow: Set[ApiKeyPolicy]): ApiKey with ApiKey.HasPolicyAllow

  override def equals(obj: Any): Boolean = obj match {
    case other: ApiKey =>
      sid == other.sid &&
      secretOpt == other.secretOpt &&
      friendlyName == other.friendlyName &&
      flagsOpt == other.flagsOpt &&
      policyAllowOpt == other.policyAllowOpt
    case _ => false
  }

  override def hashCode(): Int =
    (sid, secretOpt, friendlyName, flagsOpt, policyAllowOpt).hashCode()

  override def toString: String =
    s"ApiKey(sid=$sid, secretOpt=$secretOpt, friendlyName=$friendlyName, flagsOpt=$flagsOpt, policyAllowOpt=$policyAllowOpt)"
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
      val friendlyName: ApiKey.FriendlyName
  ) extends ApiKey {
    override val secretOpt: Option[ApiKey.Secret]          = None
    override val flagsOpt: Option[Set[ApiKey.Flag]]        = None
    override val policyAllowOpt: Option[Set[ApiKeyPolicy]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlags =
      new ApiKeyWithFlags(sid, friendlyName, flags)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecret =
      new ApiKeyWithSecret(sid, friendlyName, secret)

    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasPolicyAllow =
      new ApiKeyWithPolicyAllow(sid, friendlyName, policyAllow)
  }

  private final class ApiKeyWithSecret(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val secret: ApiKey.Secret
  ) extends ApiKey
      with HasSecret {
    override val secretOpt: Option[ApiKey.Secret]          = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]]        = None
    override val policyAllowOpt: Option[Set[ApiKeyPolicy]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecret =
      new ApiKeyWithSecret(sid, friendlyName, secret)

    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasSecretAndPolicyAllow =
      new ApiKeyWithSecretAndPolicyAllow(sid, friendlyName, secret, policyAllow)
  }

  private final class ApiKeyWithFlags(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val flags: Set[ApiKey.Flag]
  ) extends ApiKey
      with HasFlags {
    override val secretOpt: Option[ApiKey.Secret]          = None
    override val flagsOpt: Option[Set[ApiKey.Flag]]        = Some(flags)
    override val policyAllowOpt: Option[Set[ApiKeyPolicy]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlags =
      new ApiKeyWithFlags(sid, friendlyName, flags)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasFlagsAndSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags)

    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasFlagsAndPolicyAllow =
      new ApiKeyWithFlagsAndPolicyAllow(sid, friendlyName, flags, policyAllow)
  }

  private final class ApiKeyWithPolicyAllow(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val policyAllow: Set[ApiKeyPolicy]
  ) extends ApiKey
      with HasPolicyAllow {
    override val secretOpt: Option[ApiKey.Secret]          = None
    override val flagsOpt: Option[Set[ApiKey.Flag]]        = None
    override val policyAllowOpt: Option[Set[ApiKeyPolicy]] = Some(policyAllow)

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndPolicyAllow =
      new ApiKeyWithFlagsAndPolicyAllow(sid, friendlyName, flags, policyAllow)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecretAndPolicyAllow =
      new ApiKeyWithSecretAndPolicyAllow(sid, friendlyName, secret, policyAllow)

    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasPolicyAllow =
      new ApiKeyWithPolicyAllow(sid, friendlyName, policyAllow)
  }

  private final class ApiKeyWithSecretAndFlags(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val secret: ApiKey.Secret,
      val flags: Set[ApiKey.Flag]
  ) extends ApiKey
      with HasFlagsAndSecret {
    override val secretOpt: Option[ApiKey.Secret]          = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]]        = Some(flags)
    override val policyAllowOpt: Option[Set[ApiKeyPolicy]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasFlagsAndSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags)

    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(sid, friendlyName, secret, flags, policyAllow)
  }

  private final class ApiKeyWithSecretAndPolicyAllow(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val secret: ApiKey.Secret,
      val policyAllow: Set[ApiKeyPolicy]
  ) extends ApiKey
      with HasSecretAndPolicyAllow {
    override val secretOpt: Option[ApiKey.Secret]          = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]]        = None
    override val policyAllowOpt: Option[Set[ApiKeyPolicy]] = Some(policyAllow)

    override def withFlags(
        flags: Set[ApiKey.Flag]
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(sid, friendlyName, secret, flags, policyAllow)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecretAndPolicyAllow =
      new ApiKeyWithSecretAndPolicyAllow(sid, friendlyName, secret, policyAllow)

    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasSecretAndPolicyAllow =
      new ApiKeyWithSecretAndPolicyAllow(sid, friendlyName, secret, policyAllow)
  }

  private final class ApiKeyWithFlagsAndPolicyAllow(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val flags: Set[ApiKey.Flag],
      val policyAllow: Set[ApiKeyPolicy]
  ) extends ApiKey
      with HasFlagsAndPolicyAllow {
    override val secretOpt: Option[ApiKey.Secret]          = None
    override val flagsOpt: Option[Set[ApiKey.Flag]]        = Some(flags)
    override val policyAllowOpt: Option[Set[ApiKeyPolicy]] = Some(policyAllow)

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndPolicyAllow =
      new ApiKeyWithFlagsAndPolicyAllow(sid, friendlyName, flags, policyAllow)

    override def withSecret(
        secret: ApiKey.Secret
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(sid, friendlyName, secret, flags, policyAllow)

    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasFlagsAndPolicyAllow =
      new ApiKeyWithFlagsAndPolicyAllow(sid, friendlyName, flags, policyAllow)
  }

  private final class ApiKeyWithSecretAndFlagsAndPolicyAllow(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val secret: ApiKey.Secret,
      val flags: Set[ApiKey.Flag],
      val policyAllow: Set[ApiKeyPolicy]
  ) extends ApiKey
      with HasAll {
    override val secretOpt: Option[ApiKey.Secret]          = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]]        = Some(flags)
    override val policyAllowOpt: Option[Set[ApiKeyPolicy]] = Some(policyAllow)

    override def withFlags(
        flags: Set[ApiKey.Flag]
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(sid, friendlyName, secret, flags, policyAllow)

    override def withSecret(
        secret: ApiKey.Secret
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(sid, friendlyName, secret, flags, policyAllow)

    override def withPolicyAllow(
        policyAllow: Set[ApiKeyPolicy]
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(sid, friendlyName, secret, flags, policyAllow)
  }

  def apply(
      sid: ApiKey.Sid,
      friendlyName: ApiKey.FriendlyName
  ): ApiKey = new ApiKeyBase(sid, friendlyName)

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
