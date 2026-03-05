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

  /** A policy allowed by the API key.
    *
    * '''Implementation Notes'''
    *
    * Twilio's permission representation has some quirks. When all sub-permissions (read, write,
    * etc.) are set for a resource like `/twilio/voice/sip.connection-policies`, the API returns a
    * single wildcard permission: `/twilio/voice/sip.connection-policies&#47;*`.
    *
    * It's unclear whether such wildcards should be:
    *   - Treated as standalone permissions
    *   - Expanded into their constituent individual permissions
    *
    * Since Twilio doesn't currently provide an API for manipulating key permissions, we cannot
    * determine how they will structure this data when such an API becomes available. Two scenarios
    * are possible:
    *
    *   - '''If wildcards are supported''': They'll need to be preserved as-is
    *   - '''If wildcards aren't supported''': Expanding them to individual policies would be
    *     appropriate
    *
    * '''Current Approach'''
    *
    * Wildcards are modeled as distinct policy values until Twilio provides either:
    *   - An API for permission manipulation that clarifies the expected structure
    *   - Explicit documentation on the intended representation
    *
    * This approach can be reevaluated once more information is available.
    */
  sealed abstract class PolicyAllow(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object PolicyAllow extends EnumWithTwilioString[PolicyAllow] {

    /** Describes the individual URI entries that make up the BYOC Origination ConnectionPolicies
      * list.
      */
    case object SipConnectionPoliciesTargetsAll
        extends PolicyAllow("/twilio/voice/sip.connection-policies.targets/*")

    /** Annotate calls to provide subjective experience details. */
    case object InsightsCallAnnotationsUpdate
        extends PolicyAllow("/twilio/voice/insights.call.annotations/update")

    /** The OperatorType resource represents the Type of a Prebuilt or Custom Operator. */
    case object IntelligenceOperatorTypesList
        extends PolicyAllow("/twilio/voice/intelligence.operator-types/list")

    /** The OperatorType resource represents the Type of a Prebuilt or Custom Operator. */
    case object IntelligenceOperatorTypesRead
        extends PolicyAllow("/twilio/voice/intelligence.operator-types/read")

    /** The Notifications subresource on any given Call. */
    case object CallsNotificationsRead extends PolicyAllow("/twilio/voice/calls.notifications/read")

    /** Represents the transcribed text and metadata from a transcribed recording of a voice call.
      */
    case object TranscriptionsList extends PolicyAllow("/twilio/voice/transcriptions/list")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipTrunksAuthCallsCredentialListMappingsList
        extends PolicyAllow("/twilio/voice/sip.trunks.auth.calls.credential-list-mappings/list")

    /** IpAccessControlListMapping resources contain the list of IpAccessControlList resources
      * associated with this domain.
      */
    case object SipIpAclMappingsRead extends PolicyAllow("/twilio/voice/sip.ip-acl-mappings/read")

    /** Represents a single verified number that may be used as a caller ID when making outgoing
      * calls.
      */
    case object OutgoingCallerIdsAll extends PolicyAllow("/twilio/voice/outgoing-caller-ids/*")

    /** The Notifications subresource on any given Call. */
    case object CallsNotificationsList extends PolicyAllow("/twilio/voice/calls.notifications/list")

    /** Describes a call queue that contains individual calls, which are described by the queue's
      * Member resources.
      */
    case object QueuesAll extends PolicyAllow("/twilio/voice/queues/*")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipTrunksAuthCallsCredentialListMappingsRead
        extends PolicyAllow("/twilio/voice/sip.trunks.auth.calls.credential-list-mappings/read")

    /** The OriginationUrl Resource represents the Origination SIP URL(s) of your Trunk. */
    case object SipTrunksOriginationUrlsAll
        extends PolicyAllow("/twilio/voice/sip.trunks.origination-urls/*")

    /** Represents the transcribed text and metadata from a transcribed recording of a voice call.
      */
    case object TranscriptionsRead extends PolicyAllow("/twilio/voice/transcriptions/read")

    /** Exposes the multiple types of Payloads that may be in included in an Add-on Result. */
    case object RecordingsAddOnsPayloadList
        extends PolicyAllow("/twilio/voice/recordings.add-ons.payload/list")

    /** Represents the transcribed text and metadata from a transcribed recording of a voice call.
      */
    case object RecordingsTranscriptionsRead
        extends PolicyAllow("/twilio/voice/recordings.transcriptions/read")

    /** A Transcript resource represents a voice conversation that has automatically been converted
      * to text through Voice Intelligence.
      */
    case object IntelligenceTranscriptsDelete
        extends PolicyAllow("/twilio/voice/intelligence.transcripts/delete")

    /** Represents the transcribed text and metadata from a transcribed recording of a voice call.
      */
    case object RecordingsTranscriptionsList
        extends PolicyAllow("/twilio/voice/recordings.transcriptions/list")

    /** The CustomOperator subresource of the Operator resource represents a Custom Operator. */
    case object IntelligenceOperatorCustomAll
        extends PolicyAllow("/twilio/voice/intelligence.operator.custom/*")

    /** Represents the CredentialList resources associated with a SIP Domain. */
    case object SipDomainsAuthCallsCredentialListMappingsCreate
        extends PolicyAllow("/twilio/voice/sip.domains.auth.calls.credential-list-mappings/create")

    /** Subresource represents the CredentialList instances associated with this domain's
      * registration.
      */
    case object SipDomainsAuthRegistrationsCredentialListMappingsList
        extends PolicyAllow(
          "/twilio/voice/sip.domains.auth.registrations.credential-list-mappings/list"
        )

    /** OperatorAttachment represents the link between a specific Prebuilt or Custom Operator and a
      * specific Voice Intelligence Service.
      */
    case object IntelligenceOperatorAttachmentDelete
        extends PolicyAllow("/twilio/voice/intelligence.operator-attachment/delete")

    /** A Transcript resource represents a voice conversation that has automatically been converted
      * to text through Voice Intelligence.
      */
    case object IntelligenceTranscriptsRead
        extends PolicyAllow("/twilio/voice/intelligence.transcripts/read")

    /** IpAccessControlListMapping resources contain the list of IpAccessControlList resources
      * associated with this domain.
      */
    case object SipTrunksAuthCallsIpAclMappingsDelete
        extends PolicyAllow("/twilio/voice/sip.trunks.auth.calls.ip-acl-mappings/delete")

    /** Subresource represents the CredentialList instances associated with this domain's
      * registration.
      */
    case object SipDomainsAuthRegistrationsCredentialListMappingsRead
        extends PolicyAllow(
          "/twilio/voice/sip.domains.auth.registrations.credential-list-mappings/read"
        )

    /** IpAccessControlListMapping resources contain the list of IpAccessControlList resources
      * associated with this domain.
      */
    case object SipIpAclMappingsDelete
        extends PolicyAllow("/twilio/voice/sip.ip-acl-mappings/delete")

    /** A Transcript resource represents a voice conversation that has automatically been converted
      * to text through Voice Intelligence.
      */
    case object IntelligenceTranscriptsList
        extends PolicyAllow("/twilio/voice/intelligence.transcripts/list")

    /** Exposes the multiple types of Payloads that may be in included in an Add-on Result. */
    case object RecordingsAddOnsPayloadRead
        extends PolicyAllow("/twilio/voice/recordings.add-ons.payload/read")

    /** A Transcript resource represents a voice conversation that has automatically been converted
      * to text through Voice Intelligence.
      */
    case object IntelligenceTranscriptsCreate
        extends PolicyAllow("/twilio/voice/intelligence.transcripts/create")

    /** Contain the credentials resource entry of the users who are allowed to reach your SIP
      * Domain.
      */
    case object SipCredentialListsCredentialsAll
        extends PolicyAllow("/twilio/voice/sip.credential-lists.credentials/*")

    /** The Conference resource allows you to query and manage the state of conferences on your
      * Twilio account.
      */
    case object ConferencesUpdate extends PolicyAllow("/twilio/voice/conferences/update")

    /** Represents the CredentialList resources associated with a SIP Domain. */
    case object SipDomainsAuthCallsCredentialListMappingsDelete
        extends PolicyAllow("/twilio/voice/sip.domains.auth.calls.credential-list-mappings/delete")

    /** IpAccessControlListMapping resources contain the list of IpAccessControlList resources
      * associated with this domain.
      */
    case object SipIpAclMappingsList extends PolicyAllow("/twilio/voice/sip.ip-acl-mappings/list")

    /** Describes the IP addresses that have access to the SIP Domain. */
    case object SipIpAclsIpAddressesRead
        extends PolicyAllow("/twilio/voice/sip.ip-acls.ip-addresses/read")

    /** Conference summaries with events and metadata. */
    case object InsightsConferenceSummariesRead
        extends PolicyAllow("/twilio/voice/insights.conference.summaries/read")

    /** A Transcript Sentence is the actual text of the recording transcription. */
    case object IntelligenceTranscriptSentencesRead
        extends PolicyAllow("/twilio/voice/intelligence.transcript-sentences/read")

    /** Describes the publicly-routable Static IP addresses that can be used to receive Termination
      * traffic from a BYOC Carrier.
      */
    case object SipSourceIpMappingsAll extends PolicyAllow("/twilio/voice/sip.source-ip-mappings/*")

    /** Each Conference has a Participants subresource. Participants represent the set of people
      * currently connected to a running conference.
      */
    case object ConferencesParticipantsAll
        extends PolicyAllow("/twilio/voice/conferences.participants/*")

    /** Describes the IP addresses that have access to the SIP Domain. */
    case object SipIpAclsIpAddressesList
        extends PolicyAllow("/twilio/voice/sip.ip-acls.ip-addresses/list")

    /** Conference summaries with events and metadata. */
    case object InsightsConferenceSummariesList
        extends PolicyAllow("/twilio/voice/insights.conference.summaries/list")

    /** Provides call progress and quality-related Voice SDK events data for a specific call. */
    case object InsightsCallEventsList
        extends PolicyAllow("/twilio/voice/insights.call.events/list")

    /** Subresource of the Queue resource and represents a single call in a call queue. */
    case object QueuesMemberUpdate extends PolicyAllow("/twilio/voice/queues.member/update")

    /** Describes a trunk that can be configured to send/receive traffic to/from a PSTN Carrier. */
    case object SipByocTrunksAll extends PolicyAllow("/twilio/voice/sip.byoc-trunks/*")

    /** Represents the recording associated with a voice call, conference, or SIP Trunk. */
    case object RecordingsRead extends PolicyAllow("/twilio/voice/recordings/read")

    /** An object that represents a connection between a telephone and Twilio. */
    case object CallsAll extends PolicyAllow("/twilio/voice/calls/*")

    /** Represents the transcribed text and metadata from a transcribed recording of a voice call.
      */
    case object RecordingsTranscriptionsDelete
        extends PolicyAllow("/twilio/voice/recordings.transcriptions/delete")

    /** Represents the transcribed text and metadata from a transcribed recording of a voice call.
      */
    case object TranscriptionsDelete extends PolicyAllow("/twilio/voice/transcriptions/delete")

    /** Represents the recording associated with a voice call, conference, or SIP Trunk. */
    case object RecordingsList extends PolicyAllow("/twilio/voice/recordings/list")

    /** OperatorAttachment represents the link between a specific Prebuilt or Custom Operator and a
      * specific Voice Intelligence Service.
      */
    case object IntelligenceOperatorAttachmentCreate
        extends PolicyAllow("/twilio/voice/intelligence.operator-attachment/create")

    /** Describes a list of URI Entries that are used to route Origination traffic to a PSTN Carrier
      * over a BYOC Trunk.
      */
    case object SipConnectionPoliciesAll
        extends PolicyAllow("/twilio/voice/sip.connection-policies/*")

    /** Conference participant summaries with events and metadata for individual participants. */
    case object InsightsConferenceParticipantsRead
        extends PolicyAllow("/twilio/voice/insights.conference.participants/read")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipTrunksAuthCallsCredentialListMappingsDelete
        extends PolicyAllow("/twilio/voice/sip.trunks.auth.calls.credential-list-mappings/delete")

    /** IpAccessControlListMapping resources contain the list of IpAccessControlList resources
      * associated with this domain.
      */
    case object SipTrunksAuthCallsIpAclMappingsCreate
        extends PolicyAllow("/twilio/voice/sip.trunks.auth.calls.ip-acl-mappings/create")

    /** Control Voice Insights Advanced Features and Voice Trace status for an account. */
    case object InsightsSettingsUpdate extends PolicyAllow("/twilio/voice/insights.settings/update")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipCredentialListsAll extends PolicyAllow("/twilio/voice/sip.credential-lists/*")

    /** Add-on results live as a subresource under the integration point that the Add-on was
      * configured with.
      */
    case object RecordingsAddOnsDelete
        extends PolicyAllow("/twilio/voice/recordings.add-ons/delete")

    /** Subresource represents the CredentialList instances associated with this domain's
      * registration.
      */
    case object SipDomainsAuthRegistrationsCredentialListMappingsDelete
        extends PolicyAllow(
          "/twilio/voice/sip.domains.auth.registrations.credential-list-mappings/delete"
        )

    /** The PrebuiltOperator subresource of the Operator resource represents a Prebuilt Operator. */
    case object IntelligenceOperatorPrebuiltList
        extends PolicyAllow("/twilio/voice/intelligence.operator.prebuilt/list")

    /** Subresource of the Queue resource and represents a single call in a call queue. */
    case object QueuesMemberList extends PolicyAllow("/twilio/voice/queues.member/list")

    /** Elastic SIP Trunking enables you to make & receive telephone calls from your IP
      * communications infrastructure.
      */
    case object SipTrunksAll extends PolicyAllow("/twilio/voice/sip.trunks/*")

    /** The Recordings subresource on any given Conference. */
    case object ConferencesRecordingsUpdate
        extends PolicyAllow("/twilio/voice/conferences.recordings/update")

    /** Voice twiml apps. */
    case object TwimlAppsAll extends PolicyAllow("/twilio/voice/twiml.apps/*")

    /** Subresource of the Queue resource and represents a single call in a call queue. */
    case object QueuesMemberRead extends PolicyAllow("/twilio/voice/queues.member/read")

    /** Annotate calls to provide subjective experience details. */
    case object InsightsCallAnnotationsRead
        extends PolicyAllow("/twilio/voice/insights.call.annotations/read")

    /** Provides quality-related metrics for a specific call. */
    case object InsightsCallMetricsList
        extends PolicyAllow("/twilio/voice/insights.call.metrics/list")

    /** A Service provides control and configuration for how Transcripts are processed. */
    case object IntelligenceServicesAll extends PolicyAllow("/twilio/voice/intelligence.services/*")

    /** IpAccessControlListMapping resources contain the list of IpAccessControlList resources
      * associated with this domain.
      */
    case object SipIpAclMappingsCreate
        extends PolicyAllow("/twilio/voice/sip.ip-acl-mappings/create")

    /** Describes the IP addresses that have access to the SIP Domain. */
    case object SipIpAclsIpAddressesUpdate
        extends PolicyAllow("/twilio/voice/sip.ip-acls.ip-addresses/update")

    /** Emergency Address associated with a Twilio number. */
    case object SipEmergencyAddressesDelete
        extends PolicyAllow("/twilio/voice/sip.emergency-addresses/delete")

    /** Describes the IP addresses that have access to the SIP Domain. */
    case object SipIpAclsIpAddressesCreate
        extends PolicyAllow("/twilio/voice/sip.ip-acls.ip-addresses/create")

    /** The Operator resource represents a Prebuilt or Custom Operator. */
    case object IntelligenceOperatorsRead
        extends PolicyAllow("/twilio/voice/intelligence.operators/read")

    /** IpAccessControlListMapping resources contain the list of IpAccessControlList resources
      * associated with this domain.
      */
    case object SipTrunksAuthCallsIpAclMappingsList
        extends PolicyAllow("/twilio/voice/sip.trunks.auth.calls.ip-acl-mappings/list")

    /** OperatorAttachment represents the link between a specific Prebuilt or Custom Operator and a
      * specific Voice Intelligence Service.
      */
    case object IntelligenceOperatorAttachmentList
        extends PolicyAllow("/twilio/voice/intelligence.operator-attachment/list")

    /** Represents the CredentialList resources associated with a SIP Domain. */
    case object SipDomainsAuthCallsCredentialListMappingsList
        extends PolicyAllow("/twilio/voice/sip.domains.auth.calls.credential-list-mappings/list")

    /** Represents the recording associated with a voice call, conference, or SIP Trunk. */
    case object RecordingsDelete extends PolicyAllow("/twilio/voice/recordings/delete")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipTrunksAuthCallsCredentialListMappingsCreate
        extends PolicyAllow("/twilio/voice/sip.trunks.auth.calls.credential-list-mappings/create")

    /** Add-on results live as a subresource under the integration point that the Add-on was
      * configured with.
      */
    case object RecordingsAddOnsRead extends PolicyAllow("/twilio/voice/recordings.add-ons/read")

    /** Call Event resource. */
    case object RequestInspectorRead extends PolicyAllow("/twilio/voice/request-inspector/read")

    /** Emergency Address associated with a Twilio number. */
    case object SipEmergencyAddressesCreate
        extends PolicyAllow("/twilio/voice/sip.emergency-addresses/create")

    /** Describes a custom DNS hostname that can accept SIP traffic for your account. */
    case object SipDomainsAll extends PolicyAllow("/twilio/voice/sip.domains/*")

    /** The Operator resource represents a Prebuilt or Custom Operator. */
    case object IntelligenceOperatorsList
        extends PolicyAllow("/twilio/voice/intelligence.operators/list")

    /** A Transcript Media returns a signed URL for the Media (call recording) corresponding to the
      * Transcript.
      */
    case object IntelligenceTranscriptMediaRead
        extends PolicyAllow("/twilio/voice/intelligence.transcript-media/read")

    /** Hosts the actual data returned by the Add-on. */
    case object RecordingsAddOnsPayloadDataRead
        extends PolicyAllow("/twilio/voice/recordings.add-ons.payload.data/read")

    /** The Recordings subresource on any given Conference. */
    case object ConferencesRecordingsList
        extends PolicyAllow("/twilio/voice/conferences.recordings/list")

    /** IpAccessControlListMapping resources contain the list of IpAccessControlList resources
      * associated with this domain.
      */
    case object SipTrunksAuthCallsIpAclMappingsRead
        extends PolicyAllow("/twilio/voice/sip.trunks.auth.calls.ip-acl-mappings/read")

    /** Represents the CredentialList resources associated with a SIP Domain. */
    case object SipDomainsAuthCallsCredentialListMappingsRead
        extends PolicyAllow("/twilio/voice/sip.domains.auth.calls.credential-list-mappings/read")

    /** The PrebuiltOperator subresource of the Operator resource represents a Prebuilt Operator. */
    case object IntelligenceOperatorPrebuiltRead
        extends PolicyAllow("/twilio/voice/intelligence.operator.prebuilt/read")

    /** The Recordings subresource on any given Call. */
    case object CallsRecordingsAll extends PolicyAllow("/twilio/voice/calls.recordings/*")

    /** Conference participant summaries with events and metadata for individual participants. */
    case object InsightsConferenceParticipantsList
        extends PolicyAllow("/twilio/voice/insights.conference.participants/list")

    /** The OperatorResults resource returns a list of operator inferences for a Transcript. */
    case object IntelligenceOperatorResultsRead
        extends PolicyAllow("/twilio/voice/intelligence.operator-results/read")

    /** A call summary for calls. */
    case object InsightsCallSummariesRead
        extends PolicyAllow("/twilio/voice/insights.call.summaries/read")

    /** Describes Static IP addresses used to address the BYOC Trunk's Termination SIP Domain via an
      * IP Address instead of an FQDN.
      */
    case object SipIpRecordsAll extends PolicyAllow("/twilio/voice/sip.ip-records/*")

    /** Add-on results live as a subresource under the integration point that the Add-on was
      * configured with.
      */
    case object RecordingsAddOnsList extends PolicyAllow("/twilio/voice/recordings.add-ons/list")

    /** A call summary for calls. */
    case object InsightsCallSummariesList
        extends PolicyAllow("/twilio/voice/insights.call.summaries/list")

    /** Subresource represents the CredentialList instances associated with this domain's
      * registration.
      */
    case object SipDomainsAuthRegistrationsCredentialListMappingsCreate
        extends PolicyAllow(
          "/twilio/voice/sip.domains.auth.registrations.credential-list-mappings/create"
        )

    /** Control Voice Insights Advanced Features and Voice Trace status for an account. */
    case object InsightsSettingsRead extends PolicyAllow("/twilio/voice/insights.settings/read")

    /** IpAccessControlList resources contain the Access Control List (ACL). */
    case object SipIpAclsAll extends PolicyAllow("/twilio/voice/sip.ip-acls/*")

    /** The Conference resource allows you to query and manage the state of conferences on your
      * Twilio account.
      */
    case object ConferencesList extends PolicyAllow("/twilio/voice/conferences/list")

    /** The Conference resource allows you to query and manage the state of conferences on your
      * Twilio account.
      */
    case object ConferencesRead extends PolicyAllow("/twilio/voice/conferences/read")

    /** Represents a single verified number that may be used as a caller ID when making outgoing
      * calls.
      */
    case object OutgoingCallerIdsRead extends PolicyAllow("/twilio/voice/outgoing-caller-ids/read")

    /** Represents a single verified number that may be used as a caller ID when making outgoing
      * calls.
      */
    case object OutgoingCallerIdsList extends PolicyAllow("/twilio/voice/outgoing-caller-ids/list")

    /** Represents a single verified number that may be used as a caller ID when making outgoing
      * calls.
      */
    case object OutgoingCallerIdsCreate
        extends PolicyAllow("/twilio/voice/outgoing-caller-ids/create")

    /** Represents a single verified number that may be used as a caller ID when making outgoing
      * calls.
      */
    case object OutgoingCallerIdsUpdate
        extends PolicyAllow("/twilio/voice/outgoing-caller-ids/update")

    /** Represents a single verified number that may be used as a caller ID when making outgoing
      * calls.
      */
    case object OutgoingCallerIdsDelete
        extends PolicyAllow("/twilio/voice/outgoing-caller-ids/delete")

    /** Describes a call queue that contains individual calls, which are described by the queue's
      * Member resources.
      */
    case object QueuesRead extends PolicyAllow("/twilio/voice/queues/read")

    /** Describes a call queue that contains individual calls, which are described by the queue's
      * Member resources.
      */
    case object QueuesList extends PolicyAllow("/twilio/voice/queues/list")

    /** Describes a call queue that contains individual calls, which are described by the queue's
      * Member resources.
      */
    case object QueuesCreate extends PolicyAllow("/twilio/voice/queues/create")

    /** Describes a call queue that contains individual calls, which are described by the queue's
      * Member resources.
      */
    case object QueuesUpdate extends PolicyAllow("/twilio/voice/queues/update")

    /** Describes a call queue that contains individual calls, which are described by the queue's
      * Member resources.
      */
    case object QueuesDelete extends PolicyAllow("/twilio/voice/queues/delete")

    /** The OriginationUrl Resource represents the Origination SIP URL(s) of your Trunk. */
    case object SipTrunksOriginationUrlsRead
        extends PolicyAllow("/twilio/voice/sip.trunks.origination-urls/read")

    /** The OriginationUrl Resource represents the Origination SIP URL(s) of your Trunk. */
    case object SipTrunksOriginationUrlsList
        extends PolicyAllow("/twilio/voice/sip.trunks.origination-urls/list")

    /** The OriginationUrl Resource represents the Origination SIP URL(s) of your Trunk. */
    case object SipTrunksOriginationUrlsCreate
        extends PolicyAllow("/twilio/voice/sip.trunks.origination-urls/create")

    /** The OriginationUrl Resource represents the Origination SIP URL(s) of your Trunk. */
    case object SipTrunksOriginationUrlsUpdate
        extends PolicyAllow("/twilio/voice/sip.trunks.origination-urls/update")

    /** The OriginationUrl Resource represents the Origination SIP URL(s) of your Trunk. */
    case object SipTrunksOriginationUrlsDelete
        extends PolicyAllow("/twilio/voice/sip.trunks.origination-urls/delete")

    /** The CustomOperator subresource of the Operator resource represents a Custom Operator. */
    case object IntelligenceOperatorCustomRead
        extends PolicyAllow("/twilio/voice/intelligence.operator.custom/read")

    /** The CustomOperator subresource of the Operator resource represents a Custom Operator. */
    case object IntelligenceOperatorCustomList
        extends PolicyAllow("/twilio/voice/intelligence.operator.custom/list")

    /** The CustomOperator subresource of the Operator resource represents a Custom Operator. */
    case object IntelligenceOperatorCustomCreate
        extends PolicyAllow("/twilio/voice/intelligence.operator.custom/create")

    /** The CustomOperator subresource of the Operator resource represents a Custom Operator. */
    case object IntelligenceOperatorCustomUpdate
        extends PolicyAllow("/twilio/voice/intelligence.operator.custom/update")

    /** The CustomOperator subresource of the Operator resource represents a Custom Operator. */
    case object IntelligenceOperatorCustomDelete
        extends PolicyAllow("/twilio/voice/intelligence.operator.custom/delete")

    /** Contain the credentials resource entry of the users who are allowed to reach your SIP
      * Domain.
      */
    case object SipCredentialListsCredentialsRead
        extends PolicyAllow("/twilio/voice/sip.credential-lists.credentials/read")

    /** Contain the credentials resource entry of the users who are allowed to reach your SIP
      * Domain.
      */
    case object SipCredentialListsCredentialsList
        extends PolicyAllow("/twilio/voice/sip.credential-lists.credentials/list")

    /** Contain the credentials resource entry of the users who are allowed to reach your SIP
      * Domain.
      */
    case object SipCredentialListsCredentialsCreate
        extends PolicyAllow("/twilio/voice/sip.credential-lists.credentials/create")

    /** Contain the credentials resource entry of the users who are allowed to reach your SIP
      * Domain.
      */
    case object SipCredentialListsCredentialsUpdate
        extends PolicyAllow("/twilio/voice/sip.credential-lists.credentials/update")

    /** Contain the credentials resource entry of the users who are allowed to reach your SIP
      * Domain.
      */
    case object SipCredentialListsCredentialsDelete
        extends PolicyAllow("/twilio/voice/sip.credential-lists.credentials/delete")

    /** Describes the publicly-routable Static IP addresses that can be used to receive Termination
      * traffic from a BYOC Carrier.
      */
    case object SipSourceIpMappingsRead
        extends PolicyAllow("/twilio/voice/sip.source-ip-mappings/read")

    /** Describes the publicly-routable Static IP addresses that can be used to receive Termination
      * traffic from a BYOC Carrier.
      */
    case object SipSourceIpMappingsList
        extends PolicyAllow("/twilio/voice/sip.source-ip-mappings/list")

    /** Describes the publicly-routable Static IP addresses that can be used to receive Termination
      * traffic from a BYOC Carrier.
      */
    case object SipSourceIpMappingsCreate
        extends PolicyAllow("/twilio/voice/sip.source-ip-mappings/create")

    /** Describes the publicly-routable Static IP addresses that can be used to receive Termination
      * traffic from a BYOC Carrier.
      */
    case object SipSourceIpMappingsUpdate
        extends PolicyAllow("/twilio/voice/sip.source-ip-mappings/update")

    /** Describes the publicly-routable Static IP addresses that can be used to receive Termination
      * traffic from a BYOC Carrier.
      */
    case object SipSourceIpMappingsDelete
        extends PolicyAllow("/twilio/voice/sip.source-ip-mappings/delete")

    /** Each Conference has a Participants subresource. Participants represent the set of people
      * currently connected to a running conference.
      */
    case object ConferencesParticipantsRead
        extends PolicyAllow("/twilio/voice/conferences.participants/read")

    /** Each Conference has a Participants subresource. Participants represent the set of people
      * currently connected to a running conference.
      */
    case object ConferencesParticipantsList
        extends PolicyAllow("/twilio/voice/conferences.participants/list")

    /** Each Conference has a Participants subresource. Participants represent the set of people
      * currently connected to a running conference.
      */
    case object ConferencesParticipantsCreate
        extends PolicyAllow("/twilio/voice/conferences.participants/create")

    /** Each Conference has a Participants subresource. Participants represent the set of people
      * currently connected to a running conference.
      */
    case object ConferencesParticipantsUpdate
        extends PolicyAllow("/twilio/voice/conferences.participants/update")

    /** Each Conference has a Participants subresource. Participants represent the set of people
      * currently connected to a running conference.
      */
    case object ConferencesParticipantsDelete
        extends PolicyAllow("/twilio/voice/conferences.participants/delete")

    /** Describes a trunk that can be configured to send/receive traffic to/from a PSTN Carrier. */
    case object SipByocTrunksRead extends PolicyAllow("/twilio/voice/sip.byoc-trunks/read")

    /** Describes a trunk that can be configured to send/receive traffic to/from a PSTN Carrier. */
    case object SipByocTrunksList extends PolicyAllow("/twilio/voice/sip.byoc-trunks/list")

    /** Describes a trunk that can be configured to send/receive traffic to/from a PSTN Carrier. */
    case object SipByocTrunksCreate extends PolicyAllow("/twilio/voice/sip.byoc-trunks/create")

    /** Describes a trunk that can be configured to send/receive traffic to/from a PSTN Carrier. */
    case object SipByocTrunksUpdate extends PolicyAllow("/twilio/voice/sip.byoc-trunks/update")

    /** Describes a trunk that can be configured to send/receive traffic to/from a PSTN Carrier. */
    case object SipByocTrunksDelete extends PolicyAllow("/twilio/voice/sip.byoc-trunks/delete")

    /** An object that represents a connection between a telephone and Twilio. */
    case object CallsRead extends PolicyAllow("/twilio/voice/calls/read")

    /** An object that represents a connection between a telephone and Twilio. */
    case object CallsList extends PolicyAllow("/twilio/voice/calls/list")

    /** An object that represents a connection between a telephone and Twilio. */
    case object CallsCreate extends PolicyAllow("/twilio/voice/calls/create")

    /** An object that represents a connection between a telephone and Twilio. */
    case object CallsUpdate extends PolicyAllow("/twilio/voice/calls/update")

    /** An object that represents a connection between a telephone and Twilio. */
    case object CallsDelete extends PolicyAllow("/twilio/voice/calls/delete")

    /** Describes a list of URI Entries that are used to route Origination traffic to a PSTN Carrier
      * over a BYOC Trunk.
      */
    case object SipConnectionPoliciesRead
        extends PolicyAllow("/twilio/voice/sip.connection-policies/read")

    /** Describes a list of URI Entries that are used to route Origination traffic to a PSTN Carrier
      * over a BYOC Trunk.
      */
    case object SipConnectionPoliciesList
        extends PolicyAllow("/twilio/voice/sip.connection-policies/list")

    /** Describes a list of URI Entries that are used to route Origination traffic to a PSTN Carrier
      * over a BYOC Trunk.
      */
    case object SipConnectionPoliciesCreate
        extends PolicyAllow("/twilio/voice/sip.connection-policies/create")

    /** Describes a list of URI Entries that are used to route Origination traffic to a PSTN Carrier
      * over a BYOC Trunk.
      */
    case object SipConnectionPoliciesUpdate
        extends PolicyAllow("/twilio/voice/sip.connection-policies/update")

    /** Describes a list of URI Entries that are used to route Origination traffic to a PSTN Carrier
      * over a BYOC Trunk.
      */
    case object SipConnectionPoliciesDelete
        extends PolicyAllow("/twilio/voice/sip.connection-policies/delete")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipCredentialListsRead
        extends PolicyAllow("/twilio/voice/sip.credential-lists/read")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipCredentialListsList
        extends PolicyAllow("/twilio/voice/sip.credential-lists/list")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipCredentialListsCreate
        extends PolicyAllow("/twilio/voice/sip.credential-lists/create")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipCredentialListsUpdate
        extends PolicyAllow("/twilio/voice/sip.credential-lists/update")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipCredentialListsDelete
        extends PolicyAllow("/twilio/voice/sip.credential-lists/delete")

    /** Elastic SIP Trunking enables you to make & receive telephone calls from your IP
      * communications infrastructure.
      */
    case object SipTrunksRead extends PolicyAllow("/twilio/voice/sip.trunks/read")

    /** Elastic SIP Trunking enables you to make & receive telephone calls from your IP
      * communications infrastructure.
      */
    case object SipTrunksList extends PolicyAllow("/twilio/voice/sip.trunks/list")

    /** Elastic SIP Trunking enables you to make & receive telephone calls from your IP
      * communications infrastructure.
      */
    case object SipTrunksCreate extends PolicyAllow("/twilio/voice/sip.trunks/create")

    /** Elastic SIP Trunking enables you to make & receive telephone calls from your IP
      * communications infrastructure.
      */
    case object SipTrunksUpdate extends PolicyAllow("/twilio/voice/sip.trunks/update")

    /** Elastic SIP Trunking enables you to make & receive telephone calls from your IP
      * communications infrastructure.
      */
    case object SipTrunksDelete extends PolicyAllow("/twilio/voice/sip.trunks/delete")

    /** Voice twiml apps. */
    case object TwimlAppsRead extends PolicyAllow("/twilio/voice/twiml.apps/read")

    /** Voice twiml apps. */
    case object TwimlAppsList extends PolicyAllow("/twilio/voice/twiml.apps/list")

    /** Voice twiml apps. */
    case object TwimlAppsCreate extends PolicyAllow("/twilio/voice/twiml.apps/create")

    /** Voice twiml apps. */
    case object TwimlAppsUpdate extends PolicyAllow("/twilio/voice/twiml.apps/update")

    /** Voice twiml apps. */
    case object TwimlAppsDelete extends PolicyAllow("/twilio/voice/twiml.apps/delete")

    /** A Service provides control and configuration for how Transcripts are processed. */
    case object IntelligenceServicesRead
        extends PolicyAllow("/twilio/voice/intelligence.services/read")

    /** A Service provides control and configuration for how Transcripts are processed. */
    case object IntelligenceServicesList
        extends PolicyAllow("/twilio/voice/intelligence.services/list")

    /** A Service provides control and configuration for how Transcripts are processed. */
    case object IntelligenceServicesCreate
        extends PolicyAllow("/twilio/voice/intelligence.services/create")

    /** A Service provides control and configuration for how Transcripts are processed. */
    case object IntelligenceServicesUpdate
        extends PolicyAllow("/twilio/voice/intelligence.services/update")

    /** A Service provides control and configuration for how Transcripts are processed. */
    case object IntelligenceServicesDelete
        extends PolicyAllow("/twilio/voice/intelligence.services/delete")

    /** Describes a custom DNS hostname that can accept SIP traffic for your account. */
    case object SipDomainsRead extends PolicyAllow("/twilio/voice/sip.domains/read")

    /** Describes a custom DNS hostname that can accept SIP traffic for your account. */
    case object SipDomainsList extends PolicyAllow("/twilio/voice/sip.domains/list")

    /** Describes a custom DNS hostname that can accept SIP traffic for your account. */
    case object SipDomainsCreate extends PolicyAllow("/twilio/voice/sip.domains/create")

    /** Describes a custom DNS hostname that can accept SIP traffic for your account. */
    case object SipDomainsUpdate extends PolicyAllow("/twilio/voice/sip.domains/update")

    /** Describes a custom DNS hostname that can accept SIP traffic for your account. */
    case object SipDomainsDelete extends PolicyAllow("/twilio/voice/sip.domains/delete")

    /** The Recordings subresource on any given Call. */
    case object CallsRecordingsRead extends PolicyAllow("/twilio/voice/calls.recordings/read")

    /** The Recordings subresource on any given Call. */
    case object CallsRecordingsList extends PolicyAllow("/twilio/voice/calls.recordings/list")

    /** The Recordings subresource on any given Call. */
    case object CallsRecordingsCreate extends PolicyAllow("/twilio/voice/calls.recordings/create")

    /** The Recordings subresource on any given Call. */
    case object CallsRecordingsUpdate extends PolicyAllow("/twilio/voice/calls.recordings/update")

    /** The Recordings subresource on any given Call. */
    case object CallsRecordingsDelete extends PolicyAllow("/twilio/voice/calls.recordings/delete")

    /** Describes Static IP addresses used to address the BYOC Trunk's Termination SIP Domain via an
      * IP Address instead of an FQDN.
      */
    case object SipIpRecordsRead extends PolicyAllow("/twilio/voice/sip.ip-records/read")

    /** Describes Static IP addresses used to address the BYOC Trunk's Termination SIP Domain via an
      * IP Address instead of an FQDN.
      */
    case object SipIpRecordsList extends PolicyAllow("/twilio/voice/sip.ip-records/list")

    /** Describes Static IP addresses used to address the BYOC Trunk's Termination SIP Domain via an
      * IP Address instead of an FQDN.
      */
    case object SipIpRecordsCreate extends PolicyAllow("/twilio/voice/sip.ip-records/create")

    /** Describes Static IP addresses used to address the BYOC Trunk's Termination SIP Domain via an
      * IP Address instead of an FQDN.
      */
    case object SipIpRecordsUpdate extends PolicyAllow("/twilio/voice/sip.ip-records/update")

    /** Describes Static IP addresses used to address the BYOC Trunk's Termination SIP Domain via an
      * IP Address instead of an FQDN.
      */
    case object SipIpRecordsDelete extends PolicyAllow("/twilio/voice/sip.ip-records/delete")

    /** IpAccessControlList resources contain the Access Control List (ACL). */
    case object SipIpAclsRead extends PolicyAllow("/twilio/voice/sip.ip-acls/read")

    /** IpAccessControlList resources contain the Access Control List (ACL). */
    case object SipIpAclsList extends PolicyAllow("/twilio/voice/sip.ip-acls/list")

    /** IpAccessControlList resources contain the Access Control List (ACL). */
    case object SipIpAclsCreate extends PolicyAllow("/twilio/voice/sip.ip-acls/create")

    /** IpAccessControlList resources contain the Access Control List (ACL). */
    case object SipIpAclsUpdate extends PolicyAllow("/twilio/voice/sip.ip-acls/update")

    /** IpAccessControlList resources contain the Access Control List (ACL). */
    case object SipIpAclsDelete extends PolicyAllow("/twilio/voice/sip.ip-acls/delete")

    /** A general pattern for how one system can be notified of events generated by another system
      * in real-time.
      */
    case object VerifyWebhookAll extends PolicyAllow("/twilio/verify/webhook/*")

    /** A general pattern for how one system can be notified of events generated by another system
      * in real-time.
      */
    case object VerifyWebhookRead extends PolicyAllow("/twilio/verify/webhook/read")

    /** A general pattern for how one system can be notified of events generated by another system
      * in real-time.
      */
    case object VerifyWebhookList extends PolicyAllow("/twilio/verify/webhook/list")

    /** A general pattern for how one system can be notified of events generated by another system
      * in real-time.
      */
    case object VerifyWebhookCreate extends PolicyAllow("/twilio/verify/webhook/create")

    /** A general pattern for how one system can be notified of events generated by another system
      * in real-time.
      */
    case object VerifyWebhookUpdate extends PolicyAllow("/twilio/verify/webhook/update")

    /** A general pattern for how one system can be notified of events generated by another system
      * in real-time.
      */
    case object VerifyWebhookDelete extends PolicyAllow("/twilio/verify/webhook/delete")

    /** A verification channel. */
    case object VerifyFactorAll extends PolicyAllow("/twilio/verify/factor/*")

    /** A verification channel. */
    case object VerifyFactorRead extends PolicyAllow("/twilio/verify/factor/read")

    /** A verification channel. */
    case object VerifyFactorList extends PolicyAllow("/twilio/verify/factor/list")

    /** A verification channel. */
    case object VerifyFactorCreate extends PolicyAllow("/twilio/verify/factor/create")

    /** A verification channel. */
    case object VerifyFactorUpdate extends PolicyAllow("/twilio/verify/factor/update")

    /** A verification channel. */
    case object VerifyFactorDelete extends PolicyAllow("/twilio/verify/factor/delete")

    /** The limit that should be enforced against the key it is associated with. */
    case object VerifyBucketAll extends PolicyAllow("/twilio/verify/bucket/*")

    /** The limit that should be enforced against the key it is associated with. */
    case object VerifyBucketRead extends PolicyAllow("/twilio/verify/bucket/read")

    /** The limit that should be enforced against the key it is associated with. */
    case object VerifyBucketList extends PolicyAllow("/twilio/verify/bucket/list")

    /** The limit that should be enforced against the key it is associated with. */
    case object VerifyBucketCreate extends PolicyAllow("/twilio/verify/bucket/create")

    /** The limit that should be enforced against the key it is associated with. */
    case object VerifyBucketUpdate extends PolicyAllow("/twilio/verify/bucket/update")

    /** The limit that should be enforced against the key it is associated with. */
    case object VerifyBucketDelete extends PolicyAllow("/twilio/verify/bucket/delete")

    /** The set of common configurations used to create and check verifications. */
    case object VerifyServiceAll extends PolicyAllow("/twilio/verify/service/*")

    /** The set of common configurations used to create and check verifications. */
    case object VerifyServiceRead extends PolicyAllow("/twilio/verify/service/read")

    /** The set of common configurations used to create and check verifications. */
    case object VerifyServiceList extends PolicyAllow("/twilio/verify/service/list")

    /** The set of common configurations used to create and check verifications. */
    case object VerifyServiceCreate extends PolicyAllow("/twilio/verify/service/create")

    /** The set of common configurations used to create and check verifications. */
    case object VerifyServiceUpdate extends PolicyAllow("/twilio/verify/service/update")

    /** The set of common configurations used to create and check verifications. */
    case object VerifyServiceDelete extends PolicyAllow("/twilio/verify/service/delete")

    /** Define the keys to meter and limits to enforce when starting user verifications. */
    case object VerifyRateLimitAll extends PolicyAllow("/twilio/verify/rate-limit/*")

    /** Define the keys to meter and limits to enforce when starting user verifications. */
    case object VerifyRateLimitRead extends PolicyAllow("/twilio/verify/rate-limit/read")

    /** Define the keys to meter and limits to enforce when starting user verifications. */
    case object VerifyRateLimitList extends PolicyAllow("/twilio/verify/rate-limit/list")

    /** Define the keys to meter and limits to enforce when starting user verifications. */
    case object VerifyRateLimitCreate extends PolicyAllow("/twilio/verify/rate-limit/create")

    /** Define the keys to meter and limits to enforce when starting user verifications. */
    case object VerifyRateLimitUpdate extends PolicyAllow("/twilio/verify/rate-limit/update")

    /** Define the keys to meter and limits to enforce when starting user verifications. */
    case object VerifyRateLimitDelete extends PolicyAllow("/twilio/verify/rate-limit/delete")

    /** Rules define the logic flow used by the Fraud Risk Engine's rule processor to identify
      * fraudulent activity and take appropriate actions.
      */
    case object VerifyRuleUpdate extends PolicyAllow("/twilio/verify/rule/update")

    /** Rules define the logic flow used by the Fraud Risk Engine's rule processor to identify
      * fraudulent activity and take appropriate actions.
      */
    case object VerifyRuleDelete extends PolicyAllow("/twilio/verify/rule/delete")

    /** A user or other identity that needs verification. */
    case object VerifyEntityCreate extends PolicyAllow("/twilio/verify/entity/create")

    /** Predefined and approved messages used to send verifications that allow customization of the
      * verification message.
      */
    case object VerifyVerificationTemplateList
        extends PolicyAllow("/twilio/verify/verification-template/list")

    /** A single verification attempt of an Entity using a Factor. */
    case object VerifyChallengeCreate extends PolicyAllow("/twilio/verify/challenge/create")

    /** A single verification attempt of an Entity using a Factor. */
    case object VerifyChallengeList extends PolicyAllow("/twilio/verify/challenge/list")

    /** A single verification attempt of an Entity using a Factor. */
    case object VerifyChallengeRead extends PolicyAllow("/twilio/verify/challenge/read")

    /** List of phone numbers that will never be blocked by Verify Fraud Guard or Geo permissions.
      */
    case object VerifySafelistRead extends PolicyAllow("/twilio/verify/safelist/read")

    /** A user or other identity that needs verification. */
    case object VerifyEntityRead extends PolicyAllow("/twilio/verify/entity/read")

    /** Represents a verification validation that checks if a user-provided token is correct. */
    case object VerifyVerificationCheckCreate
        extends PolicyAllow("/twilio/verify/verification-check/create")

    /** Resource to authenticate client's request to the Verify Push API when creating (i.e.,
      * enrolling or registering) an Entity and/or Factor.
      */
    case object VerifyAccessTokenRead extends PolicyAllow("/twilio/verify/access-token/read")

    /** A user or other identity that needs verification. */
    case object VerifyEntityList extends PolicyAllow("/twilio/verify/entity/list")

    /** Verify a user has a claimed device, phone number, or email address in their possession. */
    case object VerifyVerificationCreate extends PolicyAllow("/twilio/verify/verification/create")

    /** Verify a user has a claimed device, phone number, or email address in their possession. */
    case object VerifyVerificationRead extends PolicyAllow("/twilio/verify/verification/read")

    /** Rules define the logic flow used by the Fraud Risk Engine's rule processor to identify
      * fraudulent activity and take appropriate actions.
      */
    case object VerifyRuleList extends PolicyAllow("/twilio/verify/rule/list")

    /** List of phone numbers that will never be blocked by Verify Fraud Guard or Geo permissions.
      */
    case object VerifySafelistDelete extends PolicyAllow("/twilio/verify/safelist/delete")

    /** Lets developers request Verify Push retry sending a push notification for the same
      * Challenge.
      */
    case object VerifyPushNotificationCreate
        extends PolicyAllow("/twilio/verify/push-notification/create")

    /** Verify a user has a claimed device, phone number, or email address in their possession. */
    case object VerifyVerificationUpdate extends PolicyAllow("/twilio/verify/verification/update")

    /** List and filter verification attempts generated by Verify V2 services. */
    case object VerifyVerificationAttemptList
        extends PolicyAllow("/twilio/verify/verification-attempt/list")

    /** A user or other identity that needs verification. */
    case object VerifyEntityDelete extends PolicyAllow("/twilio/verify/entity/delete")

    /** Rules define the logic flow used by the Fraud Risk Engine's rule processor to identify
      * fraudulent activity and take appropriate actions.
      */
    case object VerifyRuleCreate extends PolicyAllow("/twilio/verify/rule/create")

    /** List of phone numbers that will never be blocked by Verify Fraud Guard or Geo permissions.
      */
    case object VerifySafelistCreate extends PolicyAllow("/twilio/verify/safelist/create")

    /** List and filter verification attempts generated by Verify V2 services. */
    case object VerifyVerificationAttemptRead
        extends PolicyAllow("/twilio/verify/verification-attempt/read")

    /** Summarize verification attempts generated by Verify V2 services. */
    case object VerifyVerificationAttemptsSummaryRead
        extends PolicyAllow("/twilio/verify/verification-attempts-summary/read")

    /** Resource to authenticate client's request to the Verify Push API when creating (i.e.,
      * enrolling or registering) an Entity and/or Factor.
      */
    case object VerifyAccessTokenCreate extends PolicyAllow("/twilio/verify/access-token/create")

    /** The US App to Person (A2P) Messaging Service Use Case Resource allows you to fetch possible
      * A2P use cases for a Messaging Service.
      */
    case object MessagingServicesUsa2pUsecaseList
        extends PolicyAllow("/twilio/messaging/services.usa2p-usecase/list")

    /** Represents a phone number associated to a Messaging Service. */
    case object MessagingServicesPhonenumbersRead
        extends PolicyAllow("/twilio/messaging/services.phonenumbers/read")

    /** Represents a phone number associated to a Messaging Service. */
    case object MessagingServicesPhonenumbersList
        extends PolicyAllow("/twilio/messaging/services.phonenumbers/list")

    /** Represents a phone number associated to a Messaging Service. */
    case object MessagingServicesPhonenumbersCreate
        extends PolicyAllow("/twilio/messaging/services.phonenumbers/create")

    /** Represents a phone number associated to a Messaging Service. */
    case object MessagingServicesPhonenumbersDelete
        extends PolicyAllow("/twilio/messaging/services.phonenumbers/delete")

    /** The US App to Person (A2P) Campaign Resource allows you to create a US A2P Campaign for a
      * Messaging Service.
      */
    case object MessagingServicesUsa2pCampaignRead
        extends PolicyAllow("/twilio/messaging/services.usa2p-campaign/read")

    /** The US App to Person (A2P) Campaign Resource allows you to create a US A2P Campaign for a
      * Messaging Service.
      */
    case object MessagingServicesUsa2pCampaignList
        extends PolicyAllow("/twilio/messaging/services.usa2p-campaign/list")

    /** The US App to Person (A2P) Campaign Resource allows you to create a US A2P Campaign for a
      * Messaging Service.
      */
    case object MessagingServicesUsa2pCampaignCreate
        extends PolicyAllow("/twilio/messaging/services.usa2p-campaign/create")

    /** The US App to Person (A2P) Campaign Resource allows you to create a US A2P Campaign for a
      * Messaging Service.
      */
    case object MessagingServicesUsa2pCampaignDelete
        extends PolicyAllow("/twilio/messaging/services.usa2p-campaign/delete")

    /** Represents a channel sender that is associated with a Messaging Service, such as WhatsApp.
      */
    case object MessagingServicesChannelsendersRead
        extends PolicyAllow("/twilio/messaging/services.channelsenders/read")

    /** Represents a channel sender that is associated with a Messaging Service, such as WhatsApp.
      */
    case object MessagingServicesChannelsendersList
        extends PolicyAllow("/twilio/messaging/services.channelsenders/list")

    /** Represents the short codes associated to a Messaging Service. */
    case object MessagingServicesShortcodesRead
        extends PolicyAllow("/twilio/messaging/services.shortcodes/read")

    /** Represents the short codes associated to a Messaging Service. */
    case object MessagingServicesShortcodesList
        extends PolicyAllow("/twilio/messaging/services.shortcodes/list")

    /** Represents the short codes associated to a Messaging Service. */
    case object MessagingServicesShortcodesCreate
        extends PolicyAllow("/twilio/messaging/services.shortcodes/create")

    /** Represents the short codes associated to a Messaging Service. */
    case object MessagingServicesShortcodesDelete
        extends PolicyAllow("/twilio/messaging/services.shortcodes/delete")

    /** Represents an Alphanumeric Sender ID (alpha sender) associated with a Messaging Service. */
    case object MessagingServicesAlphasendersRead
        extends PolicyAllow("/twilio/messaging/services.alphasenders/read")

    /** Represents an Alphanumeric Sender ID (alpha sender) associated with a Messaging Service. */
    case object MessagingServicesAlphasendersList
        extends PolicyAllow("/twilio/messaging/services.alphasenders/list")

    /** Represents an Alphanumeric Sender ID (alpha sender) associated with a Messaging Service. */
    case object MessagingServicesAlphasendersCreate
        extends PolicyAllow("/twilio/messaging/services.alphasenders/create")

    /** Represents an Alphanumeric Sender ID (alpha sender) associated with a Messaging Service. */
    case object MessagingServicesAlphasendersDelete
        extends PolicyAllow("/twilio/messaging/services.alphasenders/delete")

    /** Represents a set of configurable behavior for sending and receiving messages. */
    case object MessagingServicesAll extends PolicyAllow("/twilio/messaging/services/*")

    /** Represents a set of configurable behavior for sending and receiving messages. */
    case object MessagingServicesRead extends PolicyAllow("/twilio/messaging/services/read")

    /** Represents a set of configurable behavior for sending and receiving messages. */
    case object MessagingServicesList extends PolicyAllow("/twilio/messaging/services/list")

    /** Represents a set of configurable behavior for sending and receiving messages. */
    case object MessagingServicesCreate extends PolicyAllow("/twilio/messaging/services/create")

    /** Represents a set of configurable behavior for sending and receiving messages. */
    case object MessagingServicesUpdate extends PolicyAllow("/twilio/messaging/services/update")

    /** Represents a set of configurable behavior for sending and receiving messages. */
    case object MessagingServicesDelete extends PolicyAllow("/twilio/messaging/services/delete")

    /** Represents a Destination Alpha Sender associated with a Messaging Service. Destination Alpha
      * Sender can send to a particular ISO country code.
      */
    case object MessagingServicesDestinationAlphaSendersRead
        extends PolicyAllow("/twilio/messaging/services.destination-alpha-senders/read")

    /** Represents a Destination Alpha Sender associated with a Messaging Service. Destination Alpha
      * Sender can send to a particular ISO country code.
      */
    case object MessagingServicesDestinationAlphaSendersList
        extends PolicyAllow("/twilio/messaging/services.destination-alpha-senders/list")

    /** Represents a Destination Alpha Sender associated with a Messaging Service. Destination Alpha
      * Sender can send to a particular ISO country code.
      */
    case object MessagingServicesDestinationAlphaSendersCreate
        extends PolicyAllow("/twilio/messaging/services.destination-alpha-senders/create")

    /** Represents a Destination Alpha Sender associated with a Messaging Service. Destination Alpha
      * Sender can send to a particular ISO country code.
      */
    case object MessagingServicesDestinationAlphaSendersDelete
        extends PolicyAllow("/twilio/messaging/services.destination-alpha-senders/delete")

    /** Represents an inbound or outbound message. */
    case object MessagingMessagesAll extends PolicyAllow("/twilio/messaging/messages/*")

    /** Represents an inbound or outbound message. */
    case object MessagingMessagesRead extends PolicyAllow("/twilio/messaging/messages/read")

    /** Represents an inbound or outbound message. */
    case object MessagingMessagesList extends PolicyAllow("/twilio/messaging/messages/list")

    /** Represents an inbound or outbound message. */
    case object MessagingMessagesCreate extends PolicyAllow("/twilio/messaging/messages/create")

    /** Represents an inbound or outbound message. */
    case object MessagingMessagesUpdate extends PolicyAllow("/twilio/messaging/messages/update")

    /** Represents an inbound or outbound message. */
    case object MessagingMessagesDelete extends PolicyAllow("/twilio/messaging/messages/delete")

    /** The MessageFeedback subresource represents the reported outcome of tracking the performance
      * of a user action taken by the recipient of the message.
      */
    case object MessagingMessagesFeedbackCreate
        extends PolicyAllow("/twilio/messaging/messages.feedback/create")

    /** Represents templated messages for messaging use cases. */
    case object MessagingContentTemplatesRead
        extends PolicyAllow("/twilio/messaging/content-templates/read")

    /** Represents templated messages for messaging use cases. */
    case object MessagingContentTemplatesList
        extends PolicyAllow("/twilio/messaging/content-templates/list")

    /** Represents templated messages for messaging use cases. */
    case object MessagingContentTemplatesCreate
        extends PolicyAllow("/twilio/messaging/content-templates/create")

    /** Represents templated messages for messaging use cases. */
    case object MessagingContentTemplatesDelete
        extends PolicyAllow("/twilio/messaging/content-templates/delete")

    /** Provides a simple API to pull real-time, account specific pricing. */
    case object MessagingPricingRead extends PolicyAllow("/twilio/messaging/pricing/read")

    /** Provides a simple API to pull real-time, account specific pricing. */
    case object MessagingPricingList extends PolicyAllow("/twilio/messaging/pricing/list")

    /** This resource retrieves a list of United States phone numbers that have been deactivated by
      * mobile carriers.
      */
    case object MessagingDeactivationsList
        extends PolicyAllow("/twilio/messaging/deactivations/list")

    /** Represents a Whatsapp Sender. */
    case object MessagingWhatsappSendersAll
        extends PolicyAllow("/twilio/messaging/whatsapp-senders/*")

    /** Represents a Whatsapp Sender. */
    case object MessagingWhatsappSendersRead
        extends PolicyAllow("/twilio/messaging/whatsapp-senders/read")

    /** Represents a Whatsapp Sender. */
    case object MessagingWhatsappSendersList
        extends PolicyAllow("/twilio/messaging/whatsapp-senders/list")

    /** Represents a Whatsapp Sender. */
    case object MessagingWhatsappSendersCreate
        extends PolicyAllow("/twilio/messaging/whatsapp-senders/create")

    /** Represents a Whatsapp Sender. */
    case object MessagingWhatsappSendersUpdate
        extends PolicyAllow("/twilio/messaging/whatsapp-senders/update")

    /** Represents a Whatsapp Sender. */
    case object MessagingWhatsappSendersDelete
        extends PolicyAllow("/twilio/messaging/whatsapp-senders/delete")

    /** Real time statistics for a Task Queue. */
    case object TaskRouterWorkspacesTaskQueueRealTimeStatisticsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.task-queue.real-time-statistics/list")

    /** TaskRouter logs Events for each state change in the Workspace for the purpose of historical
      * reporting and auditing; it keeps that data for 30 days.
      */
    case object TaskRouterWorkspacesEventsRead
        extends PolicyAllow("/twilio/taskrouter/workspaces.events/read")

    /** TaskRouter logs Events for each state change in the Workspace for the purpose of historical
      * reporting and auditing; it keeps that data for 30 days.
      */
    case object TaskRouterWorkspacesEventsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.events/list")

    /** Cumulative statistics of your Task Queue over a certain time period. Cumulative statistics
      * allow you to analyze data from the past 30 days.
      */
    case object TaskRouterWorkspacesTaskQueuesCumulativeStatisticsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.task-queues.cumulative-statistics/list")

    /** Real time statistics for a Workspace. */
    case object TaskRouterWorkspacesRealTimeStatisticsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.real-time-statistics/list")

    /** Real time statistics for multiple Task Queues. */
    case object TaskRouterWorkspacesTaskQueueBulkRealTimeStatisticsList
        extends PolicyAllow(
          "/twilio/taskrouter/workspaces.task-queue.bulk-real-time-statistics/list"
        )

    /** Statistics for a Workspace. */
    case object TaskRouterWorkspacesStatisticsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.statistics/list")

    /** Cumulative statistics for your Workspace over a certain time period. Cumulative statistics
      * allow you to analyze data from the past 30 days.
      */
    case object TaskRouterWorkspacesCumulativeStatisticsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.cumulative-statistics/list")

    /** Workflows control how tasks will be prioritized and routed into Queues, and how Tasks should
      * escalate in priority or move across queues over time.
      */
    case object TaskRouterWorkspacesWorkflowsAll
        extends PolicyAllow("/twilio/taskrouter/workspaces.workflows/*")

    /** Workflows control how tasks will be prioritized and routed into Queues, and how Tasks should
      * escalate in priority or move across queues over time.
      */
    case object TaskRouterWorkspacesWorkflowsRead
        extends PolicyAllow("/twilio/taskrouter/workspaces.workflows/read")

    /** Workflows control how tasks will be prioritized and routed into Queues, and how Tasks should
      * escalate in priority or move across queues over time.
      */
    case object TaskRouterWorkspacesWorkflowsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.workflows/list")

    /** Workflows control how tasks will be prioritized and routed into Queues, and how Tasks should
      * escalate in priority or move across queues over time.
      */
    case object TaskRouterWorkspacesWorkflowsCreate
        extends PolicyAllow("/twilio/taskrouter/workspaces.workflows/create")

    /** Workflows control how tasks will be prioritized and routed into Queues, and how Tasks should
      * escalate in priority or move across queues over time.
      */
    case object TaskRouterWorkspacesWorkflowsUpdate
        extends PolicyAllow("/twilio/taskrouter/workspaces.workflows/update")

    /** Workflows control how tasks will be prioritized and routed into Queues, and how Tasks should
      * escalate in priority or move across queues over time.
      */
    case object TaskRouterWorkspacesWorkflowsDelete
        extends PolicyAllow("/twilio/taskrouter/workspaces.workflows/delete")

    /** TaskRouter creates a Reservation subresource whenever a Task is reserved for a Worker.
      * TaskRouter will provide the details of this Reservation Instance subresource in the
      * Assignment Callback HTTP request it makes to your application server.
      */
    case object TaskRouterWorkspacesTasksReservationsRead
        extends PolicyAllow("/twilio/taskrouter/workspaces.tasks.reservations/read")

    /** TaskRouter creates a Reservation subresource whenever a Task is reserved for a Worker.
      * TaskRouter will provide the details of this Reservation Instance subresource in the
      * Assignment Callback HTTP request it makes to your application server.
      */
    case object TaskRouterWorkspacesTasksReservationsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.tasks.reservations/list")

    /** TaskRouter creates a Reservation subresource whenever a Task is reserved for a Worker.
      * TaskRouter will provide the details of this Reservation Instance subresource in the
      * Assignment Callback HTTP request it makes to your application server.
      */
    case object TaskRouterWorkspacesTasksReservationsUpdate
        extends PolicyAllow("/twilio/taskrouter/workspaces.tasks.reservations/update")

    /** Real time statistics for a Workflow. */
    case object TaskRouterWorkspacesWorkflowsRealTimeStatisticsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.workflows.real-time-statistics/list")

    /** Worker Reservations represent the current and past reservations for a Worker. Current
      * Reservations can be accepted using the Reservation instance resource.
      */
    case object TaskRouterWorkspacesWorkersReservationsRead
        extends PolicyAllow("/twilio/taskrouter/workspaces.workers.reservations/read")

    /** Worker Reservations represent the current and past reservations for a Worker. Current
      * Reservations can be accepted using the Reservation instance resource.
      */
    case object TaskRouterWorkspacesWorkersReservationsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.workers.reservations/list")

    /** Worker Reservations represent the current and past reservations for a Worker. Current
      * Reservations can be accepted using the Reservation instance resource.
      */
    case object TaskRouterWorkspacesWorkersReservationsUpdate
        extends PolicyAllow("/twilio/taskrouter/workspaces.workers.reservations/update")

    /** Statistics for Workers. */
    case object TaskRouterWorkspacesWorkersStatisticsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.workers.statistics/list")

    /** Workers represent an entity that is able to perform tasks, such as an agent working in a
      * call center, or a salesperson handling leads.
      */
    case object TaskRouterWorkspacesWorkersAll
        extends PolicyAllow("/twilio/taskrouter/workspaces.workers/*")

    /** Workers represent an entity that is able to perform tasks, such as an agent working in a
      * call center, or a salesperson handling leads.
      */
    case object TaskRouterWorkspacesWorkersRead
        extends PolicyAllow("/twilio/taskrouter/workspaces.workers/read")

    /** Workers represent an entity that is able to perform tasks, such as an agent working in a
      * call center, or a salesperson handling leads.
      */
    case object TaskRouterWorkspacesWorkersList
        extends PolicyAllow("/twilio/taskrouter/workspaces.workers/list")

    /** Workers represent an entity that is able to perform tasks, such as an agent working in a
      * call center, or a salesperson handling leads.
      */
    case object TaskRouterWorkspacesWorkersCreate
        extends PolicyAllow("/twilio/taskrouter/workspaces.workers/create")

    /** Workers represent an entity that is able to perform tasks, such as an agent working in a
      * call center, or a salesperson handling leads.
      */
    case object TaskRouterWorkspacesWorkersUpdate
        extends PolicyAllow("/twilio/taskrouter/workspaces.workers/update")

    /** Workers represent an entity that is able to perform tasks, such as an agent working in a
      * call center, or a salesperson handling leads.
      */
    case object TaskRouterWorkspacesWorkersDelete
        extends PolicyAllow("/twilio/taskrouter/workspaces.workers/delete")

    /** Statistics of all the queues in a workspace. */
    case object TaskRouterWorkspacesTaskQueuesStatisticsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.task-queues.statistics/list")

    /** A Task represents a single item of work waiting to be processed. */
    case object TaskRouterWorkspacesTasksAll
        extends PolicyAllow("/twilio/taskrouter/workspaces.tasks/*")

    /** A Task represents a single item of work waiting to be processed. */
    case object TaskRouterWorkspacesTasksRead
        extends PolicyAllow("/twilio/taskrouter/workspaces.tasks/read")

    /** A Task represents a single item of work waiting to be processed. */
    case object TaskRouterWorkspacesTasksList
        extends PolicyAllow("/twilio/taskrouter/workspaces.tasks/list")

    /** A Task represents a single item of work waiting to be processed. */
    case object TaskRouterWorkspacesTasksCreate
        extends PolicyAllow("/twilio/taskrouter/workspaces.tasks/create")

    /** A Task represents a single item of work waiting to be processed. */
    case object TaskRouterWorkspacesTasksUpdate
        extends PolicyAllow("/twilio/taskrouter/workspaces.tasks/update")

    /** A Task represents a single item of work waiting to be processed. */
    case object TaskRouterWorkspacesTasksDelete
        extends PolicyAllow("/twilio/taskrouter/workspaces.tasks/delete")

    /** Task Queues allow you to categorize Tasks and describe which Workers are eligible to handle
      * those Tasks.
      */
    case object TaskRouterWorkspacesTaskQueuesAll
        extends PolicyAllow("/twilio/taskrouter/workspaces.task-queues/*")

    /** Task Queues allow you to categorize Tasks and describe which Workers are eligible to handle
      * those Tasks.
      */
    case object TaskRouterWorkspacesTaskQueuesRead
        extends PolicyAllow("/twilio/taskrouter/workspaces.task-queues/read")

    /** Task Queues allow you to categorize Tasks and describe which Workers are eligible to handle
      * those Tasks.
      */
    case object TaskRouterWorkspacesTaskQueuesList
        extends PolicyAllow("/twilio/taskrouter/workspaces.task-queues/list")

    /** Task Queues allow you to categorize Tasks and describe which Workers are eligible to handle
      * those Tasks.
      */
    case object TaskRouterWorkspacesTaskQueuesCreate
        extends PolicyAllow("/twilio/taskrouter/workspaces.task-queues/create")

    /** Task Queues allow you to categorize Tasks and describe which Workers are eligible to handle
      * those Tasks.
      */
    case object TaskRouterWorkspacesTaskQueuesUpdate
        extends PolicyAllow("/twilio/taskrouter/workspaces.task-queues/update")

    /** Task Queues allow you to categorize Tasks and describe which Workers are eligible to handle
      * those Tasks.
      */
    case object TaskRouterWorkspacesTaskQueuesDelete
        extends PolicyAllow("/twilio/taskrouter/workspaces.task-queues/delete")

    /** Statistics for a Workflow. */
    case object TaskRouterWorkspacesWorkflowsStatisticsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.workflows.statistics/list")

    /** Cumulative statistics for your Workflow over a certain time period. Cumulative statistics
      * allow you to analyze data from the past 30 days.
      */
    case object TaskRouterWorkspacesWorkflowsCumulativeStatisticsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.workflows.cumulative-statistics/list")

    /** Cumulative statistics for your Workers over a certain time period. Cumulative statistics
      * allow you to analyze Worker data from the past 30 days.
      */
    case object TaskRouterWorkspacesWorkersCumulativeStatisticsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.workers.cumulative-statistics/list")

    /** Instance statistics of your Task Queue. */
    case object TaskRouterWorkspacesTaskQueuesInstanceStatisticsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.task-queues.instance-statistics/list")

    /** Real time statistics for Workers. */
    case object TaskRouterWorkspacesWorkersRealTimeStatisticsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.workers.real-time-statistics/list")

    /** Task Channels provide a mechanism to separate tasks of different types. You can specify
      * different concurrent capacity for tasks of each type.
      */
    case object TaskRouterWorkspacesTaskChannelsAll
        extends PolicyAllow("/twilio/taskrouter/workspaces.task-channels/*")

    /** Task Channels provide a mechanism to separate tasks of different types. You can specify
      * different concurrent capacity for tasks of each type.
      */
    case object TaskRouterWorkspacesTaskChannelsRead
        extends PolicyAllow("/twilio/taskrouter/workspaces.task-channels/read")

    /** Task Channels provide a mechanism to separate tasks of different types. You can specify
      * different concurrent capacity for tasks of each type.
      */
    case object TaskRouterWorkspacesTaskChannelsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.task-channels/list")

    /** Task Channels provide a mechanism to separate tasks of different types. You can specify
      * different concurrent capacity for tasks of each type.
      */
    case object TaskRouterWorkspacesTaskChannelsCreate
        extends PolicyAllow("/twilio/taskrouter/workspaces.task-channels/create")

    /** Task Channels provide a mechanism to separate tasks of different types. You can specify
      * different concurrent capacity for tasks of each type.
      */
    case object TaskRouterWorkspacesTaskChannelsUpdate
        extends PolicyAllow("/twilio/taskrouter/workspaces.task-channels/update")

    /** Task Channels provide a mechanism to separate tasks of different types. You can specify
      * different concurrent capacity for tasks of each type.
      */
    case object TaskRouterWorkspacesTaskChannelsDelete
        extends PolicyAllow("/twilio/taskrouter/workspaces.task-channels/delete")

    /** Statistics for a specific Worker. */
    case object TaskRouterWorkspacesWorkersInstanceStatisticsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.workers.instance-statistics/list")

    /** Worker Channels show the Worker's capacity for handling multiple concurrent Tasks. */
    case object TaskRouterWorkspacesWorkersChannelsRead
        extends PolicyAllow("/twilio/taskrouter/workspaces.workers.channels/read")

    /** Worker Channels show the Worker's capacity for handling multiple concurrent Tasks. */
    case object TaskRouterWorkspacesWorkersChannelsList
        extends PolicyAllow("/twilio/taskrouter/workspaces.workers.channels/list")

    /** Worker Channels show the Worker's capacity for handling multiple concurrent Tasks. */
    case object TaskRouterWorkspacesWorkersChannelsUpdate
        extends PolicyAllow("/twilio/taskrouter/workspaces.workers.channels/update")

    /** Activities describe the current status of your Workers, which determines whether they are
      * eligible to receive task assignments. Workers are always set to a single Activity.
      */
    case object TaskRouterWorkspacesActivitiesAll
        extends PolicyAllow("/twilio/taskrouter/workspaces.activities/*")

    /** Activities describe the current status of your Workers, which determines whether they are
      * eligible to receive task assignments. Workers are always set to a single Activity.
      */
    case object TaskRouterWorkspacesActivitiesRead
        extends PolicyAllow("/twilio/taskrouter/workspaces.activities/read")

    /** Activities describe the current status of your Workers, which determines whether they are
      * eligible to receive task assignments. Workers are always set to a single Activity.
      */
    case object TaskRouterWorkspacesActivitiesList
        extends PolicyAllow("/twilio/taskrouter/workspaces.activities/list")

    /** Activities describe the current status of your Workers, which determines whether they are
      * eligible to receive task assignments. Workers are always set to a single Activity.
      */
    case object TaskRouterWorkspacesActivitiesCreate
        extends PolicyAllow("/twilio/taskrouter/workspaces.activities/create")

    /** Activities describe the current status of your Workers, which determines whether they are
      * eligible to receive task assignments. Workers are always set to a single Activity.
      */
    case object TaskRouterWorkspacesActivitiesUpdate
        extends PolicyAllow("/twilio/taskrouter/workspaces.activities/update")

    /** Activities describe the current status of your Workers, which determines whether they are
      * eligible to receive task assignments. Workers are always set to a single Activity.
      */
    case object TaskRouterWorkspacesActivitiesDelete
        extends PolicyAllow("/twilio/taskrouter/workspaces.activities/delete")

    /** A Workspace is a container for your Tasks, Workers, TaskQueues, Workflows, TaskChannels and
      * Activities.
      */
    case object TaskRouterWorkspacesAll extends PolicyAllow("/twilio/taskrouter/workspaces/*")

    /** A Workspace is a container for your Tasks, Workers, TaskQueues, Workflows, TaskChannels and
      * Activities.
      */
    case object TaskRouterWorkspacesRead extends PolicyAllow("/twilio/taskrouter/workspaces/read")

    /** A Workspace is a container for your Tasks, Workers, TaskQueues, Workflows, TaskChannels and
      * Activities.
      */
    case object TaskRouterWorkspacesList extends PolicyAllow("/twilio/taskrouter/workspaces/list")

    /** A Workspace is a container for your Tasks, Workers, TaskQueues, Workflows, TaskChannels and
      * Activities.
      */
    case object TaskRouterWorkspacesCreate
        extends PolicyAllow("/twilio/taskrouter/workspaces/create")

    /** A Workspace is a container for your Tasks, Workers, TaskQueues, Workflows, TaskChannels and
      * Activities.
      */
    case object TaskRouterWorkspacesUpdate
        extends PolicyAllow("/twilio/taskrouter/workspaces/update")

    /** A Workspace is a container for your Tasks, Workers, TaskQueues, Workflows, TaskChannels and
      * Activities.
      */
    case object TaskRouterWorkspacesDelete
        extends PolicyAllow("/twilio/taskrouter/workspaces/delete")

    /** Query information on a phone number so that you can make a trusted interaction with the
      * user.
      */
    case object LookupPhoneNumbersRead extends PolicyAllow("/twilio/lookup/phone-numbers/read")

    /** OAuth apps generate credentials to access Twilio Account APIs. Currently, we support the
      * client credentials grant type of OAuth 2.0.
      */
    case object IamAccountOauthAppsAll extends PolicyAllow("/twilio/iam/account-oauth-apps/*")

    /** OAuth apps generate credentials to access Twilio Account APIs. Currently, we support the
      * client credentials grant type of OAuth 2.0.
      */
    case object IamAccountOauthAppsRead extends PolicyAllow("/twilio/iam/account-oauth-apps/read")

    /** OAuth apps generate credentials to access Twilio Account APIs. Currently, we support the
      * client credentials grant type of OAuth 2.0.
      */
    case object IamAccountOauthAppsList extends PolicyAllow("/twilio/iam/account-oauth-apps/list")

    /** OAuth apps generate credentials to access Twilio Account APIs. Currently, we support the
      * client credentials grant type of OAuth 2.0.
      */
    case object IamAccountOauthAppsCreate
        extends PolicyAllow("/twilio/iam/account-oauth-apps/create")

    /** OAuth apps generate credentials to access Twilio Account APIs. Currently, we support the
      * client credentials grant type of OAuth 2.0.
      */
    case object IamAccountOauthAppsUpdate
        extends PolicyAllow("/twilio/iam/account-oauth-apps/update")

    /** OAuth apps generate credentials to access Twilio Account APIs. Currently, we support the
      * client credentials grant type of OAuth 2.0.
      */
    case object IamAccountOauthAppsDelete
        extends PolicyAllow("/twilio/iam/account-oauth-apps/delete")

    /** API keys are unique credentials used to authenticate and authorize requests to an API,
      * enabling controlled access to its resources and operations.
      */
    case object IamApiKeysAll extends PolicyAllow("/twilio/iam/api-keys/*")

    /** API keys are unique credentials used to authenticate and authorize requests to an API,
      * enabling controlled access to its resources and operations.
      */
    case object IamApiKeysRead extends PolicyAllow("/twilio/iam/api-keys/read")

    /** API keys are unique credentials used to authenticate and authorize requests to an API,
      * enabling controlled access to its resources and operations.
      */
    case object IamApiKeysList extends PolicyAllow("/twilio/iam/api-keys/list")

    /** API keys are unique credentials used to authenticate and authorize requests to an API,
      * enabling controlled access to its resources and operations.
      */
    case object IamApiKeysCreate extends PolicyAllow("/twilio/iam/api-keys/create")

    /** API keys are unique credentials used to authenticate and authorize requests to an API,
      * enabling controlled access to its resources and operations.
      */
    case object IamApiKeysUpdate extends PolicyAllow("/twilio/iam/api-keys/update")

    /** API keys are unique credentials used to authenticate and authorize requests to an API,
      * enabling controlled access to its resources and operations.
      */
    case object IamApiKeysDelete extends PolicyAllow("/twilio/iam/api-keys/delete")

    /** A public key is a cryptographic code that anyone can use to encrypt data or verify a digital
      * signature, but only the matching private key can decrypt or create the signature.
      */
    case object IamPublicKeysList extends PolicyAllow("/twilio/iam/public-keys/list")

    /** Represents Twilio Accounts. When customers first sign up with Twilio, they have just one
      * main account and they can create more accounts and subaccounts for segmenting phone numbers
      * and usage data for their customers and controlling access to data.
      */
    case object IamAccountsRead extends PolicyAllow("/twilio/iam/accounts/read")

    /** Represents Twilio Accounts. When customers first sign up with Twilio, they have just one
      * main account and they can create more accounts and subaccounts for segmenting phone numbers
      * and usage data for their customers and controlling access to data.
      */
    case object IamAccountsList extends PolicyAllow("/twilio/iam/accounts/list")

    override val values: IndexedSeq[PolicyAllow] = findValues
  }
}
