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
  def policyAllowOpt: Option[Set[ApiKey.PolicyAllow]]

  def withFlags(flags: Set[ApiKey.Flag]): ApiKey with ApiKey.HasFlags
  def withSecret(secret: ApiKey.Secret): ApiKey with ApiKey.HasSecret
  def withPolicyAllow(policyAllow: Set[ApiKey.PolicyAllow]): ApiKey with ApiKey.HasPolicyAllow

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
    def withPolicyAllow(policyAllow: Set[ApiKey.PolicyAllow]): ApiKey with HasFlagsAndPolicyAllow
  }

  sealed trait HasSecret { self: ApiKey =>
    def secret: ApiKey.Secret
    def withSecret(secret: ApiKey.Secret): ApiKey with HasSecret
    def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndSecret
    def withPolicyAllow(policyAllow: Set[ApiKey.PolicyAllow]): ApiKey with HasSecretAndPolicyAllow
  }

  sealed trait HasPolicyAllow { self: ApiKey =>
    def policyAllow: Set[ApiKey.PolicyAllow]
    def withPolicyAllow(policyAllow: Set[ApiKey.PolicyAllow]): ApiKey with HasPolicyAllow
    def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndPolicyAllow
    def withSecret(secret: ApiKey.Secret): ApiKey with HasSecretAndPolicyAllow
  }

  sealed trait HasFlagsAndSecret extends HasFlags with HasSecret { self: ApiKey =>
    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndSecret
    override def withSecret(secret: ApiKey.Secret): ApiKey with HasFlagsAndSecret
    override def withPolicyAllow(policyAllow: Set[ApiKey.PolicyAllow]): ApiKey with HasAll
  }

  sealed trait HasFlagsAndPolicyAllow extends HasFlags with HasPolicyAllow { self: ApiKey =>
    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndPolicyAllow
    override def withPolicyAllow(
        policyAllow: Set[ApiKey.PolicyAllow]
    ): ApiKey with HasFlagsAndPolicyAllow
    override def withSecret(secret: ApiKey.Secret): ApiKey with HasAll
  }

  sealed trait HasSecretAndPolicyAllow extends HasSecret with HasPolicyAllow { self: ApiKey =>
    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecretAndPolicyAllow
    override def withPolicyAllow(
        policyAllow: Set[ApiKey.PolicyAllow]
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
    override def withPolicyAllow(policyAllow: Set[ApiKey.PolicyAllow]): ApiKey with HasAll
  }

  private final class ApiKeyBase(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName
  ) extends ApiKey {
    override val secretOpt: Option[ApiKey.Secret]                = None
    override val flagsOpt: Option[Set[ApiKey.Flag]]              = None
    override val policyAllowOpt: Option[Set[ApiKey.PolicyAllow]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlags =
      new ApiKeyWithFlags(sid, friendlyName, flags)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecret =
      new ApiKeyWithSecret(sid, friendlyName, secret)

    override def withPolicyAllow(policyAllow: Set[ApiKey.PolicyAllow]): ApiKey with HasPolicyAllow =
      new ApiKeyWithPolicyAllow(sid, friendlyName, policyAllow)
  }

  private final class ApiKeyWithSecret(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val secret: ApiKey.Secret
  ) extends ApiKey
      with HasSecret {
    override val secretOpt: Option[ApiKey.Secret]                = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]]              = None
    override val policyAllowOpt: Option[Set[ApiKey.PolicyAllow]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecret =
      new ApiKeyWithSecret(sid, friendlyName, secret)

    override def withPolicyAllow(
        policyAllow: Set[ApiKey.PolicyAllow]
    ): ApiKey with HasSecretAndPolicyAllow =
      new ApiKeyWithSecretAndPolicyAllow(sid, friendlyName, secret, policyAllow)
  }

  private final class ApiKeyWithFlags(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val flags: Set[ApiKey.Flag]
  ) extends ApiKey
      with HasFlags {
    override val secretOpt: Option[ApiKey.Secret]                = None
    override val flagsOpt: Option[Set[ApiKey.Flag]]              = Some(flags)
    override val policyAllowOpt: Option[Set[ApiKey.PolicyAllow]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlags =
      new ApiKeyWithFlags(sid, friendlyName, flags)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasFlagsAndSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags)

    override def withPolicyAllow(
        policyAllow: Set[ApiKey.PolicyAllow]
    ): ApiKey with HasFlagsAndPolicyAllow =
      new ApiKeyWithFlagsAndPolicyAllow(sid, friendlyName, flags, policyAllow)
  }

  private final class ApiKeyWithPolicyAllow(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val policyAllow: Set[ApiKey.PolicyAllow]
  ) extends ApiKey
      with HasPolicyAllow {
    override val secretOpt: Option[ApiKey.Secret]                = None
    override val flagsOpt: Option[Set[ApiKey.Flag]]              = None
    override val policyAllowOpt: Option[Set[ApiKey.PolicyAllow]] = Some(policyAllow)

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndPolicyAllow =
      new ApiKeyWithFlagsAndPolicyAllow(sid, friendlyName, flags, policyAllow)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecretAndPolicyAllow =
      new ApiKeyWithSecretAndPolicyAllow(sid, friendlyName, secret, policyAllow)

    override def withPolicyAllow(policyAllow: Set[ApiKey.PolicyAllow]): ApiKey with HasPolicyAllow =
      new ApiKeyWithPolicyAllow(sid, friendlyName, policyAllow)
  }

  private final class ApiKeyWithSecretAndFlags(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val secret: ApiKey.Secret,
      val flags: Set[ApiKey.Flag]
  ) extends ApiKey
      with HasFlagsAndSecret {
    override val secretOpt: Option[ApiKey.Secret]                = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]]              = Some(flags)
    override val policyAllowOpt: Option[Set[ApiKey.PolicyAllow]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasFlagsAndSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags)

    override def withPolicyAllow(
        policyAllow: Set[ApiKey.PolicyAllow]
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(sid, friendlyName, secret, flags, policyAllow)
  }

  private final class ApiKeyWithSecretAndPolicyAllow(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val secret: ApiKey.Secret,
      val policyAllow: Set[ApiKey.PolicyAllow]
  ) extends ApiKey
      with HasSecretAndPolicyAllow {
    override val secretOpt: Option[ApiKey.Secret]                = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]]              = None
    override val policyAllowOpt: Option[Set[ApiKey.PolicyAllow]] = Some(policyAllow)

    override def withFlags(
        flags: Set[ApiKey.Flag]
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(sid, friendlyName, secret, flags, policyAllow)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecretAndPolicyAllow =
      new ApiKeyWithSecretAndPolicyAllow(sid, friendlyName, secret, policyAllow)

    override def withPolicyAllow(
        policyAllow: Set[ApiKey.PolicyAllow]
    ): ApiKey with HasSecretAndPolicyAllow =
      new ApiKeyWithSecretAndPolicyAllow(sid, friendlyName, secret, policyAllow)
  }

  private final class ApiKeyWithFlagsAndPolicyAllow(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val flags: Set[ApiKey.Flag],
      val policyAllow: Set[ApiKey.PolicyAllow]
  ) extends ApiKey
      with HasFlagsAndPolicyAllow {
    override val secretOpt: Option[ApiKey.Secret]                = None
    override val flagsOpt: Option[Set[ApiKey.Flag]]              = Some(flags)
    override val policyAllowOpt: Option[Set[ApiKey.PolicyAllow]] = Some(policyAllow)

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndPolicyAllow =
      new ApiKeyWithFlagsAndPolicyAllow(sid, friendlyName, flags, policyAllow)

    override def withSecret(
        secret: ApiKey.Secret
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(sid, friendlyName, secret, flags, policyAllow)

    override def withPolicyAllow(
        policyAllow: Set[ApiKey.PolicyAllow]
    ): ApiKey with HasFlagsAndPolicyAllow =
      new ApiKeyWithFlagsAndPolicyAllow(sid, friendlyName, flags, policyAllow)
  }

  private final class ApiKeyWithSecretAndFlagsAndPolicyAllow(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val secret: ApiKey.Secret,
      val flags: Set[ApiKey.Flag],
      val policyAllow: Set[ApiKey.PolicyAllow]
  ) extends ApiKey
      with HasAll {
    override val secretOpt: Option[ApiKey.Secret]                = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]]              = Some(flags)
    override val policyAllowOpt: Option[Set[ApiKey.PolicyAllow]] = Some(policyAllow)

    override def withFlags(
        flags: Set[ApiKey.Flag]
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(sid, friendlyName, secret, flags, policyAllow)

    override def withSecret(
        secret: ApiKey.Secret
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(sid, friendlyName, secret, flags, policyAllow)

    override def withPolicyAllow(
        policyAllow: Set[ApiKey.PolicyAllow]
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(sid, friendlyName, secret, flags, policyAllow)
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
  final case class Secret(value: String) {

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

  /** A policy allowed by the API key. */
  sealed abstract class PolicyAllow(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object PolicyAllow extends EnumWithTwilioString[PolicyAllow] {
    case object SipConnectionPoliciesTargetsAll
        extends PolicyAllow("/twilio/voice/sip.connection-policies.targets/*")
    case object InsightsCallAnnotationsUpdate
        extends PolicyAllow("/twilio/voice/insights.call.annotations/update")
    case object IntelligenceOperatorTypesList
        extends PolicyAllow("/twilio/voice/intelligence.operator-types/list")
    case object IntelligenceOperatorTypesRead
        extends PolicyAllow("/twilio/voice/intelligence.operator-types/read")
    case object CallsNotificationsRead extends PolicyAllow("/twilio/voice/calls.notifications/read")
    case object TranscriptionsList     extends PolicyAllow("/twilio/voice/transcriptions/list")
    case object SipTrunksAuthCallsCredentialListMappingsList
        extends PolicyAllow("/twilio/voice/sip.trunks.auth.calls.credential-list-mappings/list")
    case object SipIpAclMappingsRead   extends PolicyAllow("/twilio/voice/sip.ip-acl-mappings/read")
    case object OutgoingCallerIdsAll   extends PolicyAllow("/twilio/voice/outgoing-caller-ids/*")
    case object CallsNotificationsList extends PolicyAllow("/twilio/voice/calls.notifications/list")
    case object QueuesAll              extends PolicyAllow("/twilio/voice/queues/*")
    case object SipTrunksAuthCallsCredentialListMappingsRead
        extends PolicyAllow("/twilio/voice/sip.trunks.auth.calls.credential-list-mappings/read")
    case object SipTrunksOriginationUrlsAll
        extends PolicyAllow("/twilio/voice/sip.trunks.origination-urls/*")
    case object TranscriptionsRead extends PolicyAllow("/twilio/voice/transcriptions/read")
    case object RecordingsAddOnsPayloadList
        extends PolicyAllow("/twilio/voice/recordings.add-ons.payload/list")
    case object RecordingsTranscriptionsRead
        extends PolicyAllow("/twilio/voice/recordings.transcriptions/read")
    case object IntelligenceTranscriptsDelete
        extends PolicyAllow("/twilio/voice/intelligence.transcripts/delete")
    case object RecordingsTranscriptionsList
        extends PolicyAllow("/twilio/voice/recordings.transcriptions/list")
    case object IntelligenceOperatorCustomAll
        extends PolicyAllow("/twilio/voice/intelligence.operator.custom/*")
    case object SipDomainsAuthCallsCredentialListMappingsCreate
        extends PolicyAllow("/twilio/voice/sip.domains.auth.calls.credential-list-mappings/create")
    case object SipDomainsAuthRegistrationsCredentialListMappingsList
        extends PolicyAllow(
          "/twilio/voice/sip.domains.auth.registrations.credential-list-mappings/list"
        )
    case object IntelligenceOperatorAttachmentDelete
        extends PolicyAllow("/twilio/voice/intelligence.operator-attachment/delete")
    case object IntelligenceTranscriptsRead
        extends PolicyAllow("/twilio/voice/intelligence.transcripts/read")
    case object SipTrunksAuthCallsIpAclMappingsDelete
        extends PolicyAllow("/twilio/voice/sip.trunks.auth.calls.ip-acl-mappings/delete")
    case object SipDomainsAuthRegistrationsCredentialListMappingsRead
        extends PolicyAllow(
          "/twilio/voice/sip.domains.auth.registrations.credential-list-mappings/read"
        )
    case object SipIpAclMappingsDelete
        extends PolicyAllow("/twilio/voice/sip.ip-acl-mappings/delete")
    case object IntelligenceTranscriptsList
        extends PolicyAllow("/twilio/voice/intelligence.transcripts/list")
    case object RecordingsAddOnsPayloadRead
        extends PolicyAllow("/twilio/voice/recordings.add-ons.payload/read")
    case object IntelligenceTranscriptsCreate
        extends PolicyAllow("/twilio/voice/intelligence.transcripts/create")
    case object SipCredentialListsCredentialsAll
        extends PolicyAllow("/twilio/voice/sip.credential-lists.credentials/*")
    case object ConferencesUpdate extends PolicyAllow("/twilio/voice/conferences/update")
    case object SipDomainsAuthCallsCredentialListMappingsDelete
        extends PolicyAllow("/twilio/voice/sip.domains.auth.calls.credential-list-mappings/delete")
    case object SipIpAclMappingsList extends PolicyAllow("/twilio/voice/sip.ip-acl-mappings/list")
    case object SipIpAclsIpAddressesRead
        extends PolicyAllow("/twilio/voice/sip.ip-acls.ip-addresses/read")
    case object InsightsConferenceSummariesRead
        extends PolicyAllow("/twilio/voice/insights.conference.summaries/read")
    case object IntelligenceTranscriptSentencesRead
        extends PolicyAllow("/twilio/voice/intelligence.transcript-sentences/read")
    case object SipSourceIpMappingsAll extends PolicyAllow("/twilio/voice/sip.source-ip-mappings/*")
    case object ConferencesParticipantsAll
        extends PolicyAllow("/twilio/voice/conferences.participants/*")
    case object SipIpAclsIpAddressesList
        extends PolicyAllow("/twilio/voice/sip.ip-acls.ip-addresses/list")
    case object InsightsConferenceSummariesList
        extends PolicyAllow("/twilio/voice/insights.conference.summaries/list")
    case object InsightsCallEventsList
        extends PolicyAllow("/twilio/voice/insights.call.events/list")
    case object QueuesMemberUpdate extends PolicyAllow("/twilio/voice/queues.member/update")
    case object SipByocTrunksAll   extends PolicyAllow("/twilio/voice/sip.byoc-trunks/*")
    case object RecordingsRead     extends PolicyAllow("/twilio/voice/recordings/read")
    case object CallsAll           extends PolicyAllow("/twilio/voice/calls/*")
    case object RecordingsTranscriptionsDelete
        extends PolicyAllow("/twilio/voice/recordings.transcriptions/delete")
    case object TranscriptionsDelete extends PolicyAllow("/twilio/voice/transcriptions/delete")
    case object RecordingsList       extends PolicyAllow("/twilio/voice/recordings/list")
    case object IntelligenceOperatorAttachmentCreate
        extends PolicyAllow("/twilio/voice/intelligence.operator-attachment/create")
    case object SipConnectionPoliciesAll
        extends PolicyAllow("/twilio/voice/sip.connection-policies/*")
    case object InsightsConferenceParticipantsRead
        extends PolicyAllow("/twilio/voice/insights.conference.participants/read")
    case object SipTrunksAuthCallsCredentialListMappingsDelete
        extends PolicyAllow("/twilio/voice/sip.trunks.auth.calls.credential-list-mappings/delete")
    case object SipTrunksAuthCallsIpAclMappingsCreate
        extends PolicyAllow("/twilio/voice/sip.trunks.auth.calls.ip-acl-mappings/create")
    case object InsightsSettingsUpdate extends PolicyAllow("/twilio/voice/insights.settings/update")
    case object SipCredentialListsAll  extends PolicyAllow("/twilio/voice/sip.credential-lists/*")
    case object RecordingsAddOnsDelete
        extends PolicyAllow("/twilio/voice/recordings.add-ons/delete")
    case object SipDomainsAuthRegistrationsCredentialListMappingsDelete
        extends PolicyAllow(
          "/twilio/voice/sip.domains.auth.registrations.credential-list-mappings/delete"
        )
    case object IntelligenceOperatorPrebuiltList
        extends PolicyAllow("/twilio/voice/intelligence.operator.prebuilt/list")
    case object QueuesMemberList extends PolicyAllow("/twilio/voice/queues.member/list")
    case object SipTrunksAll     extends PolicyAllow("/twilio/voice/sip.trunks/*")
    case object ConferencesRecordingsUpdate
        extends PolicyAllow("/twilio/voice/conferences.recordings/update")
    case object TwimlAppsAll     extends PolicyAllow("/twilio/voice/twiml.apps/*")
    case object QueuesMemberRead extends PolicyAllow("/twilio/voice/queues.member/read")
    case object InsightsCallAnnotationsRead
        extends PolicyAllow("/twilio/voice/insights.call.annotations/read")
    case object InsightsCallMetricsList
        extends PolicyAllow("/twilio/voice/insights.call.metrics/list")
    case object IntelligenceServicesAll extends PolicyAllow("/twilio/voice/intelligence.services/*")
    case object SipIpAclMappingsCreate
        extends PolicyAllow("/twilio/voice/sip.ip-acl-mappings/create")
    case object SipIpAclsIpAddressesUpdate
        extends PolicyAllow("/twilio/voice/sip.ip-acls.ip-addresses/update")
    case object SipEmergencyAddressesDelete
        extends PolicyAllow("/twilio/voice/sip.emergency-addresses/delete")
    case object SipIpAclsIpAddressesCreate
        extends PolicyAllow("/twilio/voice/sip.ip-acls.ip-addresses/create")
    case object IntelligenceOperatorsRead
        extends PolicyAllow("/twilio/voice/intelligence.operators/read")
    case object SipTrunksAuthCallsIpAclMappingsList
        extends PolicyAllow("/twilio/voice/sip.trunks.auth.calls.ip-acl-mappings/list")
    case object IntelligenceOperatorAttachmentList
        extends PolicyAllow("/twilio/voice/intelligence.operator-attachment/list")
    case object SipDomainsAuthCallsCredentialListMappingsList
        extends PolicyAllow("/twilio/voice/sip.domains.auth.calls.credential-list-mappings/list")
    case object RecordingsDelete extends PolicyAllow("/twilio/voice/recordings/delete")
    case object SipTrunksAuthCallsCredentialListMappingsCreate
        extends PolicyAllow("/twilio/voice/sip.trunks.auth.calls.credential-list-mappings/create")
    case object RecordingsAddOnsRead extends PolicyAllow("/twilio/voice/recordings.add-ons/read")
    case object RequestInspectorRead extends PolicyAllow("/twilio/voice/request-inspector/read")
    case object SipEmergencyAddressesCreate
        extends PolicyAllow("/twilio/voice/sip.emergency-addresses/create")
    case object SipDomainsAll extends PolicyAllow("/twilio/voice/sip.domains/*")
    case object IntelligenceOperatorsList
        extends PolicyAllow("/twilio/voice/intelligence.operators/list")
    case object IntelligenceTranscriptMediaRead
        extends PolicyAllow("/twilio/voice/intelligence.transcript-media/read")
    case object RecordingsAddOnsPayloadDataRead
        extends PolicyAllow("/twilio/voice/recordings.add-ons.payload.data/read")
    case object ConferencesRecordingsList
        extends PolicyAllow("/twilio/voice/conferences.recordings/list")
    case object SipTrunksAuthCallsIpAclMappingsRead
        extends PolicyAllow("/twilio/voice/sip.trunks.auth.calls.ip-acl-mappings/read")
    case object SipDomainsAuthCallsCredentialListMappingsRead
        extends PolicyAllow("/twilio/voice/sip.domains.auth.calls.credential-list-mappings/read")
    case object IntelligenceOperatorPrebuiltRead
        extends PolicyAllow("/twilio/voice/intelligence.operator.prebuilt/read")
    case object CallsRecordingsAll extends PolicyAllow("/twilio/voice/calls.recordings/*")
    case object InsightsConferenceParticipantsList
        extends PolicyAllow("/twilio/voice/insights.conference.participants/list")
    case object IntelligenceOperatorResultsRead
        extends PolicyAllow("/twilio/voice/intelligence.operator-results/read")
    case object InsightsCallSummariesRead
        extends PolicyAllow("/twilio/voice/insights.call.summaries/read")
    case object SipIpRecordsAll      extends PolicyAllow("/twilio/voice/sip.ip-records/*")
    case object RecordingsAddOnsList extends PolicyAllow("/twilio/voice/recordings.add-ons/list")
    case object InsightsCallSummariesList
        extends PolicyAllow("/twilio/voice/insights.call.summaries/list")
    case object SipDomainsAuthRegistrationsCredentialListMappingsCreate
        extends PolicyAllow(
          "/twilio/voice/sip.domains.auth.registrations.credential-list-mappings/create"
        )
    case object InsightsSettingsRead extends PolicyAllow("/twilio/voice/insights.settings/read")
    case object SipIpAclsAll         extends PolicyAllow("/twilio/voice/sip.ip-acls/*")
    case object ConferencesList      extends PolicyAllow("/twilio/voice/conferences/list")
    case object ConferencesRead      extends PolicyAllow("/twilio/voice/conferences/read")

    override val values: IndexedSeq[PolicyAllow] = findValues
  }
}
