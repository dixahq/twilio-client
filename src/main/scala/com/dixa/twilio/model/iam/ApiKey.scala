package com.dixa.twilio.model.iam

import com.dixa.twilio.model.{EnumWithTwilioString, TwilioStringValue}

/** Represent a Twilio Standard API key, as returned when the key is created.
  *
  * The [[ApiKey.Secret]] is only returned at creation time and cannot be retrieved afterwards.
  * Store it securely as soon as it is received.
  */
final case class ApiKey(
    sid: ApiKey.Sid,
    secret: ApiKey.Secret,
    friendlyName: ApiKey.FriendlyName,
    flags: Set[ApiKey.Flag]
)

object ApiKey {

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
