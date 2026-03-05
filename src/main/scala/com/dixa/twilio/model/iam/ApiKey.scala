package com.dixa.twilio.model.iam

import com.dixa.twilio.model.{EnumWithTwilioString, TwilioStringValue}

/** Represent a Twilio Standard API key, as returned when the key is created.
  *
  * The [[ApiKey.Secret]] is only returned at creation time and cannot be retrieved afterwards.
  * Store it securely as soon as it is received.
  */
sealed trait ApiKey {
  def sid: ApiKey.Sid
  def secretOpt: Option[ApiKey.Secret]
  def friendlyName: ApiKey.FriendlyName
  def flagsOpt: Option[Set[ApiKey.Flag]]

  def withFlags(flags: Set[ApiKey.Flag]): ApiKey with ApiKey.HasFlags
  def withSecret(secret: ApiKey.Secret): ApiKey with ApiKey.HasSecret

  override def equals(obj: Any): Boolean = obj match {
    case other: ApiKey =>
      sid == other.sid &&
      secretOpt == other.secretOpt &&
      friendlyName == other.friendlyName &&
      flagsOpt == other.flagsOpt
    case _ => false
  }

  override def hashCode(): Int =
    (sid, secretOpt, friendlyName, flagsOpt).hashCode()

  override def toString: String =
    s"ApiKey(sid=$sid, secretOpt=$secretOpt, friendlyName=$friendlyName, flagsOpt=$flagsOpt)"
}

object ApiKey {

  sealed trait HasFlags { self: ApiKey =>
    def flags: Set[ApiKey.Flag]
  }

  sealed trait HasSecret { self: ApiKey =>
    def secret: ApiKey.Secret
  }

  private final class ApiKeyBase(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName
  ) extends ApiKey {
    override val secretOpt: Option[ApiKey.Secret]   = None
    override val flagsOpt: Option[Set[ApiKey.Flag]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlags =
      new ApiKeyWithFlags(sid, friendlyName, flags)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecret =
      new ApiKeyWithSecret(sid, friendlyName, secret)
  }

  private final class ApiKeyWithSecret(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val secret: ApiKey.Secret
  ) extends ApiKey
      with HasSecret {
    override val secretOpt: Option[ApiKey.Secret]   = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlags with HasSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecret =
      new ApiKeyWithSecret(sid, friendlyName, secret)
  }

  private final class ApiKeyWithFlags(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val flags: Set[ApiKey.Flag]
  ) extends ApiKey
      with HasFlags {
    override val secretOpt: Option[ApiKey.Secret]   = None
    override val flagsOpt: Option[Set[ApiKey.Flag]] = Some(flags)

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlags =
      new ApiKeyWithFlags(sid, friendlyName, flags)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecret with HasFlags =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags)
  }

  private final class ApiKeyWithSecretAndFlags(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val secret: ApiKey.Secret,
      val flags: Set[ApiKey.Flag]
  ) extends ApiKey
      with HasSecret
      with HasFlags {
    override val secretOpt: Option[ApiKey.Secret]   = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]] = Some(flags)

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlags with HasSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecret with HasFlags =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags)
  }

  /** Factory method to create an [[ApiKey]] with only the base attributes. */
  def apply(
      sid: ApiKey.Sid,
      friendlyName: ApiKey.FriendlyName
  ): ApiKey = new ApiKeyBase(sid, friendlyName)

  /** The SID of the API key. Starts with `SK`. */
  final case class Sid(val value: String) extends TwilioStringValue {
    override val toString: String = value
  }

  /** The secret of the API key.
    *
    * Only available at creation time — [[toString]] always redacts the value to prevent accidental
    * logging.
    */
  final case class Secret(val value: String) {

    /** Always returns a redacted representation to prevent accidental logging of the secret. */
    override val toString: String = "ApiKey.Secret(***)"
  }

  /** A human-readable label for the API key. */
  final case class FriendlyName(override val twilioString: String) extends TwilioStringValue

  /** A flag associated with the API key. */
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
