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
  def policyAllowOpt: Option[Set[ApiKey.ApiKeyPolicy]]

  def withFlags(flags: Set[ApiKey.Flag]): ApiKey with ApiKey.HasFlags
  def withSecret(secret: ApiKey.Secret): ApiKey with ApiKey.HasSecret
  def withPolicyAllow(policyAllow: Set[ApiKey.ApiKeyPolicy]): ApiKey with ApiKey.HasPolicyAllow

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
    def withPolicyAllow(policyAllow: Set[ApiKey.ApiKeyPolicy]): ApiKey with HasFlagsAndPolicyAllow
  }

  sealed trait HasSecret { self: ApiKey =>
    def secret: ApiKey.Secret
    def withSecret(secret: ApiKey.Secret): ApiKey with HasSecret
    def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndSecret
    def withPolicyAllow(policyAllow: Set[ApiKey.ApiKeyPolicy]): ApiKey with HasSecretAndPolicyAllow
  }

  sealed trait HasPolicyAllow { self: ApiKey =>
    def policyAllow: Set[ApiKey.ApiKeyPolicy]
    def withPolicyAllow(policyAllow: Set[ApiKey.ApiKeyPolicy]): ApiKey with HasPolicyAllow
    def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndPolicyAllow
    def withSecret(secret: ApiKey.Secret): ApiKey with HasSecretAndPolicyAllow
  }

  sealed trait HasFlagsAndSecret extends HasFlags with HasSecret { self: ApiKey =>
    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndSecret
    override def withSecret(secret: ApiKey.Secret): ApiKey with HasFlagsAndSecret
    override def withPolicyAllow(policyAllow: Set[ApiKey.ApiKeyPolicy]): ApiKey with HasAll
  }

  sealed trait HasFlagsAndPolicyAllow extends HasFlags with HasPolicyAllow { self: ApiKey =>
    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndPolicyAllow
    override def withPolicyAllow(
        policyAllow: Set[ApiKey.ApiKeyPolicy]
    ): ApiKey with HasFlagsAndPolicyAllow
    override def withSecret(secret: ApiKey.Secret): ApiKey with HasAll
  }

  sealed trait HasSecretAndPolicyAllow extends HasSecret with HasPolicyAllow { self: ApiKey =>
    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecretAndPolicyAllow
    override def withPolicyAllow(
        policyAllow: Set[ApiKey.ApiKeyPolicy]
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
    override def withPolicyAllow(policyAllow: Set[ApiKey.ApiKeyPolicy]): ApiKey with HasAll
  }

  private final class ApiKeyBase(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName
  ) extends ApiKey {
    override val secretOpt: Option[ApiKey.Secret]                 = None
    override val flagsOpt: Option[Set[ApiKey.Flag]]               = None
    override val policyAllowOpt: Option[Set[ApiKey.ApiKeyPolicy]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlags =
      new ApiKeyWithFlags(sid, friendlyName, flags)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecret =
      new ApiKeyWithSecret(sid, friendlyName, secret)

    override def withPolicyAllow(
        policyAllow: Set[ApiKey.ApiKeyPolicy]
    ): ApiKey with HasPolicyAllow =
      new ApiKeyWithPolicyAllow(sid, friendlyName, policyAllow)
  }

  private final class ApiKeyWithSecret(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val secret: ApiKey.Secret
  ) extends ApiKey
      with HasSecret {
    override val secretOpt: Option[ApiKey.Secret]                 = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]]               = None
    override val policyAllowOpt: Option[Set[ApiKey.ApiKeyPolicy]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecret =
      new ApiKeyWithSecret(sid, friendlyName, secret)

    override def withPolicyAllow(
        policyAllow: Set[ApiKey.ApiKeyPolicy]
    ): ApiKey with HasSecretAndPolicyAllow =
      new ApiKeyWithSecretAndPolicyAllow(sid, friendlyName, secret, policyAllow)
  }

  private final class ApiKeyWithFlags(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val flags: Set[ApiKey.Flag]
  ) extends ApiKey
      with HasFlags {
    override val secretOpt: Option[ApiKey.Secret]                 = None
    override val flagsOpt: Option[Set[ApiKey.Flag]]               = Some(flags)
    override val policyAllowOpt: Option[Set[ApiKey.ApiKeyPolicy]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlags =
      new ApiKeyWithFlags(sid, friendlyName, flags)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasFlagsAndSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags)

    override def withPolicyAllow(
        policyAllow: Set[ApiKey.ApiKeyPolicy]
    ): ApiKey with HasFlagsAndPolicyAllow =
      new ApiKeyWithFlagsAndPolicyAllow(sid, friendlyName, flags, policyAllow)
  }

  private final class ApiKeyWithPolicyAllow(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val policyAllow: Set[ApiKey.ApiKeyPolicy]
  ) extends ApiKey
      with HasPolicyAllow {
    override val secretOpt: Option[ApiKey.Secret]                 = None
    override val flagsOpt: Option[Set[ApiKey.Flag]]               = None
    override val policyAllowOpt: Option[Set[ApiKey.ApiKeyPolicy]] = Some(policyAllow)

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndPolicyAllow =
      new ApiKeyWithFlagsAndPolicyAllow(sid, friendlyName, flags, policyAllow)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecretAndPolicyAllow =
      new ApiKeyWithSecretAndPolicyAllow(sid, friendlyName, secret, policyAllow)

    override def withPolicyAllow(
        policyAllow: Set[ApiKey.ApiKeyPolicy]
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
    override val secretOpt: Option[ApiKey.Secret]                 = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]]               = Some(flags)
    override val policyAllowOpt: Option[Set[ApiKey.ApiKeyPolicy]] = None

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasFlagsAndSecret =
      new ApiKeyWithSecretAndFlags(sid, friendlyName, secret, flags)

    override def withPolicyAllow(
        policyAllow: Set[ApiKey.ApiKeyPolicy]
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(sid, friendlyName, secret, flags, policyAllow)
  }

  private final class ApiKeyWithSecretAndPolicyAllow(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val secret: ApiKey.Secret,
      val policyAllow: Set[ApiKey.ApiKeyPolicy]
  ) extends ApiKey
      with HasSecretAndPolicyAllow {
    override val secretOpt: Option[ApiKey.Secret]                 = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]]               = None
    override val policyAllowOpt: Option[Set[ApiKey.ApiKeyPolicy]] = Some(policyAllow)

    override def withFlags(
        flags: Set[ApiKey.Flag]
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(sid, friendlyName, secret, flags, policyAllow)

    override def withSecret(secret: ApiKey.Secret): ApiKey with HasSecretAndPolicyAllow =
      new ApiKeyWithSecretAndPolicyAllow(sid, friendlyName, secret, policyAllow)

    override def withPolicyAllow(
        policyAllow: Set[ApiKey.ApiKeyPolicy]
    ): ApiKey with HasSecretAndPolicyAllow =
      new ApiKeyWithSecretAndPolicyAllow(sid, friendlyName, secret, policyAllow)
  }

  private final class ApiKeyWithFlagsAndPolicyAllow(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val flags: Set[ApiKey.Flag],
      val policyAllow: Set[ApiKey.ApiKeyPolicy]
  ) extends ApiKey
      with HasFlagsAndPolicyAllow {
    override val secretOpt: Option[ApiKey.Secret]                 = None
    override val flagsOpt: Option[Set[ApiKey.Flag]]               = Some(flags)
    override val policyAllowOpt: Option[Set[ApiKey.ApiKeyPolicy]] = Some(policyAllow)

    override def withFlags(flags: Set[ApiKey.Flag]): ApiKey with HasFlagsAndPolicyAllow =
      new ApiKeyWithFlagsAndPolicyAllow(sid, friendlyName, flags, policyAllow)

    override def withSecret(
        secret: ApiKey.Secret
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(sid, friendlyName, secret, flags, policyAllow)

    override def withPolicyAllow(
        policyAllow: Set[ApiKey.ApiKeyPolicy]
    ): ApiKey with HasFlagsAndPolicyAllow =
      new ApiKeyWithFlagsAndPolicyAllow(sid, friendlyName, flags, policyAllow)
  }

  private final class ApiKeyWithSecretAndFlagsAndPolicyAllow(
      val sid: ApiKey.Sid,
      val friendlyName: ApiKey.FriendlyName,
      val secret: ApiKey.Secret,
      val flags: Set[ApiKey.Flag],
      val policyAllow: Set[ApiKey.ApiKeyPolicy]
  ) extends ApiKey
      with HasAll {
    override val secretOpt: Option[ApiKey.Secret]                 = Some(secret)
    override val flagsOpt: Option[Set[ApiKey.Flag]]               = Some(flags)
    override val policyAllowOpt: Option[Set[ApiKey.ApiKeyPolicy]] = Some(policyAllow)

    override def withFlags(
        flags: Set[ApiKey.Flag]
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(sid, friendlyName, secret, flags, policyAllow)

    override def withSecret(
        secret: ApiKey.Secret
    ): ApiKey with HasAll =
      new ApiKeyWithSecretAndFlagsAndPolicyAllow(sid, friendlyName, secret, flags, policyAllow)

    override def withPolicyAllow(
        policyAllow: Set[ApiKey.ApiKeyPolicy]
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
  sealed abstract class ApiKeyPolicy(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object ApiKeyPolicy extends EnumWithTwilioString[ApiKeyPolicy] {

    /** Describes the individual URI entries that make up the BYOC Origination ConnectionPolicies
      * list.
      */
    case object SipConnectionPoliciesTargetsAll
        extends ApiKeyPolicy("/twilio/voice/sip.connection-policies.targets/*")

    /** Annotate calls to provide subjective experience details. */
    case object InsightsCallAnnotationsUpdate
        extends ApiKeyPolicy("/twilio/voice/insights.call.annotations/update")

    /** The OperatorType resource represents the Type of a Prebuilt or Custom Operator. */
    case object IntelligenceOperatorTypesList
        extends ApiKeyPolicy("/twilio/voice/intelligence.operator-types/list")

    /** The OperatorType resource represents the Type of a Prebuilt or Custom Operator. */
    case object IntelligenceOperatorTypesRead
        extends ApiKeyPolicy("/twilio/voice/intelligence.operator-types/read")

    /** The Notifications subresource on any given Call. */
    case object CallsNotificationsRead
        extends ApiKeyPolicy("/twilio/voice/calls.notifications/read")

    /** Represents the transcribed text and metadata from a transcribed recording of a voice call.
      */
    case object TranscriptionsList extends ApiKeyPolicy("/twilio/voice/transcriptions/list")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipTrunksAuthCallsCredentialListMappingsList
        extends ApiKeyPolicy("/twilio/voice/sip.trunks.auth.calls.credential-list-mappings/list")

    /** IpAccessControlListMapping resources contain the list of IpAccessControlList resources
      * associated with this domain.
      */
    case object SipIpAclMappingsRead extends ApiKeyPolicy("/twilio/voice/sip.ip-acl-mappings/read")

    /** Represents a single verified number that may be used as a caller ID when making outgoing
      * calls.
      */
    case object OutgoingCallerIdsAll extends ApiKeyPolicy("/twilio/voice/outgoing-caller-ids/*")

    /** The Notifications subresource on any given Call. */
    case object CallsNotificationsList
        extends ApiKeyPolicy("/twilio/voice/calls.notifications/list")

    /** Describes a call queue that contains individual calls, which are described by the queue's
      * Member resources.
      */
    case object QueuesAll extends ApiKeyPolicy("/twilio/voice/queues/*")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipTrunksAuthCallsCredentialListMappingsRead
        extends ApiKeyPolicy("/twilio/voice/sip.trunks.auth.calls.credential-list-mappings/read")

    /** The OriginationUrl Resource represents the Origination SIP URL(s) of your Trunk. */
    case object SipTrunksOriginationUrlsAll
        extends ApiKeyPolicy("/twilio/voice/sip.trunks.origination-urls/*")

    /** Represents the transcribed text and metadata from a transcribed recording of a voice call.
      */
    case object TranscriptionsRead extends ApiKeyPolicy("/twilio/voice/transcriptions/read")

    /** Exposes the multiple types of Payloads that may be in included in an Add-on Result. */
    case object RecordingsAddOnsPayloadList
        extends ApiKeyPolicy("/twilio/voice/recordings.add-ons.payload/list")

    /** Represents the transcribed text and metadata from a transcribed recording of a voice call.
      */
    case object RecordingsTranscriptionsRead
        extends ApiKeyPolicy("/twilio/voice/recordings.transcriptions/read")

    /** A Transcript resource represents a voice conversation that has automatically been converted
      * to text through Voice Intelligence.
      */
    case object IntelligenceTranscriptsDelete
        extends ApiKeyPolicy("/twilio/voice/intelligence.transcripts/delete")

    /** Represents the transcribed text and metadata from a transcribed recording of a voice call.
      */
    case object RecordingsTranscriptionsList
        extends ApiKeyPolicy("/twilio/voice/recordings.transcriptions/list")

    /** The CustomOperator subresource of the Operator resource represents a Custom Operator. */
    case object IntelligenceOperatorCustomAll
        extends ApiKeyPolicy("/twilio/voice/intelligence.operator.custom/*")

    /** Represents the CredentialList resources associated with a SIP Domain. */
    case object SipDomainsAuthCallsCredentialListMappingsCreate
        extends ApiKeyPolicy("/twilio/voice/sip.domains.auth.calls.credential-list-mappings/create")

    /** Subresource represents the CredentialList instances associated with this domain's
      * registration.
      */
    case object SipDomainsAuthRegistrationsCredentialListMappingsList
        extends ApiKeyPolicy(
          "/twilio/voice/sip.domains.auth.registrations.credential-list-mappings/list"
        )

    /** OperatorAttachment represents the link between a specific Prebuilt or Custom Operator and a
      * specific Voice Intelligence Service.
      */
    case object IntelligenceOperatorAttachmentDelete
        extends ApiKeyPolicy("/twilio/voice/intelligence.operator-attachment/delete")

    /** A Transcript resource represents a voice conversation that has automatically been converted
      * to text through Voice Intelligence.
      */
    case object IntelligenceTranscriptsRead
        extends ApiKeyPolicy("/twilio/voice/intelligence.transcripts/read")

    /** IpAccessControlListMapping resources contain the list of IpAccessControlList resources
      * associated with this domain.
      */
    case object SipTrunksAuthCallsIpAclMappingsDelete
        extends ApiKeyPolicy("/twilio/voice/sip.trunks.auth.calls.ip-acl-mappings/delete")

    /** Subresource represents the CredentialList instances associated with this domain's
      * registration.
      */
    case object SipDomainsAuthRegistrationsCredentialListMappingsRead
        extends ApiKeyPolicy(
          "/twilio/voice/sip.domains.auth.registrations.credential-list-mappings/read"
        )

    /** IpAccessControlListMapping resources contain the list of IpAccessControlList resources
      * associated with this domain.
      */
    case object SipIpAclMappingsDelete
        extends ApiKeyPolicy("/twilio/voice/sip.ip-acl-mappings/delete")

    /** A Transcript resource represents a voice conversation that has automatically been converted
      * to text through Voice Intelligence.
      */
    case object IntelligenceTranscriptsList
        extends ApiKeyPolicy("/twilio/voice/intelligence.transcripts/list")

    /** Exposes the multiple types of Payloads that may be in included in an Add-on Result. */
    case object RecordingsAddOnsPayloadRead
        extends ApiKeyPolicy("/twilio/voice/recordings.add-ons.payload/read")

    /** A Transcript resource represents a voice conversation that has automatically been converted
      * to text through Voice Intelligence.
      */
    case object IntelligenceTranscriptsCreate
        extends ApiKeyPolicy("/twilio/voice/intelligence.transcripts/create")

    /** Contain the credentials resource entry of the users who are allowed to reach your SIP
      * Domain.
      */
    case object SipCredentialListsCredentialsAll
        extends ApiKeyPolicy("/twilio/voice/sip.credential-lists.credentials/*")

    /** The Conference resource allows you to query and manage the state of conferences on your
      * Twilio account.
      */
    case object ConferencesUpdate extends ApiKeyPolicy("/twilio/voice/conferences/update")

    /** Represents the CredentialList resources associated with a SIP Domain. */
    case object SipDomainsAuthCallsCredentialListMappingsDelete
        extends ApiKeyPolicy("/twilio/voice/sip.domains.auth.calls.credential-list-mappings/delete")

    /** IpAccessControlListMapping resources contain the list of IpAccessControlList resources
      * associated with this domain.
      */
    case object SipIpAclMappingsList extends ApiKeyPolicy("/twilio/voice/sip.ip-acl-mappings/list")

    /** Describes the IP addresses that have access to the SIP Domain. */
    case object SipIpAclsIpAddressesRead
        extends ApiKeyPolicy("/twilio/voice/sip.ip-acls.ip-addresses/read")

    /** Conference summaries with events and metadata. */
    case object InsightsConferenceSummariesRead
        extends ApiKeyPolicy("/twilio/voice/insights.conference.summaries/read")

    /** A Transcript Sentence is the actual text of the recording transcription. */
    case object IntelligenceTranscriptSentencesRead
        extends ApiKeyPolicy("/twilio/voice/intelligence.transcript-sentences/read")

    /** Describes the publicly-routable Static IP addresses that can be used to receive Termination
      * traffic from a BYOC Carrier.
      */
    case object SipSourceIpMappingsAll
        extends ApiKeyPolicy("/twilio/voice/sip.source-ip-mappings/*")

    /** Each Conference has a Participants subresource. Participants represent the set of people
      * currently connected to a running conference.
      */
    case object ConferencesParticipantsAll
        extends ApiKeyPolicy("/twilio/voice/conferences.participants/*")

    /** Describes the IP addresses that have access to the SIP Domain. */
    case object SipIpAclsIpAddressesList
        extends ApiKeyPolicy("/twilio/voice/sip.ip-acls.ip-addresses/list")

    /** Conference summaries with events and metadata. */
    case object InsightsConferenceSummariesList
        extends ApiKeyPolicy("/twilio/voice/insights.conference.summaries/list")

    /** Provides call progress and quality-related Voice SDK events data for a specific call. */
    case object InsightsCallEventsList
        extends ApiKeyPolicy("/twilio/voice/insights.call.events/list")

    /** Subresource of the Queue resource and represents a single call in a call queue. */
    case object QueuesMemberUpdate extends ApiKeyPolicy("/twilio/voice/queues.member/update")

    /** Describes a trunk that can be configured to send/receive traffic to/from a PSTN Carrier. */
    case object SipByocTrunksAll extends ApiKeyPolicy("/twilio/voice/sip.byoc-trunks/*")

    /** Represents the recording associated with a voice call, conference, or SIP Trunk. */
    case object RecordingsRead extends ApiKeyPolicy("/twilio/voice/recordings/read")

    /** An object that represents a connection between a telephone and Twilio. */
    case object CallsAll extends ApiKeyPolicy("/twilio/voice/calls/*")

    /** Represents the transcribed text and metadata from a transcribed recording of a voice call.
      */
    case object RecordingsTranscriptionsDelete
        extends ApiKeyPolicy("/twilio/voice/recordings.transcriptions/delete")

    /** Represents the transcribed text and metadata from a transcribed recording of a voice call.
      */
    case object TranscriptionsDelete extends ApiKeyPolicy("/twilio/voice/transcriptions/delete")

    /** Represents the recording associated with a voice call, conference, or SIP Trunk. */
    case object RecordingsList extends ApiKeyPolicy("/twilio/voice/recordings/list")

    /** OperatorAttachment represents the link between a specific Prebuilt or Custom Operator and a
      * specific Voice Intelligence Service.
      */
    case object IntelligenceOperatorAttachmentCreate
        extends ApiKeyPolicy("/twilio/voice/intelligence.operator-attachment/create")

    /** Describes a list of URI Entries that are used to route Origination traffic to a PSTN Carrier
      * over a BYOC Trunk.
      */
    case object SipConnectionPoliciesAll
        extends ApiKeyPolicy("/twilio/voice/sip.connection-policies/*")

    /** Conference participant summaries with events and metadata for individual participants. */
    case object InsightsConferenceParticipantsRead
        extends ApiKeyPolicy("/twilio/voice/insights.conference.participants/read")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipTrunksAuthCallsCredentialListMappingsDelete
        extends ApiKeyPolicy("/twilio/voice/sip.trunks.auth.calls.credential-list-mappings/delete")

    /** IpAccessControlListMapping resources contain the list of IpAccessControlList resources
      * associated with this domain.
      */
    case object SipTrunksAuthCallsIpAclMappingsCreate
        extends ApiKeyPolicy("/twilio/voice/sip.trunks.auth.calls.ip-acl-mappings/create")

    /** Control Voice Insights Advanced Features and Voice Trace status for an account. */
    case object InsightsSettingsUpdate
        extends ApiKeyPolicy("/twilio/voice/insights.settings/update")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipCredentialListsAll extends ApiKeyPolicy("/twilio/voice/sip.credential-lists/*")

    /** Add-on results live as a subresource under the integration point that the Add-on was
      * configured with.
      */
    case object RecordingsAddOnsDelete
        extends ApiKeyPolicy("/twilio/voice/recordings.add-ons/delete")

    /** Subresource represents the CredentialList instances associated with this domain's
      * registration.
      */
    case object SipDomainsAuthRegistrationsCredentialListMappingsDelete
        extends ApiKeyPolicy(
          "/twilio/voice/sip.domains.auth.registrations.credential-list-mappings/delete"
        )

    /** The PrebuiltOperator subresource of the Operator resource represents a Prebuilt Operator. */
    case object IntelligenceOperatorPrebuiltList
        extends ApiKeyPolicy("/twilio/voice/intelligence.operator.prebuilt/list")

    /** Subresource of the Queue resource and represents a single call in a call queue. */
    case object QueuesMemberList extends ApiKeyPolicy("/twilio/voice/queues.member/list")

    /** Elastic SIP Trunking enables you to make & receive telephone calls from your IP
      * communications infrastructure.
      */
    case object SipTrunksAll extends ApiKeyPolicy("/twilio/voice/sip.trunks/*")

    /** The Recordings subresource on any given Conference. */
    case object ConferencesRecordingsUpdate
        extends ApiKeyPolicy("/twilio/voice/conferences.recordings/update")

    /** Voice twiml apps. */
    case object TwimlAppsAll extends ApiKeyPolicy("/twilio/voice/twiml.apps/*")

    /** Subresource of the Queue resource and represents a single call in a call queue. */
    case object QueuesMemberRead extends ApiKeyPolicy("/twilio/voice/queues.member/read")

    /** Annotate calls to provide subjective experience details. */
    case object InsightsCallAnnotationsRead
        extends ApiKeyPolicy("/twilio/voice/insights.call.annotations/read")

    /** Provides quality-related metrics for a specific call. */
    case object InsightsCallMetricsList
        extends ApiKeyPolicy("/twilio/voice/insights.call.metrics/list")

    /** A Service provides control and configuration for how Transcripts are processed. */
    case object IntelligenceServicesAll
        extends ApiKeyPolicy("/twilio/voice/intelligence.services/*")

    /** IpAccessControlListMapping resources contain the list of IpAccessControlList resources
      * associated with this domain.
      */
    case object SipIpAclMappingsCreate
        extends ApiKeyPolicy("/twilio/voice/sip.ip-acl-mappings/create")

    /** Describes the IP addresses that have access to the SIP Domain. */
    case object SipIpAclsIpAddressesUpdate
        extends ApiKeyPolicy("/twilio/voice/sip.ip-acls.ip-addresses/update")

    /** Emergency Address associated with a Twilio number. */
    case object SipEmergencyAddressesDelete
        extends ApiKeyPolicy("/twilio/voice/sip.emergency-addresses/delete")

    /** Describes the IP addresses that have access to the SIP Domain. */
    case object SipIpAclsIpAddressesCreate
        extends ApiKeyPolicy("/twilio/voice/sip.ip-acls.ip-addresses/create")

    /** The Operator resource represents a Prebuilt or Custom Operator. */
    case object IntelligenceOperatorsRead
        extends ApiKeyPolicy("/twilio/voice/intelligence.operators/read")

    /** IpAccessControlListMapping resources contain the list of IpAccessControlList resources
      * associated with this domain.
      */
    case object SipTrunksAuthCallsIpAclMappingsList
        extends ApiKeyPolicy("/twilio/voice/sip.trunks.auth.calls.ip-acl-mappings/list")

    /** OperatorAttachment represents the link between a specific Prebuilt or Custom Operator and a
      * specific Voice Intelligence Service.
      */
    case object IntelligenceOperatorAttachmentList
        extends ApiKeyPolicy("/twilio/voice/intelligence.operator-attachment/list")

    /** Represents the CredentialList resources associated with a SIP Domain. */
    case object SipDomainsAuthCallsCredentialListMappingsList
        extends ApiKeyPolicy("/twilio/voice/sip.domains.auth.calls.credential-list-mappings/list")

    /** Represents the recording associated with a voice call, conference, or SIP Trunk. */
    case object RecordingsDelete extends ApiKeyPolicy("/twilio/voice/recordings/delete")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipTrunksAuthCallsCredentialListMappingsCreate
        extends ApiKeyPolicy("/twilio/voice/sip.trunks.auth.calls.credential-list-mappings/create")

    /** Add-on results live as a subresource under the integration point that the Add-on was
      * configured with.
      */
    case object RecordingsAddOnsRead extends ApiKeyPolicy("/twilio/voice/recordings.add-ons/read")

    /** Call Event resource. */
    case object RequestInspectorRead extends ApiKeyPolicy("/twilio/voice/request-inspector/read")

    /** Emergency Address associated with a Twilio number. */
    case object SipEmergencyAddressesCreate
        extends ApiKeyPolicy("/twilio/voice/sip.emergency-addresses/create")

    /** Describes a custom DNS hostname that can accept SIP traffic for your account. */
    case object SipDomainsAll extends ApiKeyPolicy("/twilio/voice/sip.domains/*")

    /** The Operator resource represents a Prebuilt or Custom Operator. */
    case object IntelligenceOperatorsList
        extends ApiKeyPolicy("/twilio/voice/intelligence.operators/list")

    /** A Transcript Media returns a signed URL for the Media (call recording) corresponding to the
      * Transcript.
      */
    case object IntelligenceTranscriptMediaRead
        extends ApiKeyPolicy("/twilio/voice/intelligence.transcript-media/read")

    /** Hosts the actual data returned by the Add-on. */
    case object RecordingsAddOnsPayloadDataRead
        extends ApiKeyPolicy("/twilio/voice/recordings.add-ons.payload.data/read")

    /** The Recordings subresource on any given Conference. */
    case object ConferencesRecordingsList
        extends ApiKeyPolicy("/twilio/voice/conferences.recordings/list")

    /** IpAccessControlListMapping resources contain the list of IpAccessControlList resources
      * associated with this domain.
      */
    case object SipTrunksAuthCallsIpAclMappingsRead
        extends ApiKeyPolicy("/twilio/voice/sip.trunks.auth.calls.ip-acl-mappings/read")

    /** Represents the CredentialList resources associated with a SIP Domain. */
    case object SipDomainsAuthCallsCredentialListMappingsRead
        extends ApiKeyPolicy("/twilio/voice/sip.domains.auth.calls.credential-list-mappings/read")

    /** The PrebuiltOperator subresource of the Operator resource represents a Prebuilt Operator. */
    case object IntelligenceOperatorPrebuiltRead
        extends ApiKeyPolicy("/twilio/voice/intelligence.operator.prebuilt/read")

    /** The Recordings subresource on any given Call. */
    case object CallsRecordingsAll extends ApiKeyPolicy("/twilio/voice/calls.recordings/*")

    /** Conference participant summaries with events and metadata for individual participants. */
    case object InsightsConferenceParticipantsList
        extends ApiKeyPolicy("/twilio/voice/insights.conference.participants/list")

    /** The OperatorResults resource returns a list of operator inferences for a Transcript. */
    case object IntelligenceOperatorResultsRead
        extends ApiKeyPolicy("/twilio/voice/intelligence.operator-results/read")

    /** A call summary for calls. */
    case object InsightsCallSummariesRead
        extends ApiKeyPolicy("/twilio/voice/insights.call.summaries/read")

    /** Describes Static IP addresses used to address the BYOC Trunk's Termination SIP Domain via an
      * IP Address instead of an FQDN.
      */
    case object SipIpRecordsAll extends ApiKeyPolicy("/twilio/voice/sip.ip-records/*")

    /** Add-on results live as a subresource under the integration point that the Add-on was
      * configured with.
      */
    case object RecordingsAddOnsList extends ApiKeyPolicy("/twilio/voice/recordings.add-ons/list")

    /** A call summary for calls. */
    case object InsightsCallSummariesList
        extends ApiKeyPolicy("/twilio/voice/insights.call.summaries/list")

    /** Subresource represents the CredentialList instances associated with this domain's
      * registration.
      */
    case object SipDomainsAuthRegistrationsCredentialListMappingsCreate
        extends ApiKeyPolicy(
          "/twilio/voice/sip.domains.auth.registrations.credential-list-mappings/create"
        )

    /** Control Voice Insights Advanced Features and Voice Trace status for an account. */
    case object InsightsSettingsRead extends ApiKeyPolicy("/twilio/voice/insights.settings/read")

    /** IpAccessControlList resources contain the Access Control List (ACL). */
    case object SipIpAclsAll extends ApiKeyPolicy("/twilio/voice/sip.ip-acls/*")

    /** The Conference resource allows you to query and manage the state of conferences on your
      * Twilio account.
      */
    case object ConferencesList extends ApiKeyPolicy("/twilio/voice/conferences/list")

    /** The Conference resource allows you to query and manage the state of conferences on your
      * Twilio account.
      */
    case object ConferencesRead extends ApiKeyPolicy("/twilio/voice/conferences/read")

    /** Represents a single verified number that may be used as a caller ID when making outgoing
      * calls.
      */
    case object OutgoingCallerIdsRead extends ApiKeyPolicy("/twilio/voice/outgoing-caller-ids/read")

    /** Represents a single verified number that may be used as a caller ID when making outgoing
      * calls.
      */
    case object OutgoingCallerIdsList extends ApiKeyPolicy("/twilio/voice/outgoing-caller-ids/list")

    /** Represents a single verified number that may be used as a caller ID when making outgoing
      * calls.
      */
    case object OutgoingCallerIdsCreate
        extends ApiKeyPolicy("/twilio/voice/outgoing-caller-ids/create")

    /** Represents a single verified number that may be used as a caller ID when making outgoing
      * calls.
      */
    case object OutgoingCallerIdsUpdate
        extends ApiKeyPolicy("/twilio/voice/outgoing-caller-ids/update")

    /** Represents a single verified number that may be used as a caller ID when making outgoing
      * calls.
      */
    case object OutgoingCallerIdsDelete
        extends ApiKeyPolicy("/twilio/voice/outgoing-caller-ids/delete")

    /** Describes a call queue that contains individual calls, which are described by the queue's
      * Member resources.
      */
    case object QueuesRead extends ApiKeyPolicy("/twilio/voice/queues/read")

    /** Describes a call queue that contains individual calls, which are described by the queue's
      * Member resources.
      */
    case object QueuesList extends ApiKeyPolicy("/twilio/voice/queues/list")

    /** Describes a call queue that contains individual calls, which are described by the queue's
      * Member resources.
      */
    case object QueuesCreate extends ApiKeyPolicy("/twilio/voice/queues/create")

    /** Describes a call queue that contains individual calls, which are described by the queue's
      * Member resources.
      */
    case object QueuesUpdate extends ApiKeyPolicy("/twilio/voice/queues/update")

    /** Describes a call queue that contains individual calls, which are described by the queue's
      * Member resources.
      */
    case object QueuesDelete extends ApiKeyPolicy("/twilio/voice/queues/delete")

    /** The OriginationUrl Resource represents the Origination SIP URL(s) of your Trunk. */
    case object SipTrunksOriginationUrlsRead
        extends ApiKeyPolicy("/twilio/voice/sip.trunks.origination-urls/read")

    /** The OriginationUrl Resource represents the Origination SIP URL(s) of your Trunk. */
    case object SipTrunksOriginationUrlsList
        extends ApiKeyPolicy("/twilio/voice/sip.trunks.origination-urls/list")

    /** The OriginationUrl Resource represents the Origination SIP URL(s) of your Trunk. */
    case object SipTrunksOriginationUrlsCreate
        extends ApiKeyPolicy("/twilio/voice/sip.trunks.origination-urls/create")

    /** The OriginationUrl Resource represents the Origination SIP URL(s) of your Trunk. */
    case object SipTrunksOriginationUrlsUpdate
        extends ApiKeyPolicy("/twilio/voice/sip.trunks.origination-urls/update")

    /** The OriginationUrl Resource represents the Origination SIP URL(s) of your Trunk. */
    case object SipTrunksOriginationUrlsDelete
        extends ApiKeyPolicy("/twilio/voice/sip.trunks.origination-urls/delete")

    /** The CustomOperator subresource of the Operator resource represents a Custom Operator. */
    case object IntelligenceOperatorCustomRead
        extends ApiKeyPolicy("/twilio/voice/intelligence.operator.custom/read")

    /** The CustomOperator subresource of the Operator resource represents a Custom Operator. */
    case object IntelligenceOperatorCustomList
        extends ApiKeyPolicy("/twilio/voice/intelligence.operator.custom/list")

    /** The CustomOperator subresource of the Operator resource represents a Custom Operator. */
    case object IntelligenceOperatorCustomCreate
        extends ApiKeyPolicy("/twilio/voice/intelligence.operator.custom/create")

    /** The CustomOperator subresource of the Operator resource represents a Custom Operator. */
    case object IntelligenceOperatorCustomUpdate
        extends ApiKeyPolicy("/twilio/voice/intelligence.operator.custom/update")

    /** The CustomOperator subresource of the Operator resource represents a Custom Operator. */
    case object IntelligenceOperatorCustomDelete
        extends ApiKeyPolicy("/twilio/voice/intelligence.operator.custom/delete")

    /** Contain the credentials resource entry of the users who are allowed to reach your SIP
      * Domain.
      */
    case object SipCredentialListsCredentialsRead
        extends ApiKeyPolicy("/twilio/voice/sip.credential-lists.credentials/read")

    /** Contain the credentials resource entry of the users who are allowed to reach your SIP
      * Domain.
      */
    case object SipCredentialListsCredentialsList
        extends ApiKeyPolicy("/twilio/voice/sip.credential-lists.credentials/list")

    /** Contain the credentials resource entry of the users who are allowed to reach your SIP
      * Domain.
      */
    case object SipCredentialListsCredentialsCreate
        extends ApiKeyPolicy("/twilio/voice/sip.credential-lists.credentials/create")

    /** Contain the credentials resource entry of the users who are allowed to reach your SIP
      * Domain.
      */
    case object SipCredentialListsCredentialsUpdate
        extends ApiKeyPolicy("/twilio/voice/sip.credential-lists.credentials/update")

    /** Contain the credentials resource entry of the users who are allowed to reach your SIP
      * Domain.
      */
    case object SipCredentialListsCredentialsDelete
        extends ApiKeyPolicy("/twilio/voice/sip.credential-lists.credentials/delete")

    /** Describes the publicly-routable Static IP addresses that can be used to receive Termination
      * traffic from a BYOC Carrier.
      */
    case object SipSourceIpMappingsRead
        extends ApiKeyPolicy("/twilio/voice/sip.source-ip-mappings/read")

    /** Describes the publicly-routable Static IP addresses that can be used to receive Termination
      * traffic from a BYOC Carrier.
      */
    case object SipSourceIpMappingsList
        extends ApiKeyPolicy("/twilio/voice/sip.source-ip-mappings/list")

    /** Describes the publicly-routable Static IP addresses that can be used to receive Termination
      * traffic from a BYOC Carrier.
      */
    case object SipSourceIpMappingsCreate
        extends ApiKeyPolicy("/twilio/voice/sip.source-ip-mappings/create")

    /** Describes the publicly-routable Static IP addresses that can be used to receive Termination
      * traffic from a BYOC Carrier.
      */
    case object SipSourceIpMappingsUpdate
        extends ApiKeyPolicy("/twilio/voice/sip.source-ip-mappings/update")

    /** Describes the publicly-routable Static IP addresses that can be used to receive Termination
      * traffic from a BYOC Carrier.
      */
    case object SipSourceIpMappingsDelete
        extends ApiKeyPolicy("/twilio/voice/sip.source-ip-mappings/delete")

    /** Each Conference has a Participants subresource. Participants represent the set of people
      * currently connected to a running conference.
      */
    case object ConferencesParticipantsRead
        extends ApiKeyPolicy("/twilio/voice/conferences.participants/read")

    /** Each Conference has a Participants subresource. Participants represent the set of people
      * currently connected to a running conference.
      */
    case object ConferencesParticipantsList
        extends ApiKeyPolicy("/twilio/voice/conferences.participants/list")

    /** Each Conference has a Participants subresource. Participants represent the set of people
      * currently connected to a running conference.
      */
    case object ConferencesParticipantsCreate
        extends ApiKeyPolicy("/twilio/voice/conferences.participants/create")

    /** Each Conference has a Participants subresource. Participants represent the set of people
      * currently connected to a running conference.
      */
    case object ConferencesParticipantsUpdate
        extends ApiKeyPolicy("/twilio/voice/conferences.participants/update")

    /** Each Conference has a Participants subresource. Participants represent the set of people
      * currently connected to a running conference.
      */
    case object ConferencesParticipantsDelete
        extends ApiKeyPolicy("/twilio/voice/conferences.participants/delete")

    /** Describes a trunk that can be configured to send/receive traffic to/from a PSTN Carrier. */
    case object SipByocTrunksRead extends ApiKeyPolicy("/twilio/voice/sip.byoc-trunks/read")

    /** Describes a trunk that can be configured to send/receive traffic to/from a PSTN Carrier. */
    case object SipByocTrunksList extends ApiKeyPolicy("/twilio/voice/sip.byoc-trunks/list")

    /** Describes a trunk that can be configured to send/receive traffic to/from a PSTN Carrier. */
    case object SipByocTrunksCreate extends ApiKeyPolicy("/twilio/voice/sip.byoc-trunks/create")

    /** Describes a trunk that can be configured to send/receive traffic to/from a PSTN Carrier. */
    case object SipByocTrunksUpdate extends ApiKeyPolicy("/twilio/voice/sip.byoc-trunks/update")

    /** Describes a trunk that can be configured to send/receive traffic to/from a PSTN Carrier. */
    case object SipByocTrunksDelete extends ApiKeyPolicy("/twilio/voice/sip.byoc-trunks/delete")

    /** An object that represents a connection between a telephone and Twilio. */
    case object CallsRead extends ApiKeyPolicy("/twilio/voice/calls/read")

    /** An object that represents a connection between a telephone and Twilio. */
    case object CallsList extends ApiKeyPolicy("/twilio/voice/calls/list")

    /** An object that represents a connection between a telephone and Twilio. */
    case object CallsCreate extends ApiKeyPolicy("/twilio/voice/calls/create")

    /** An object that represents a connection between a telephone and Twilio. */
    case object CallsUpdate extends ApiKeyPolicy("/twilio/voice/calls/update")

    /** An object that represents a connection between a telephone and Twilio. */
    case object CallsDelete extends ApiKeyPolicy("/twilio/voice/calls/delete")

    /** Describes a list of URI Entries that are used to route Origination traffic to a PSTN Carrier
      * over a BYOC Trunk.
      */
    case object SipConnectionPoliciesRead
        extends ApiKeyPolicy("/twilio/voice/sip.connection-policies/read")

    /** Describes a list of URI Entries that are used to route Origination traffic to a PSTN Carrier
      * over a BYOC Trunk.
      */
    case object SipConnectionPoliciesList
        extends ApiKeyPolicy("/twilio/voice/sip.connection-policies/list")

    /** Describes a list of URI Entries that are used to route Origination traffic to a PSTN Carrier
      * over a BYOC Trunk.
      */
    case object SipConnectionPoliciesCreate
        extends ApiKeyPolicy("/twilio/voice/sip.connection-policies/create")

    /** Describes a list of URI Entries that are used to route Origination traffic to a PSTN Carrier
      * over a BYOC Trunk.
      */
    case object SipConnectionPoliciesUpdate
        extends ApiKeyPolicy("/twilio/voice/sip.connection-policies/update")

    /** Describes a list of URI Entries that are used to route Origination traffic to a PSTN Carrier
      * over a BYOC Trunk.
      */
    case object SipConnectionPoliciesDelete
        extends ApiKeyPolicy("/twilio/voice/sip.connection-policies/delete")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipCredentialListsRead
        extends ApiKeyPolicy("/twilio/voice/sip.credential-lists/read")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipCredentialListsList
        extends ApiKeyPolicy("/twilio/voice/sip.credential-lists/list")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipCredentialListsCreate
        extends ApiKeyPolicy("/twilio/voice/sip.credential-lists/create")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipCredentialListsUpdate
        extends ApiKeyPolicy("/twilio/voice/sip.credential-lists/update")

    /** Contain the credentials of the users who are allowed to reach your SIP Domain. */
    case object SipCredentialListsDelete
        extends ApiKeyPolicy("/twilio/voice/sip.credential-lists/delete")

    /** Elastic SIP Trunking enables you to make & receive telephone calls from your IP
      * communications infrastructure.
      */
    case object SipTrunksRead extends ApiKeyPolicy("/twilio/voice/sip.trunks/read")

    /** Elastic SIP Trunking enables you to make & receive telephone calls from your IP
      * communications infrastructure.
      */
    case object SipTrunksList extends ApiKeyPolicy("/twilio/voice/sip.trunks/list")

    /** Elastic SIP Trunking enables you to make & receive telephone calls from your IP
      * communications infrastructure.
      */
    case object SipTrunksCreate extends ApiKeyPolicy("/twilio/voice/sip.trunks/create")

    /** Elastic SIP Trunking enables you to make & receive telephone calls from your IP
      * communications infrastructure.
      */
    case object SipTrunksUpdate extends ApiKeyPolicy("/twilio/voice/sip.trunks/update")

    /** Elastic SIP Trunking enables you to make & receive telephone calls from your IP
      * communications infrastructure.
      */
    case object SipTrunksDelete extends ApiKeyPolicy("/twilio/voice/sip.trunks/delete")

    /** Voice twiml apps. */
    case object TwimlAppsRead extends ApiKeyPolicy("/twilio/voice/twiml.apps/read")

    /** Voice twiml apps. */
    case object TwimlAppsList extends ApiKeyPolicy("/twilio/voice/twiml.apps/list")

    /** Voice twiml apps. */
    case object TwimlAppsCreate extends ApiKeyPolicy("/twilio/voice/twiml.apps/create")

    /** Voice twiml apps. */
    case object TwimlAppsUpdate extends ApiKeyPolicy("/twilio/voice/twiml.apps/update")

    /** Voice twiml apps. */
    case object TwimlAppsDelete extends ApiKeyPolicy("/twilio/voice/twiml.apps/delete")

    /** A Service provides control and configuration for how Transcripts are processed. */
    case object IntelligenceServicesRead
        extends ApiKeyPolicy("/twilio/voice/intelligence.services/read")

    /** A Service provides control and configuration for how Transcripts are processed. */
    case object IntelligenceServicesList
        extends ApiKeyPolicy("/twilio/voice/intelligence.services/list")

    /** A Service provides control and configuration for how Transcripts are processed. */
    case object IntelligenceServicesCreate
        extends ApiKeyPolicy("/twilio/voice/intelligence.services/create")

    /** A Service provides control and configuration for how Transcripts are processed. */
    case object IntelligenceServicesUpdate
        extends ApiKeyPolicy("/twilio/voice/intelligence.services/update")

    /** A Service provides control and configuration for how Transcripts are processed. */
    case object IntelligenceServicesDelete
        extends ApiKeyPolicy("/twilio/voice/intelligence.services/delete")

    /** Describes a custom DNS hostname that can accept SIP traffic for your account. */
    case object SipDomainsRead extends ApiKeyPolicy("/twilio/voice/sip.domains/read")

    /** Describes a custom DNS hostname that can accept SIP traffic for your account. */
    case object SipDomainsList extends ApiKeyPolicy("/twilio/voice/sip.domains/list")

    /** Describes a custom DNS hostname that can accept SIP traffic for your account. */
    case object SipDomainsCreate extends ApiKeyPolicy("/twilio/voice/sip.domains/create")

    /** Describes a custom DNS hostname that can accept SIP traffic for your account. */
    case object SipDomainsUpdate extends ApiKeyPolicy("/twilio/voice/sip.domains/update")

    /** Describes a custom DNS hostname that can accept SIP traffic for your account. */
    case object SipDomainsDelete extends ApiKeyPolicy("/twilio/voice/sip.domains/delete")

    /** The Recordings subresource on any given Call. */
    case object CallsRecordingsRead extends ApiKeyPolicy("/twilio/voice/calls.recordings/read")

    /** The Recordings subresource on any given Call. */
    case object CallsRecordingsList extends ApiKeyPolicy("/twilio/voice/calls.recordings/list")

    /** The Recordings subresource on any given Call. */
    case object CallsRecordingsCreate extends ApiKeyPolicy("/twilio/voice/calls.recordings/create")

    /** The Recordings subresource on any given Call. */
    case object CallsRecordingsUpdate extends ApiKeyPolicy("/twilio/voice/calls.recordings/update")

    /** The Recordings subresource on any given Call. */
    case object CallsRecordingsDelete extends ApiKeyPolicy("/twilio/voice/calls.recordings/delete")

    /** Describes Static IP addresses used to address the BYOC Trunk's Termination SIP Domain via an
      * IP Address instead of an FQDN.
      */
    case object SipIpRecordsRead extends ApiKeyPolicy("/twilio/voice/sip.ip-records/read")

    /** Describes Static IP addresses used to address the BYOC Trunk's Termination SIP Domain via an
      * IP Address instead of an FQDN.
      */
    case object SipIpRecordsList extends ApiKeyPolicy("/twilio/voice/sip.ip-records/list")

    /** Describes Static IP addresses used to address the BYOC Trunk's Termination SIP Domain via an
      * IP Address instead of an FQDN.
      */
    case object SipIpRecordsCreate extends ApiKeyPolicy("/twilio/voice/sip.ip-records/create")

    /** Describes Static IP addresses used to address the BYOC Trunk's Termination SIP Domain via an
      * IP Address instead of an FQDN.
      */
    case object SipIpRecordsUpdate extends ApiKeyPolicy("/twilio/voice/sip.ip-records/update")

    /** Describes Static IP addresses used to address the BYOC Trunk's Termination SIP Domain via an
      * IP Address instead of an FQDN.
      */
    case object SipIpRecordsDelete extends ApiKeyPolicy("/twilio/voice/sip.ip-records/delete")

    /** IpAccessControlList resources contain the Access Control List (ACL). */
    case object SipIpAclsRead extends ApiKeyPolicy("/twilio/voice/sip.ip-acls/read")

    /** IpAccessControlList resources contain the Access Control List (ACL). */
    case object SipIpAclsList extends ApiKeyPolicy("/twilio/voice/sip.ip-acls/list")

    /** IpAccessControlList resources contain the Access Control List (ACL). */
    case object SipIpAclsCreate extends ApiKeyPolicy("/twilio/voice/sip.ip-acls/create")

    /** IpAccessControlList resources contain the Access Control List (ACL). */
    case object SipIpAclsUpdate extends ApiKeyPolicy("/twilio/voice/sip.ip-acls/update")

    /** IpAccessControlList resources contain the Access Control List (ACL). */
    case object SipIpAclsDelete extends ApiKeyPolicy("/twilio/voice/sip.ip-acls/delete")

    /** A general pattern for how one system can be notified of events generated by another system
      * in real-time.
      */
    case object VerifyWebhookAll extends ApiKeyPolicy("/twilio/verify/webhook/*")

    /** A general pattern for how one system can be notified of events generated by another system
      * in real-time.
      */
    case object VerifyWebhookRead extends ApiKeyPolicy("/twilio/verify/webhook/read")

    /** A general pattern for how one system can be notified of events generated by another system
      * in real-time.
      */
    case object VerifyWebhookList extends ApiKeyPolicy("/twilio/verify/webhook/list")

    /** A general pattern for how one system can be notified of events generated by another system
      * in real-time.
      */
    case object VerifyWebhookCreate extends ApiKeyPolicy("/twilio/verify/webhook/create")

    /** A general pattern for how one system can be notified of events generated by another system
      * in real-time.
      */
    case object VerifyWebhookUpdate extends ApiKeyPolicy("/twilio/verify/webhook/update")

    /** A general pattern for how one system can be notified of events generated by another system
      * in real-time.
      */
    case object VerifyWebhookDelete extends ApiKeyPolicy("/twilio/verify/webhook/delete")

    /** A verification channel. */
    case object VerifyFactorAll extends ApiKeyPolicy("/twilio/verify/factor/*")

    /** A verification channel. */
    case object VerifyFactorRead extends ApiKeyPolicy("/twilio/verify/factor/read")

    /** A verification channel. */
    case object VerifyFactorList extends ApiKeyPolicy("/twilio/verify/factor/list")

    /** A verification channel. */
    case object VerifyFactorCreate extends ApiKeyPolicy("/twilio/verify/factor/create")

    /** A verification channel. */
    case object VerifyFactorUpdate extends ApiKeyPolicy("/twilio/verify/factor/update")

    /** A verification channel. */
    case object VerifyFactorDelete extends ApiKeyPolicy("/twilio/verify/factor/delete")

    /** The limit that should be enforced against the key it is associated with. */
    case object VerifyBucketAll extends ApiKeyPolicy("/twilio/verify/bucket/*")

    /** The limit that should be enforced against the key it is associated with. */
    case object VerifyBucketRead extends ApiKeyPolicy("/twilio/verify/bucket/read")

    /** The limit that should be enforced against the key it is associated with. */
    case object VerifyBucketList extends ApiKeyPolicy("/twilio/verify/bucket/list")

    /** The limit that should be enforced against the key it is associated with. */
    case object VerifyBucketCreate extends ApiKeyPolicy("/twilio/verify/bucket/create")

    /** The limit that should be enforced against the key it is associated with. */
    case object VerifyBucketUpdate extends ApiKeyPolicy("/twilio/verify/bucket/update")

    /** The limit that should be enforced against the key it is associated with. */
    case object VerifyBucketDelete extends ApiKeyPolicy("/twilio/verify/bucket/delete")

    /** The set of common configurations used to create and check verifications. */
    case object VerifyServiceAll extends ApiKeyPolicy("/twilio/verify/service/*")

    /** The set of common configurations used to create and check verifications. */
    case object VerifyServiceRead extends ApiKeyPolicy("/twilio/verify/service/read")

    /** The set of common configurations used to create and check verifications. */
    case object VerifyServiceList extends ApiKeyPolicy("/twilio/verify/service/list")

    /** The set of common configurations used to create and check verifications. */
    case object VerifyServiceCreate extends ApiKeyPolicy("/twilio/verify/service/create")

    /** The set of common configurations used to create and check verifications. */
    case object VerifyServiceUpdate extends ApiKeyPolicy("/twilio/verify/service/update")

    /** The set of common configurations used to create and check verifications. */
    case object VerifyServiceDelete extends ApiKeyPolicy("/twilio/verify/service/delete")

    /** Define the keys to meter and limits to enforce when starting user verifications. */
    case object VerifyRateLimitAll extends ApiKeyPolicy("/twilio/verify/rate-limit/*")

    /** Define the keys to meter and limits to enforce when starting user verifications. */
    case object VerifyRateLimitRead extends ApiKeyPolicy("/twilio/verify/rate-limit/read")

    /** Define the keys to meter and limits to enforce when starting user verifications. */
    case object VerifyRateLimitList extends ApiKeyPolicy("/twilio/verify/rate-limit/list")

    /** Define the keys to meter and limits to enforce when starting user verifications. */
    case object VerifyRateLimitCreate extends ApiKeyPolicy("/twilio/verify/rate-limit/create")

    /** Define the keys to meter and limits to enforce when starting user verifications. */
    case object VerifyRateLimitUpdate extends ApiKeyPolicy("/twilio/verify/rate-limit/update")

    /** Define the keys to meter and limits to enforce when starting user verifications. */
    case object VerifyRateLimitDelete extends ApiKeyPolicy("/twilio/verify/rate-limit/delete")

    /** Rules define the logic flow used by the Fraud Risk Engine's rule processor to identify
      * fraudulent activity and take appropriate actions.
      */
    case object VerifyRuleUpdate extends ApiKeyPolicy("/twilio/verify/rule/update")

    /** Rules define the logic flow used by the Fraud Risk Engine's rule processor to identify
      * fraudulent activity and take appropriate actions.
      */
    case object VerifyRuleDelete extends ApiKeyPolicy("/twilio/verify/rule/delete")

    /** A user or other identity that needs verification. */
    case object VerifyEntityCreate extends ApiKeyPolicy("/twilio/verify/entity/create")

    /** Predefined and approved messages used to send verifications that allow customization of the
      * verification message.
      */
    case object VerifyVerificationTemplateList
        extends ApiKeyPolicy("/twilio/verify/verification-template/list")

    /** A single verification attempt of an Entity using a Factor. */
    case object VerifyChallengeCreate extends ApiKeyPolicy("/twilio/verify/challenge/create")

    /** A single verification attempt of an Entity using a Factor. */
    case object VerifyChallengeList extends ApiKeyPolicy("/twilio/verify/challenge/list")

    /** A single verification attempt of an Entity using a Factor. */
    case object VerifyChallengeRead extends ApiKeyPolicy("/twilio/verify/challenge/read")

    /** List of phone numbers that will never be blocked by Verify Fraud Guard or Geo permissions.
      */
    case object VerifySafelistRead extends ApiKeyPolicy("/twilio/verify/safelist/read")

    /** A user or other identity that needs verification. */
    case object VerifyEntityRead extends ApiKeyPolicy("/twilio/verify/entity/read")

    /** Represents a verification validation that checks if a user-provided token is correct. */
    case object VerifyVerificationCheckCreate
        extends ApiKeyPolicy("/twilio/verify/verification-check/create")

    /** Resource to authenticate client's request to the Verify Push API when creating (i.e.,
      * enrolling or registering) an Entity and/or Factor.
      */
    case object VerifyAccessTokenRead extends ApiKeyPolicy("/twilio/verify/access-token/read")

    /** A user or other identity that needs verification. */
    case object VerifyEntityList extends ApiKeyPolicy("/twilio/verify/entity/list")

    /** Verify a user has a claimed device, phone number, or email address in their possession. */
    case object VerifyVerificationCreate extends ApiKeyPolicy("/twilio/verify/verification/create")

    /** Verify a user has a claimed device, phone number, or email address in their possession. */
    case object VerifyVerificationRead extends ApiKeyPolicy("/twilio/verify/verification/read")

    /** Rules define the logic flow used by the Fraud Risk Engine's rule processor to identify
      * fraudulent activity and take appropriate actions.
      */
    case object VerifyRuleList extends ApiKeyPolicy("/twilio/verify/rule/list")

    /** List of phone numbers that will never be blocked by Verify Fraud Guard or Geo permissions.
      */
    case object VerifySafelistDelete extends ApiKeyPolicy("/twilio/verify/safelist/delete")

    /** Lets developers request Verify Push retry sending a push notification for the same
      * Challenge.
      */
    case object VerifyPushNotificationCreate
        extends ApiKeyPolicy("/twilio/verify/push-notification/create")

    /** Verify a user has a claimed device, phone number, or email address in their possession. */
    case object VerifyVerificationUpdate extends ApiKeyPolicy("/twilio/verify/verification/update")

    /** List and filter verification attempts generated by Verify V2 services. */
    case object VerifyVerificationAttemptList
        extends ApiKeyPolicy("/twilio/verify/verification-attempt/list")

    /** A user or other identity that needs verification. */
    case object VerifyEntityDelete extends ApiKeyPolicy("/twilio/verify/entity/delete")

    /** Rules define the logic flow used by the Fraud Risk Engine's rule processor to identify
      * fraudulent activity and take appropriate actions.
      */
    case object VerifyRuleCreate extends ApiKeyPolicy("/twilio/verify/rule/create")

    /** List of phone numbers that will never be blocked by Verify Fraud Guard or Geo permissions.
      */
    case object VerifySafelistCreate extends ApiKeyPolicy("/twilio/verify/safelist/create")

    /** List and filter verification attempts generated by Verify V2 services. */
    case object VerifyVerificationAttemptRead
        extends ApiKeyPolicy("/twilio/verify/verification-attempt/read")

    /** Summarize verification attempts generated by Verify V2 services. */
    case object VerifyVerificationAttemptsSummaryRead
        extends ApiKeyPolicy("/twilio/verify/verification-attempts-summary/read")

    /** Resource to authenticate client's request to the Verify Push API when creating (i.e.,
      * enrolling or registering) an Entity and/or Factor.
      */
    case object VerifyAccessTokenCreate extends ApiKeyPolicy("/twilio/verify/access-token/create")

    /** The US App to Person (A2P) Messaging Service Use Case Resource allows you to fetch possible
      * A2P use cases for a Messaging Service.
      */
    case object MessagingServicesUsa2pUsecaseList
        extends ApiKeyPolicy("/twilio/messaging/services.usa2p-usecase/list")

    /** Represents a phone number associated to a Messaging Service. */
    case object MessagingServicesPhonenumbersRead
        extends ApiKeyPolicy("/twilio/messaging/services.phonenumbers/read")

    /** Represents a phone number associated to a Messaging Service. */
    case object MessagingServicesPhonenumbersList
        extends ApiKeyPolicy("/twilio/messaging/services.phonenumbers/list")

    /** Represents a phone number associated to a Messaging Service. */
    case object MessagingServicesPhonenumbersCreate
        extends ApiKeyPolicy("/twilio/messaging/services.phonenumbers/create")

    /** Represents a phone number associated to a Messaging Service. */
    case object MessagingServicesPhonenumbersDelete
        extends ApiKeyPolicy("/twilio/messaging/services.phonenumbers/delete")

    /** The US App to Person (A2P) Campaign Resource allows you to create a US A2P Campaign for a
      * Messaging Service.
      */
    case object MessagingServicesUsa2pCampaignRead
        extends ApiKeyPolicy("/twilio/messaging/services.usa2p-campaign/read")

    /** The US App to Person (A2P) Campaign Resource allows you to create a US A2P Campaign for a
      * Messaging Service.
      */
    case object MessagingServicesUsa2pCampaignList
        extends ApiKeyPolicy("/twilio/messaging/services.usa2p-campaign/list")

    /** The US App to Person (A2P) Campaign Resource allows you to create a US A2P Campaign for a
      * Messaging Service.
      */
    case object MessagingServicesUsa2pCampaignCreate
        extends ApiKeyPolicy("/twilio/messaging/services.usa2p-campaign/create")

    /** The US App to Person (A2P) Campaign Resource allows you to create a US A2P Campaign for a
      * Messaging Service.
      */
    case object MessagingServicesUsa2pCampaignDelete
        extends ApiKeyPolicy("/twilio/messaging/services.usa2p-campaign/delete")

    /** Represents a channel sender that is associated with a Messaging Service, such as WhatsApp.
      */
    case object MessagingServicesChannelsendersRead
        extends ApiKeyPolicy("/twilio/messaging/services.channelsenders/read")

    /** Represents a channel sender that is associated with a Messaging Service, such as WhatsApp.
      */
    case object MessagingServicesChannelsendersList
        extends ApiKeyPolicy("/twilio/messaging/services.channelsenders/list")

    /** Represents the short codes associated to a Messaging Service. */
    case object MessagingServicesShortcodesRead
        extends ApiKeyPolicy("/twilio/messaging/services.shortcodes/read")

    /** Represents the short codes associated to a Messaging Service. */
    case object MessagingServicesShortcodesList
        extends ApiKeyPolicy("/twilio/messaging/services.shortcodes/list")

    /** Represents the short codes associated to a Messaging Service. */
    case object MessagingServicesShortcodesCreate
        extends ApiKeyPolicy("/twilio/messaging/services.shortcodes/create")

    /** Represents the short codes associated to a Messaging Service. */
    case object MessagingServicesShortcodesDelete
        extends ApiKeyPolicy("/twilio/messaging/services.shortcodes/delete")

    /** Represents an Alphanumeric Sender ID (alpha sender) associated with a Messaging Service. */
    case object MessagingServicesAlphasendersRead
        extends ApiKeyPolicy("/twilio/messaging/services.alphasenders/read")

    /** Represents an Alphanumeric Sender ID (alpha sender) associated with a Messaging Service. */
    case object MessagingServicesAlphasendersList
        extends ApiKeyPolicy("/twilio/messaging/services.alphasenders/list")

    /** Represents an Alphanumeric Sender ID (alpha sender) associated with a Messaging Service. */
    case object MessagingServicesAlphasendersCreate
        extends ApiKeyPolicy("/twilio/messaging/services.alphasenders/create")

    /** Represents an Alphanumeric Sender ID (alpha sender) associated with a Messaging Service. */
    case object MessagingServicesAlphasendersDelete
        extends ApiKeyPolicy("/twilio/messaging/services.alphasenders/delete")

    /** Represents a set of configurable behavior for sending and receiving messages. */
    case object MessagingServicesAll extends ApiKeyPolicy("/twilio/messaging/services/*")

    /** Represents a set of configurable behavior for sending and receiving messages. */
    case object MessagingServicesRead extends ApiKeyPolicy("/twilio/messaging/services/read")

    /** Represents a set of configurable behavior for sending and receiving messages. */
    case object MessagingServicesList extends ApiKeyPolicy("/twilio/messaging/services/list")

    /** Represents a set of configurable behavior for sending and receiving messages. */
    case object MessagingServicesCreate extends ApiKeyPolicy("/twilio/messaging/services/create")

    /** Represents a set of configurable behavior for sending and receiving messages. */
    case object MessagingServicesUpdate extends ApiKeyPolicy("/twilio/messaging/services/update")

    /** Represents a set of configurable behavior for sending and receiving messages. */
    case object MessagingServicesDelete extends ApiKeyPolicy("/twilio/messaging/services/delete")

    /** Represents a Destination Alpha Sender associated with a Messaging Service. Destination Alpha
      * Sender can send to a particular ISO country code.
      */
    case object MessagingServicesDestinationAlphaSendersRead
        extends ApiKeyPolicy("/twilio/messaging/services.destination-alpha-senders/read")

    /** Represents a Destination Alpha Sender associated with a Messaging Service. Destination Alpha
      * Sender can send to a particular ISO country code.
      */
    case object MessagingServicesDestinationAlphaSendersList
        extends ApiKeyPolicy("/twilio/messaging/services.destination-alpha-senders/list")

    /** Represents a Destination Alpha Sender associated with a Messaging Service. Destination Alpha
      * Sender can send to a particular ISO country code.
      */
    case object MessagingServicesDestinationAlphaSendersCreate
        extends ApiKeyPolicy("/twilio/messaging/services.destination-alpha-senders/create")

    /** Represents a Destination Alpha Sender associated with a Messaging Service. Destination Alpha
      * Sender can send to a particular ISO country code.
      */
    case object MessagingServicesDestinationAlphaSendersDelete
        extends ApiKeyPolicy("/twilio/messaging/services.destination-alpha-senders/delete")

    /** Represents an inbound or outbound message. */
    case object MessagingMessagesAll extends ApiKeyPolicy("/twilio/messaging/messages/*")

    /** Represents an inbound or outbound message. */
    case object MessagingMessagesRead extends ApiKeyPolicy("/twilio/messaging/messages/read")

    /** Represents an inbound or outbound message. */
    case object MessagingMessagesList extends ApiKeyPolicy("/twilio/messaging/messages/list")

    /** Represents an inbound or outbound message. */
    case object MessagingMessagesCreate extends ApiKeyPolicy("/twilio/messaging/messages/create")

    /** Represents an inbound or outbound message. */
    case object MessagingMessagesUpdate extends ApiKeyPolicy("/twilio/messaging/messages/update")

    /** Represents an inbound or outbound message. */
    case object MessagingMessagesDelete extends ApiKeyPolicy("/twilio/messaging/messages/delete")

    /** The MessageFeedback subresource represents the reported outcome of tracking the performance
      * of a user action taken by the recipient of the message.
      */
    case object MessagingMessagesFeedbackCreate
        extends ApiKeyPolicy("/twilio/messaging/messages.feedback/create")

    /** Represents templated messages for messaging use cases. */
    case object MessagingContentTemplatesRead
        extends ApiKeyPolicy("/twilio/messaging/content-templates/read")

    /** Represents templated messages for messaging use cases. */
    case object MessagingContentTemplatesList
        extends ApiKeyPolicy("/twilio/messaging/content-templates/list")

    /** Represents templated messages for messaging use cases. */
    case object MessagingContentTemplatesCreate
        extends ApiKeyPolicy("/twilio/messaging/content-templates/create")

    /** Represents templated messages for messaging use cases. */
    case object MessagingContentTemplatesDelete
        extends ApiKeyPolicy("/twilio/messaging/content-templates/delete")

    /** Provides a simple API to pull real-time, account specific pricing. */
    case object MessagingPricingRead extends ApiKeyPolicy("/twilio/messaging/pricing/read")

    /** Provides a simple API to pull real-time, account specific pricing. */
    case object MessagingPricingList extends ApiKeyPolicy("/twilio/messaging/pricing/list")

    /** This resource retrieves a list of United States phone numbers that have been deactivated by
      * mobile carriers.
      */
    case object MessagingDeactivationsList
        extends ApiKeyPolicy("/twilio/messaging/deactivations/list")

    /** Represents a Whatsapp Sender. */
    case object MessagingWhatsappSendersAll
        extends ApiKeyPolicy("/twilio/messaging/whatsapp-senders/*")

    /** Represents a Whatsapp Sender. */
    case object MessagingWhatsappSendersRead
        extends ApiKeyPolicy("/twilio/messaging/whatsapp-senders/read")

    /** Represents a Whatsapp Sender. */
    case object MessagingWhatsappSendersList
        extends ApiKeyPolicy("/twilio/messaging/whatsapp-senders/list")

    /** Represents a Whatsapp Sender. */
    case object MessagingWhatsappSendersCreate
        extends ApiKeyPolicy("/twilio/messaging/whatsapp-senders/create")

    /** Represents a Whatsapp Sender. */
    case object MessagingWhatsappSendersUpdate
        extends ApiKeyPolicy("/twilio/messaging/whatsapp-senders/update")

    /** Represents a Whatsapp Sender. */
    case object MessagingWhatsappSendersDelete
        extends ApiKeyPolicy("/twilio/messaging/whatsapp-senders/delete")

    /** Real time statistics for a Task Queue. */
    case object TaskRouterWorkspacesTaskQueueRealTimeStatisticsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.task-queue.real-time-statistics/list")

    /** TaskRouter logs Events for each state change in the Workspace for the purpose of historical
      * reporting and auditing; it keeps that data for 30 days.
      */
    case object TaskRouterWorkspacesEventsRead
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.events/read")

    /** TaskRouter logs Events for each state change in the Workspace for the purpose of historical
      * reporting and auditing; it keeps that data for 30 days.
      */
    case object TaskRouterWorkspacesEventsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.events/list")

    /** Cumulative statistics of your Task Queue over a certain time period. Cumulative statistics
      * allow you to analyze data from the past 30 days.
      */
    case object TaskRouterWorkspacesTaskQueuesCumulativeStatisticsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.task-queues.cumulative-statistics/list")

    /** Real time statistics for a Workspace. */
    case object TaskRouterWorkspacesRealTimeStatisticsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.real-time-statistics/list")

    /** Real time statistics for multiple Task Queues. */
    case object TaskRouterWorkspacesTaskQueueBulkRealTimeStatisticsList
        extends ApiKeyPolicy(
          "/twilio/taskrouter/workspaces.task-queue.bulk-real-time-statistics/list"
        )

    /** Statistics for a Workspace. */
    case object TaskRouterWorkspacesStatisticsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.statistics/list")

    /** Cumulative statistics for your Workspace over a certain time period. Cumulative statistics
      * allow you to analyze data from the past 30 days.
      */
    case object TaskRouterWorkspacesCumulativeStatisticsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.cumulative-statistics/list")

    /** Workflows control how tasks will be prioritized and routed into Queues, and how Tasks should
      * escalate in priority or move across queues over time.
      */
    case object TaskRouterWorkspacesWorkflowsAll
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workflows/*")

    /** Workflows control how tasks will be prioritized and routed into Queues, and how Tasks should
      * escalate in priority or move across queues over time.
      */
    case object TaskRouterWorkspacesWorkflowsRead
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workflows/read")

    /** Workflows control how tasks will be prioritized and routed into Queues, and how Tasks should
      * escalate in priority or move across queues over time.
      */
    case object TaskRouterWorkspacesWorkflowsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workflows/list")

    /** Workflows control how tasks will be prioritized and routed into Queues, and how Tasks should
      * escalate in priority or move across queues over time.
      */
    case object TaskRouterWorkspacesWorkflowsCreate
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workflows/create")

    /** Workflows control how tasks will be prioritized and routed into Queues, and how Tasks should
      * escalate in priority or move across queues over time.
      */
    case object TaskRouterWorkspacesWorkflowsUpdate
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workflows/update")

    /** Workflows control how tasks will be prioritized and routed into Queues, and how Tasks should
      * escalate in priority or move across queues over time.
      */
    case object TaskRouterWorkspacesWorkflowsDelete
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workflows/delete")

    /** TaskRouter creates a Reservation subresource whenever a Task is reserved for a Worker.
      * TaskRouter will provide the details of this Reservation Instance subresource in the
      * Assignment Callback HTTP request it makes to your application server.
      */
    case object TaskRouterWorkspacesTasksReservationsRead
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.tasks.reservations/read")

    /** TaskRouter creates a Reservation subresource whenever a Task is reserved for a Worker.
      * TaskRouter will provide the details of this Reservation Instance subresource in the
      * Assignment Callback HTTP request it makes to your application server.
      */
    case object TaskRouterWorkspacesTasksReservationsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.tasks.reservations/list")

    /** TaskRouter creates a Reservation subresource whenever a Task is reserved for a Worker.
      * TaskRouter will provide the details of this Reservation Instance subresource in the
      * Assignment Callback HTTP request it makes to your application server.
      */
    case object TaskRouterWorkspacesTasksReservationsUpdate
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.tasks.reservations/update")

    /** Real time statistics for a Workflow. */
    case object TaskRouterWorkspacesWorkflowsRealTimeStatisticsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workflows.real-time-statistics/list")

    /** Worker Reservations represent the current and past reservations for a Worker. Current
      * Reservations can be accepted using the Reservation instance resource.
      */
    case object TaskRouterWorkspacesWorkersReservationsRead
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workers.reservations/read")

    /** Worker Reservations represent the current and past reservations for a Worker. Current
      * Reservations can be accepted using the Reservation instance resource.
      */
    case object TaskRouterWorkspacesWorkersReservationsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workers.reservations/list")

    /** Worker Reservations represent the current and past reservations for a Worker. Current
      * Reservations can be accepted using the Reservation instance resource.
      */
    case object TaskRouterWorkspacesWorkersReservationsUpdate
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workers.reservations/update")

    /** Statistics for Workers. */
    case object TaskRouterWorkspacesWorkersStatisticsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workers.statistics/list")

    /** Workers represent an entity that is able to perform tasks, such as an agent working in a
      * call center, or a salesperson handling leads.
      */
    case object TaskRouterWorkspacesWorkersAll
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workers/*")

    /** Workers represent an entity that is able to perform tasks, such as an agent working in a
      * call center, or a salesperson handling leads.
      */
    case object TaskRouterWorkspacesWorkersRead
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workers/read")

    /** Workers represent an entity that is able to perform tasks, such as an agent working in a
      * call center, or a salesperson handling leads.
      */
    case object TaskRouterWorkspacesWorkersList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workers/list")

    /** Workers represent an entity that is able to perform tasks, such as an agent working in a
      * call center, or a salesperson handling leads.
      */
    case object TaskRouterWorkspacesWorkersCreate
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workers/create")

    /** Workers represent an entity that is able to perform tasks, such as an agent working in a
      * call center, or a salesperson handling leads.
      */
    case object TaskRouterWorkspacesWorkersUpdate
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workers/update")

    /** Workers represent an entity that is able to perform tasks, such as an agent working in a
      * call center, or a salesperson handling leads.
      */
    case object TaskRouterWorkspacesWorkersDelete
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workers/delete")

    /** Statistics of all the queues in a workspace. */
    case object TaskRouterWorkspacesTaskQueuesStatisticsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.task-queues.statistics/list")

    /** A Task represents a single item of work waiting to be processed. */
    case object TaskRouterWorkspacesTasksAll
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.tasks/*")

    /** A Task represents a single item of work waiting to be processed. */
    case object TaskRouterWorkspacesTasksRead
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.tasks/read")

    /** A Task represents a single item of work waiting to be processed. */
    case object TaskRouterWorkspacesTasksList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.tasks/list")

    /** A Task represents a single item of work waiting to be processed. */
    case object TaskRouterWorkspacesTasksCreate
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.tasks/create")

    /** A Task represents a single item of work waiting to be processed. */
    case object TaskRouterWorkspacesTasksUpdate
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.tasks/update")

    /** A Task represents a single item of work waiting to be processed. */
    case object TaskRouterWorkspacesTasksDelete
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.tasks/delete")

    /** Task Queues allow you to categorize Tasks and describe which Workers are eligible to handle
      * those Tasks.
      */
    case object TaskRouterWorkspacesTaskQueuesAll
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.task-queues/*")

    /** Task Queues allow you to categorize Tasks and describe which Workers are eligible to handle
      * those Tasks.
      */
    case object TaskRouterWorkspacesTaskQueuesRead
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.task-queues/read")

    /** Task Queues allow you to categorize Tasks and describe which Workers are eligible to handle
      * those Tasks.
      */
    case object TaskRouterWorkspacesTaskQueuesList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.task-queues/list")

    /** Task Queues allow you to categorize Tasks and describe which Workers are eligible to handle
      * those Tasks.
      */
    case object TaskRouterWorkspacesTaskQueuesCreate
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.task-queues/create")

    /** Task Queues allow you to categorize Tasks and describe which Workers are eligible to handle
      * those Tasks.
      */
    case object TaskRouterWorkspacesTaskQueuesUpdate
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.task-queues/update")

    /** Task Queues allow you to categorize Tasks and describe which Workers are eligible to handle
      * those Tasks.
      */
    case object TaskRouterWorkspacesTaskQueuesDelete
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.task-queues/delete")

    /** Statistics for a Workflow. */
    case object TaskRouterWorkspacesWorkflowsStatisticsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workflows.statistics/list")

    /** Cumulative statistics for your Workflow over a certain time period. Cumulative statistics
      * allow you to analyze data from the past 30 days.
      */
    case object TaskRouterWorkspacesWorkflowsCumulativeStatisticsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workflows.cumulative-statistics/list")

    /** Cumulative statistics for your Workers over a certain time period. Cumulative statistics
      * allow you to analyze Worker data from the past 30 days.
      */
    case object TaskRouterWorkspacesWorkersCumulativeStatisticsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workers.cumulative-statistics/list")

    /** Instance statistics of your Task Queue. */
    case object TaskRouterWorkspacesTaskQueuesInstanceStatisticsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.task-queues.instance-statistics/list")

    /** Real time statistics for Workers. */
    case object TaskRouterWorkspacesWorkersRealTimeStatisticsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workers.real-time-statistics/list")

    /** Task Channels provide a mechanism to separate tasks of different types. You can specify
      * different concurrent capacity for tasks of each type.
      */
    case object TaskRouterWorkspacesTaskChannelsAll
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.task-channels/*")

    /** Task Channels provide a mechanism to separate tasks of different types. You can specify
      * different concurrent capacity for tasks of each type.
      */
    case object TaskRouterWorkspacesTaskChannelsRead
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.task-channels/read")

    /** Task Channels provide a mechanism to separate tasks of different types. You can specify
      * different concurrent capacity for tasks of each type.
      */
    case object TaskRouterWorkspacesTaskChannelsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.task-channels/list")

    /** Task Channels provide a mechanism to separate tasks of different types. You can specify
      * different concurrent capacity for tasks of each type.
      */
    case object TaskRouterWorkspacesTaskChannelsCreate
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.task-channels/create")

    /** Task Channels provide a mechanism to separate tasks of different types. You can specify
      * different concurrent capacity for tasks of each type.
      */
    case object TaskRouterWorkspacesTaskChannelsUpdate
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.task-channels/update")

    /** Task Channels provide a mechanism to separate tasks of different types. You can specify
      * different concurrent capacity for tasks of each type.
      */
    case object TaskRouterWorkspacesTaskChannelsDelete
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.task-channels/delete")

    /** Statistics for a specific Worker. */
    case object TaskRouterWorkspacesWorkersInstanceStatisticsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workers.instance-statistics/list")

    /** Worker Channels show the Worker's capacity for handling multiple concurrent Tasks. */
    case object TaskRouterWorkspacesWorkersChannelsRead
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workers.channels/read")

    /** Worker Channels show the Worker's capacity for handling multiple concurrent Tasks. */
    case object TaskRouterWorkspacesWorkersChannelsList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workers.channels/list")

    /** Worker Channels show the Worker's capacity for handling multiple concurrent Tasks. */
    case object TaskRouterWorkspacesWorkersChannelsUpdate
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.workers.channels/update")

    /** Activities describe the current status of your Workers, which determines whether they are
      * eligible to receive task assignments. Workers are always set to a single Activity.
      */
    case object TaskRouterWorkspacesActivitiesAll
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.activities/*")

    /** Activities describe the current status of your Workers, which determines whether they are
      * eligible to receive task assignments. Workers are always set to a single Activity.
      */
    case object TaskRouterWorkspacesActivitiesRead
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.activities/read")

    /** Activities describe the current status of your Workers, which determines whether they are
      * eligible to receive task assignments. Workers are always set to a single Activity.
      */
    case object TaskRouterWorkspacesActivitiesList
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.activities/list")

    /** Activities describe the current status of your Workers, which determines whether they are
      * eligible to receive task assignments. Workers are always set to a single Activity.
      */
    case object TaskRouterWorkspacesActivitiesCreate
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.activities/create")

    /** Activities describe the current status of your Workers, which determines whether they are
      * eligible to receive task assignments. Workers are always set to a single Activity.
      */
    case object TaskRouterWorkspacesActivitiesUpdate
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.activities/update")

    /** Activities describe the current status of your Workers, which determines whether they are
      * eligible to receive task assignments. Workers are always set to a single Activity.
      */
    case object TaskRouterWorkspacesActivitiesDelete
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces.activities/delete")

    /** A Workspace is a container for your Tasks, Workers, TaskQueues, Workflows, TaskChannels and
      * Activities.
      */
    case object TaskRouterWorkspacesAll extends ApiKeyPolicy("/twilio/taskrouter/workspaces/*")

    /** A Workspace is a container for your Tasks, Workers, TaskQueues, Workflows, TaskChannels and
      * Activities.
      */
    case object TaskRouterWorkspacesRead extends ApiKeyPolicy("/twilio/taskrouter/workspaces/read")

    /** A Workspace is a container for your Tasks, Workers, TaskQueues, Workflows, TaskChannels and
      * Activities.
      */
    case object TaskRouterWorkspacesList extends ApiKeyPolicy("/twilio/taskrouter/workspaces/list")

    /** A Workspace is a container for your Tasks, Workers, TaskQueues, Workflows, TaskChannels and
      * Activities.
      */
    case object TaskRouterWorkspacesCreate
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces/create")

    /** A Workspace is a container for your Tasks, Workers, TaskQueues, Workflows, TaskChannels and
      * Activities.
      */
    case object TaskRouterWorkspacesUpdate
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces/update")

    /** A Workspace is a container for your Tasks, Workers, TaskQueues, Workflows, TaskChannels and
      * Activities.
      */
    case object TaskRouterWorkspacesDelete
        extends ApiKeyPolicy("/twilio/taskrouter/workspaces/delete")

    /** Query information on a phone number so that you can make a trusted interaction with the
      * user.
      */
    case object LookupPhoneNumbersRead extends ApiKeyPolicy("/twilio/lookup/phone-numbers/read")

    /** OAuth apps generate credentials to access Twilio Account APIs. Currently, we support the
      * client credentials grant type of OAuth 2.0.
      */
    case object IamAccountOauthAppsAll extends ApiKeyPolicy("/twilio/iam/account-oauth-apps/*")

    /** OAuth apps generate credentials to access Twilio Account APIs. Currently, we support the
      * client credentials grant type of OAuth 2.0.
      */
    case object IamAccountOauthAppsRead extends ApiKeyPolicy("/twilio/iam/account-oauth-apps/read")

    /** OAuth apps generate credentials to access Twilio Account APIs. Currently, we support the
      * client credentials grant type of OAuth 2.0.
      */
    case object IamAccountOauthAppsList extends ApiKeyPolicy("/twilio/iam/account-oauth-apps/list")

    /** OAuth apps generate credentials to access Twilio Account APIs. Currently, we support the
      * client credentials grant type of OAuth 2.0.
      */
    case object IamAccountOauthAppsCreate
        extends ApiKeyPolicy("/twilio/iam/account-oauth-apps/create")

    /** OAuth apps generate credentials to access Twilio Account APIs. Currently, we support the
      * client credentials grant type of OAuth 2.0.
      */
    case object IamAccountOauthAppsUpdate
        extends ApiKeyPolicy("/twilio/iam/account-oauth-apps/update")

    /** OAuth apps generate credentials to access Twilio Account APIs. Currently, we support the
      * client credentials grant type of OAuth 2.0.
      */
    case object IamAccountOauthAppsDelete
        extends ApiKeyPolicy("/twilio/iam/account-oauth-apps/delete")

    /** API keys are unique credentials used to authenticate and authorize requests to an API,
      * enabling controlled access to its resources and operations.
      */
    case object IamApiKeysAll extends ApiKeyPolicy("/twilio/iam/api-keys/*")

    /** API keys are unique credentials used to authenticate and authorize requests to an API,
      * enabling controlled access to its resources and operations.
      */
    case object IamApiKeysRead extends ApiKeyPolicy("/twilio/iam/api-keys/read")

    /** API keys are unique credentials used to authenticate and authorize requests to an API,
      * enabling controlled access to its resources and operations.
      */
    case object IamApiKeysList extends ApiKeyPolicy("/twilio/iam/api-keys/list")

    /** API keys are unique credentials used to authenticate and authorize requests to an API,
      * enabling controlled access to its resources and operations.
      */
    case object IamApiKeysCreate extends ApiKeyPolicy("/twilio/iam/api-keys/create")

    /** API keys are unique credentials used to authenticate and authorize requests to an API,
      * enabling controlled access to its resources and operations.
      */
    case object IamApiKeysUpdate extends ApiKeyPolicy("/twilio/iam/api-keys/update")

    /** API keys are unique credentials used to authenticate and authorize requests to an API,
      * enabling controlled access to its resources and operations.
      */
    case object IamApiKeysDelete extends ApiKeyPolicy("/twilio/iam/api-keys/delete")

    /** A public key is a cryptographic code that anyone can use to encrypt data or verify a digital
      * signature, but only the matching private key can decrypt or create the signature.
      */
    case object IamPublicKeysList extends ApiKeyPolicy("/twilio/iam/public-keys/list")

    /** Represents Twilio Accounts. When customers first sign up with Twilio, they have just one
      * main account and they can create more accounts and subaccounts for segmenting phone numbers
      * and usage data for their customers and controlling access to data.
      */
    case object IamAccountsRead extends ApiKeyPolicy("/twilio/iam/accounts/read")

    /** Represents Twilio Accounts. When customers first sign up with Twilio, they have just one
      * main account and they can create more accounts and subaccounts for segmenting phone numbers
      * and usage data for their customers and controlling access to data.
      */
    case object IamAccountsList extends ApiKeyPolicy("/twilio/iam/accounts/list")

    /** The Flow Validate endpoint will validate a Flow definition without creating a new Flow. */
    case object StudioFlowsValidateUpdate
        extends ApiKeyPolicy("/twilio/studio/flows.validate/update")

    /** Flows are individual workflows that you create. */
    case object StudioFlowsAll extends ApiKeyPolicy("/twilio/studio/flows/*")

    /** Flows are individual workflows that you create. */
    case object StudioFlowsRead extends ApiKeyPolicy("/twilio/studio/flows/read")

    /** Flows are individual workflows that you create. */
    case object StudioFlowsList extends ApiKeyPolicy("/twilio/studio/flows/list")

    /** Flows are individual workflows that you create. */
    case object StudioFlowsCreate extends ApiKeyPolicy("/twilio/studio/flows/create")

    /** Flows are individual workflows that you create. */
    case object StudioFlowsUpdate extends ApiKeyPolicy("/twilio/studio/flows/update")

    /** Flows are individual workflows that you create. */
    case object StudioFlowsDelete extends ApiKeyPolicy("/twilio/studio/flows/delete")

    /** Tracks every change made to a Flow resource. Revisions are automatically created when a Flow
      * is created or updated.
      */
    case object StudioFlowsRevisionsRead extends ApiKeyPolicy("/twilio/studio/flows.revisions/read")

    /** Tracks every change made to a Flow resource. Revisions are automatically created when a Flow
      * is created or updated.
      */
    case object StudioFlowsRevisionsList extends ApiKeyPolicy("/twilio/studio/flows.revisions/list")

    /** The current state of the Flow's Execution. As a flow executes, we save its state in this
      * context.
      */
    case object StudioExecutionsContextRead
        extends ApiKeyPolicy("/twilio/studio/executions.context/read")

    /** The current state of the Flow's Execution for a single step. As a flow executes, we save its
      * state in this context.
      */
    case object StudioExecutionsStepsContextRead
        extends ApiKeyPolicy("/twilio/studio/executions.steps.context/read")

    /** Represents a specific person's run through a Flow. */
    case object StudioExecutionsAll extends ApiKeyPolicy("/twilio/studio/executions/*")

    /** Represents a specific person's run through a Flow. */
    case object StudioExecutionsRead extends ApiKeyPolicy("/twilio/studio/executions/read")

    /** Represents a specific person's run through a Flow. */
    case object StudioExecutionsList extends ApiKeyPolicy("/twilio/studio/executions/list")

    /** Represents a specific person's run through a Flow. */
    case object StudioExecutionsCreate extends ApiKeyPolicy("/twilio/studio/executions/create")

    /** Represents a specific person's run through a Flow. */
    case object StudioExecutionsUpdate extends ApiKeyPolicy("/twilio/studio/executions/update")

    /** Represents a specific person's run through a Flow. */
    case object StudioExecutionsDelete extends ApiKeyPolicy("/twilio/studio/executions/delete")

    /** Runtime processing of a Widget, starting when that Widget is entered. Variables get set at
      * the end of a Step.
      */
    case object StudioExecutionsStepsRead
        extends ApiKeyPolicy("/twilio/studio/executions.steps/read")

    /** Runtime processing of a Widget, starting when that Widget is entered. Variables get set at
      * the end of a Step.
      */
    case object StudioExecutionsStepsList
        extends ApiKeyPolicy("/twilio/studio/executions.steps/list")

    /** Contact addresses (e.g. phone numbers, Chat identities) who can test the latest drafts of a
      * Flow even if they aren't yet published.
      */
    case object StudioTestUsersRead extends ApiKeyPolicy("/twilio/studio/test-users/read")

    /** Contact addresses (e.g. phone numbers, Chat identities) who can test the latest drafts of a
      * Flow even if they aren't yet published.
      */
    case object StudioTestUsersUpdate extends ApiKeyPolicy("/twilio/studio/test-users/update")

    /** Informs which type of document you create and what the values are to then create a new
      * Supporting Document with the correct type and values.
      */
    case object PhoneNumbersRegulatoryComplianceSupportingDocumentsTypesRead
        extends ApiKeyPolicy(
          "/twilio/phone-numbers/regulatory-compliance.supporting-documents-types/read"
        )

    /** Informs which type of document you create and what the values are to then create a new
      * Supporting Document with the correct type and values.
      */
    case object PhoneNumbersRegulatoryComplianceSupportingDocumentsTypesList
        extends ApiKeyPolicy(
          "/twilio/phone-numbers/regulatory-compliance.supporting-documents-types/list"
        )

    /** A Supporting Document is a container that holds metadata of a legal document to fulfill
      * Regulations.
      */
    case object PhoneNumbersRegulatoryComplianceSupportingDocumentsAll
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.supporting-documents/*")

    /** A Supporting Document is a container that holds metadata of a legal document to fulfill
      * Regulations.
      */
    case object PhoneNumbersRegulatoryComplianceSupportingDocumentsRead
        extends ApiKeyPolicy(
          "/twilio/phone-numbers/regulatory-compliance.supporting-documents/read"
        )

    /** A Supporting Document is a container that holds metadata of a legal document to fulfill
      * Regulations.
      */
    case object PhoneNumbersRegulatoryComplianceSupportingDocumentsList
        extends ApiKeyPolicy(
          "/twilio/phone-numbers/regulatory-compliance.supporting-documents/list"
        )

    /** A Supporting Document is a container that holds metadata of a legal document to fulfill
      * Regulations.
      */
    case object PhoneNumbersRegulatoryComplianceSupportingDocumentsCreate
        extends ApiKeyPolicy(
          "/twilio/phone-numbers/regulatory-compliance.supporting-documents/create"
        )

    /** A Supporting Document is a container that holds metadata of a legal document to fulfill
      * Regulations.
      */
    case object PhoneNumbersRegulatoryComplianceSupportingDocumentsUpdate
        extends ApiKeyPolicy(
          "/twilio/phone-numbers/regulatory-compliance.supporting-documents/update"
        )

    /** A Supporting Document is a container that holds metadata of a legal document to fulfill
      * Regulations.
      */
    case object PhoneNumbersRegulatoryComplianceSupportingDocumentsDelete
        extends ApiKeyPolicy(
          "/twilio/phone-numbers/regulatory-compliance.supporting-documents/delete"
        )

    /** Bundle is a container that references the required Regulatory Compliance information set
      * forth by the regulating telecom body of the end-user.
      */
    case object PhoneNumbersRegulatoryComplianceBundlesAll
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.bundles/*")

    /** Bundle is a container that references the required Regulatory Compliance information set
      * forth by the regulating telecom body of the end-user.
      */
    case object PhoneNumbersRegulatoryComplianceBundlesRead
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.bundles/read")

    /** Bundle is a container that references the required Regulatory Compliance information set
      * forth by the regulating telecom body of the end-user.
      */
    case object PhoneNumbersRegulatoryComplianceBundlesList
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.bundles/list")

    /** Bundle is a container that references the required Regulatory Compliance information set
      * forth by the regulating telecom body of the end-user.
      */
    case object PhoneNumbersRegulatoryComplianceBundlesCreate
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.bundles/create")

    /** Bundle is a container that references the required Regulatory Compliance information set
      * forth by the regulating telecom body of the end-user.
      */
    case object PhoneNumbersRegulatoryComplianceBundlesUpdate
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.bundles/update")

    /** Bundle is a container that references the required Regulatory Compliance information set
      * forth by the regulating telecom body of the end-user.
      */
    case object PhoneNumbersRegulatoryComplianceBundlesDelete
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.bundles/delete")

    /** End-User is the entity that answers the phone call or receives the message of a phone
      * number. An entity can be either an individual or a business.
      */
    case object PhoneNumbersRegulatoryComplianceEndUsersAll
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.end-users/*")

    /** End-User is the entity that answers the phone call or receives the message of a phone
      * number. An entity can be either an individual or a business.
      */
    case object PhoneNumbersRegulatoryComplianceEndUsersRead
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.end-users/read")

    /** End-User is the entity that answers the phone call or receives the message of a phone
      * number. An entity can be either an individual or a business.
      */
    case object PhoneNumbersRegulatoryComplianceEndUsersList
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.end-users/list")

    /** End-User is the entity that answers the phone call or receives the message of a phone
      * number. An entity can be either an individual or a business.
      */
    case object PhoneNumbersRegulatoryComplianceEndUsersCreate
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.end-users/create")

    /** End-User is the entity that answers the phone call or receives the message of a phone
      * number. An entity can be either an individual or a business.
      */
    case object PhoneNumbersRegulatoryComplianceEndUsersUpdate
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.end-users/update")

    /** End-User is the entity that answers the phone call or receives the message of a phone
      * number. An entity can be either an individual or a business.
      */
    case object PhoneNumbersRegulatoryComplianceEndUsersDelete
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.end-users/delete")

    /** Allows you to assign End-Users and Supporting Documents to Regulatory Bundles. */
    case object PhoneNumbersRegulatoryComplianceItemAssignmentsRead
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.item-assignments/read")

    /** Allows you to assign End-Users and Supporting Documents to Regulatory Bundles. */
    case object PhoneNumbersRegulatoryComplianceItemAssignmentsList
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.item-assignments/list")

    /** Allows you to assign End-Users and Supporting Documents to Regulatory Bundles. */
    case object PhoneNumbersRegulatoryComplianceItemAssignmentsCreate
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.item-assignments/create")

    /** Allows you to assign End-Users and Supporting Documents to Regulatory Bundles. */
    case object PhoneNumbersRegulatoryComplianceItemAssignmentsDelete
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.item-assignments/delete")

    /** Replace Items allows you to update compliance info when Regulations change while
      * provisioning new Phone Numbers.
      */
    case object PhoneNumbersRegulatoryComplianceBundlesReplaceItemsUpdate
        extends ApiKeyPolicy(
          "/twilio/phone-numbers/regulatory-compliance.bundles.replace-items/update"
        )

    /** Bundle Copy allows you to update compliance information when Regulations change while
      * keeping Phone Number provisioning habits.
      */
    case object PhoneNumbersRegulatoryComplianceBundleCopiesList
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.bundle-copies/list")

    /** Bundle Copy allows you to update compliance information when Regulations change while
      * keeping Phone Number provisioning habits.
      */
    case object PhoneNumbersRegulatoryComplianceBundleCopiesCreate
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.bundle-copies/create")

    /** Informs which type of end-user you can create and what the values are to then create a new
      * End-User resource.
      */
    case object PhoneNumbersRegulatoryComplianceEndUsersTypesRead
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.end-users-types/read")

    /** Informs which type of end-user you can create and what the values are to then create a new
      * End-User resource.
      */
    case object PhoneNumbersRegulatoryComplianceEndUsersTypesList
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.end-users-types/list")

    /** Evaluations allows developers to understand what failed and why when a Regulatory Bundle is
      * submitted to be evaluated against a Regulation.
      */
    case object PhoneNumbersRegulatoryComplianceEvaluationsRead
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.evaluations/read")

    /** Evaluations allows developers to understand what failed and why when a Regulatory Bundle is
      * submitted to be evaluated against a Regulation.
      */
    case object PhoneNumbersRegulatoryComplianceEvaluationsList
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.evaluations/list")

    /** Evaluations allows developers to understand what failed and why when a Regulatory Bundle is
      * submitted to be evaluated against a Regulation.
      */
    case object PhoneNumbersRegulatoryComplianceEvaluationsCreate
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.evaluations/create")

    /** Allows you to view and understand Regulations. Regulations are requirements based on
      * End-Users and Supporting Documents set by each country's government.
      */
    case object PhoneNumbersRegulatoryComplianceRegulationsRead
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.regulations/read")

    /** Allows you to view and understand Regulations. Regulations are requirements based on
      * End-Users and Supporting Documents set by each country's government.
      */
    case object PhoneNumbersRegulatoryComplianceRegulationsList
        extends ApiKeyPolicy("/twilio/phone-numbers/regulatory-compliance.regulations/list")

    /** Lets search for local, toll-free and mobile phone numbers that are available to purchase.
      */
    case object PhoneNumbersAvailableNumbersList
        extends ApiKeyPolicy("/twilio/phone-numbers/available-numbers/list")

    /** RBAC permissions for Twilio Address APIs required by TrustHub. */
    case object PhoneNumbersAddressServiceRead
        extends ApiKeyPolicy("/twilio/phone-numbers/address-service/read")

    /** RBAC permissions for Twilio Address APIs required by TrustHub. */
    case object PhoneNumbersAddressServiceCreate
        extends ApiKeyPolicy("/twilio/phone-numbers/address-service/create")

    /** Represents local, toll-free and mobile phone numbers provisioned from Twilio. */
    case object PhoneNumbersActiveNumbersAll
        extends ApiKeyPolicy("/twilio/phone-numbers/active-numbers/*")

    /** Represents local, toll-free and mobile phone numbers provisioned from Twilio. */
    case object PhoneNumbersActiveNumbersRead
        extends ApiKeyPolicy("/twilio/phone-numbers/active-numbers/read")

    /** Represents local, toll-free and mobile phone numbers provisioned from Twilio. */
    case object PhoneNumbersActiveNumbersList
        extends ApiKeyPolicy("/twilio/phone-numbers/active-numbers/list")

    /** Represents local, toll-free and mobile phone numbers provisioned from Twilio. */
    case object PhoneNumbersActiveNumbersCreate
        extends ApiKeyPolicy("/twilio/phone-numbers/active-numbers/create")

    /** Represents local, toll-free and mobile phone numbers provisioned from Twilio. */
    case object PhoneNumbersActiveNumbersUpdate
        extends ApiKeyPolicy("/twilio/phone-numbers/active-numbers/update")

    /** Represents local, toll-free and mobile phone numbers provisioned from Twilio. */
    case object PhoneNumbersActiveNumbersDelete
        extends ApiKeyPolicy("/twilio/phone-numbers/active-numbers/delete")

    /** Access to usage details on console */
    case object BillingUsageRead extends ApiKeyPolicy("/twilio/billing/usage/read")

    /** Events is a platform feature that provides comprehensive event-logging and change-tracking
      * for Twilio resources. The Events REST resource provides an API to retrieve event-log.
      */
    case object MonitorEventsList extends ApiKeyPolicy("/twilio/monitor/events/list")

    /** Events is a platform feature that provides comprehensive event-logging and change-tracking
      * for Twilio resources. The Events REST resource provides an API to retrieve event-log.
      */
    case object MonitorEventsRead extends ApiKeyPolicy("/twilio/monitor/events/read")

    /** An Alert resource instance represents a single log entry for an error or warning when Twilio
      * makes a webhook request to your server, or when your application makes a request to the REST
      * API.
      */
    case object MonitorAlertsList extends ApiKeyPolicy("/twilio/monitor/alerts/list")

    /** An Alert resource instance represents a single log entry for an error or warning when Twilio
      * makes a webhook request to your server, or when your application makes a request to the REST
      * API.
      */
    case object MonitorAlertsRead extends ApiKeyPolicy("/twilio/monitor/alerts/read")

    /** A versioned schema that all events of the same event type follow. */
    case object EventStreamsSchemaRead extends ApiKeyPolicy("/twilio/event-streams/schema/read")

    /** The versions of an event schema, each with its corresponding JSON schema. */
    case object EventStreamsSchemaVersionRead
        extends ApiKeyPolicy("/twilio/event-streams/schema.version/read")

    /** The versions of an event schema, each with its corresponding JSON schema. */
    case object EventStreamsSchemaVersionList
        extends ApiKeyPolicy("/twilio/event-streams/schema.version/list")

    /** Each of the event types a subscription is composed of. */
    case object EventStreamsSubscriptionSubscribedEventAll
        extends ApiKeyPolicy("/twilio/event-streams/subscription.subscribed-event/*")

    /** Each of the event types a subscription is composed of. */
    case object EventStreamsSubscriptionSubscribedEventRead
        extends ApiKeyPolicy("/twilio/event-streams/subscription.subscribed-event/read")

    /** Each of the event types a subscription is composed of. */
    case object EventStreamsSubscriptionSubscribedEventList
        extends ApiKeyPolicy("/twilio/event-streams/subscription.subscribed-event/list")

    /** Each of the event types a subscription is composed of. */
    case object EventStreamsSubscriptionSubscribedEventCreate
        extends ApiKeyPolicy("/twilio/event-streams/subscription.subscribed-event/create")

    /** Each of the event types a subscription is composed of. */
    case object EventStreamsSubscriptionSubscribedEventUpdate
        extends ApiKeyPolicy("/twilio/event-streams/subscription.subscribed-event/update")

    /** Each of the event types a subscription is composed of. */
    case object EventStreamsSubscriptionSubscribedEventDelete
        extends ApiKeyPolicy("/twilio/event-streams/subscription.subscribed-event/delete")

    /** Subset of event types to be sent to a sink. */
    case object EventStreamsSubscriptionAll
        extends ApiKeyPolicy("/twilio/event-streams/subscription/*")

    /** Subset of event types to be sent to a sink. */
    case object EventStreamsSubscriptionRead
        extends ApiKeyPolicy("/twilio/event-streams/subscription/read")

    /** Subset of event types to be sent to a sink. */
    case object EventStreamsSubscriptionList
        extends ApiKeyPolicy("/twilio/event-streams/subscription/list")

    /** Subset of event types to be sent to a sink. */
    case object EventStreamsSubscriptionCreate
        extends ApiKeyPolicy("/twilio/event-streams/subscription/create")

    /** Subset of event types to be sent to a sink. */
    case object EventStreamsSubscriptionUpdate
        extends ApiKeyPolicy("/twilio/event-streams/subscription/update")

    /** Subset of event types to be sent to a sink. */
    case object EventStreamsSubscriptionDelete
        extends ApiKeyPolicy("/twilio/event-streams/subscription/delete")

    /** Sample events sent to a sink for testing and troubleshooting purposes. */
    case object EventStreamsSinkTestCreate
        extends ApiKeyPolicy("/twilio/event-streams/sink.test/create")

    /** Destination capable of receiving a stream of events. */
    case object EventStreamsSinkAll extends ApiKeyPolicy("/twilio/event-streams/sink/*")

    /** Destination capable of receiving a stream of events. */
    case object EventStreamsSinkRead extends ApiKeyPolicy("/twilio/event-streams/sink/read")

    /** Destination capable of receiving a stream of events. */
    case object EventStreamsSinkList extends ApiKeyPolicy("/twilio/event-streams/sink/list")

    /** Destination capable of receiving a stream of events. */
    case object EventStreamsSinkCreate extends ApiKeyPolicy("/twilio/event-streams/sink/create")

    /** Destination capable of receiving a stream of events. */
    case object EventStreamsSinkUpdate extends ApiKeyPolicy("/twilio/event-streams/sink/update")

    /** Destination capable of receiving a stream of events. */
    case object EventStreamsSinkDelete extends ApiKeyPolicy("/twilio/event-streams/sink/delete")

    /** A kind of event described by a schema which can be subscribed to. */
    case object EventStreamsEventTypeRead
        extends ApiKeyPolicy("/twilio/event-streams/event-type/read")

    /** A kind of event described by a schema which can be subscribed to. */
    case object EventStreamsEventTypeList
        extends ApiKeyPolicy("/twilio/event-streams/event-type/list")

    /** Generate Flex Insights Historical reports */
    case object FlexInsightsHistoricalReportsRead
        extends ApiKeyPolicy("/twilio/flex/insights.historical-reports/read")

    /** Generate Flex Insights Historical reports */
    case object FlexInsightsHistoricalReportsCreate
        extends ApiKeyPolicy("/twilio/flex/insights.historical-reports/create")

    /** Generates a self-signed OpenSSL certificate to authenticate calls to Salesforce telephony
      * APIs
      */
    case object FlexScvCertificateCreate extends ApiKeyPolicy("/twilio/flex/scv-certificate/create")

    /** Creates and returns a Microvisor device certificate based on a provided CSR, registers the
      * associated device with the calling account
      */
    case object MicrovisorDeviceCertCreate
        extends ApiKeyPolicy("/twilio/microvisor/device-cert/create")

    /** Recording settings allows Twilio to store your recordings encrypted. */
    case object VideoRecordingsRecordingSettingsRead
        extends ApiKeyPolicy("/twilio/video/recordings.recording-settings/read")

    /** Recording settings allows Twilio to store your recordings encrypted. */
    case object VideoRecordingsRecordingSettingsUpdate
        extends ApiKeyPolicy("/twilio/video/recordings.recording-settings/update")

    /** Captured recordings are single-track, single-media and stored in a single file format. */
    case object VideoRecordingsRead extends ApiKeyPolicy("/twilio/video/recordings/read")

    /** Captured recordings are single-track, single-media and stored in a single file format. */
    case object VideoRecordingsList extends ApiKeyPolicy("/twilio/video/recordings/list")

    /** Captured recordings are single-track, single-media and stored in a single file format. */
    case object VideoRecordingsDelete extends ApiKeyPolicy("/twilio/video/recordings/delete")

    /** Anonymize room participant identity. */
    case object VideoRoomsParticipantsAnonymizeUpdate
        extends ApiKeyPolicy("/twilio/video/rooms.participants.anonymize/update")

    /** Recording rules that are enforced in a given Room. */
    case object VideoRoomsRecordingRulesList
        extends ApiKeyPolicy("/twilio/video/rooms.recording-rules/list")

    /** Recording rules that are enforced in a given Room. */
    case object VideoRoomsRecordingRulesUpdate
        extends ApiKeyPolicy("/twilio/video/rooms.recording-rules/update")

    /** Represents media shared in a Video Room by a Participant, including audio, video, and screen
      * share content.
      */
    case object VideoRoomsParticipantsPublishedTracksRead
        extends ApiKeyPolicy("/twilio/video/rooms.participants.published-tracks/read")

    /** Represents media shared in a Video Room by a Participant, including audio, video, and screen
      * share content.
      */
    case object VideoRoomsParticipantsPublishedTracksList
        extends ApiKeyPolicy("/twilio/video/rooms.participants.published-tracks/list")

    /** Represents participants currently connected to a given Room. */
    case object VideoRoomsParticipantsRead
        extends ApiKeyPolicy("/twilio/video/rooms.participants/read")

    /** Represents participants currently connected to a given Room. */
    case object VideoRoomsParticipantsList
        extends ApiKeyPolicy("/twilio/video/rooms.participants/list")

    /** Represents participants currently connected to a given Room. */
    case object VideoRoomsParticipantsUpdate
        extends ApiKeyPolicy("/twilio/video/rooms.participants/update")

    /** Represents the subscribe rules that are enforced on a given Participant. */
    case object VideoRoomsParticipantsSubscribeRulesList
        extends ApiKeyPolicy("/twilio/video/rooms.participants.subscribe-rules/list")

    /** Represents the subscribe rules that are enforced on a given Participant. */
    case object VideoRoomsParticipantsSubscribeRulesUpdate
        extends ApiKeyPolicy("/twilio/video/rooms.participants.subscribe-rules/update")

    /** Represents a Participant's Track Subscription, managing which media streams participants
      * receive.
      */
    case object VideoRoomsParticipantsSubscribedTracksRead
        extends ApiKeyPolicy("/twilio/video/rooms.participants.subscribed-tracks/read")

    /** Represents a Participant's Track Subscription, managing which media streams participants
      * receive.
      */
    case object VideoRoomsParticipantsSubscribedTracksList
        extends ApiKeyPolicy("/twilio/video/rooms.participants.subscribed-tracks/list")

    /** Single-track, single-media room recordings. */
    case object VideoRoomsRecordingsRead extends ApiKeyPolicy("/twilio/video/rooms.recordings/read")

    /** Single-track, single-media room recordings. */
    case object VideoRoomsRecordingsList extends ApiKeyPolicy("/twilio/video/rooms.recordings/list")

    /** Multi-party communications session where participants share real-time audio and video
      * tracks.
      */
    case object VideoRoomsRead extends ApiKeyPolicy("/twilio/video/rooms/read")

    /** Multi-party communications session where participants share real-time audio and video
      * tracks.
      */
    case object VideoRoomsList extends ApiKeyPolicy("/twilio/video/rooms/list")

    /** Multi-party communications session where participants share real-time audio and video
      * tracks.
      */
    case object VideoRoomsCreate extends ApiKeyPolicy("/twilio/video/rooms/create")

    /** Multi-party communications session where participants share real-time audio and video
      * tracks.
      */
    case object VideoRoomsUpdate extends ApiKeyPolicy("/twilio/video/rooms/update")

    /** Transcriptions in video rooms. */
    case object VideoRoomsTranscriptionsRead
        extends ApiKeyPolicy("/twilio/video/rooms.transcriptions/read")

    /** Transcriptions in video rooms. */
    case object VideoRoomsTranscriptionsList
        extends ApiKeyPolicy("/twilio/video/rooms.transcriptions/list")

    /** Transcriptions in video rooms. */
    case object VideoRoomsTranscriptionsCreate
        extends ApiKeyPolicy("/twilio/video/rooms.transcriptions/create")

    /** Transcriptions in video rooms. */
    case object VideoRoomsTranscriptionsUpdate
        extends ApiKeyPolicy("/twilio/video/rooms.transcriptions/update")

    /** Media file created as a result of applying a set of media processing operations onto a
      * number of source Recordings.
      */
    case object VideoCompositionsRead extends ApiKeyPolicy("/twilio/video/compositions/read")

    /** Media file created as a result of applying a set of media processing operations onto a
      * number of source Recordings.
      */
    case object VideoCompositionsList extends ApiKeyPolicy("/twilio/video/compositions/list")

    /** Media file created as a result of applying a set of media processing operations onto a
      * number of source Recordings.
      */
    case object VideoCompositionsCreate extends ApiKeyPolicy("/twilio/video/compositions/create")

    /** Media file created as a result of applying a set of media processing operations onto a
      * number of source Recordings.
      */
    case object VideoCompositionsDelete extends ApiKeyPolicy("/twilio/video/compositions/delete")

    /** Composition settings for account. */
    case object VideoCompositionsCompositionSettingsRead
        extends ApiKeyPolicy("/twilio/video/compositions.composition-settings/read")

    /** Composition settings for account. */
    case object VideoCompositionsCompositionSettingsUpdate
        extends ApiKeyPolicy("/twilio/video/compositions.composition-settings/update")

    /** Recording composition hooks. */
    case object VideoCompositionsCompositionHooksRead
        extends ApiKeyPolicy("/twilio/video/compositions.composition-hooks/read")

    /** Recording composition hooks. */
    case object VideoCompositionsCompositionHooksList
        extends ApiKeyPolicy("/twilio/video/compositions.composition-hooks/list")

    /** Recording composition hooks. */
    case object VideoCompositionsCompositionHooksCreate
        extends ApiKeyPolicy("/twilio/video/compositions.composition-hooks/create")

    /** Recording composition hooks. */
    case object VideoCompositionsCompositionHooksUpdate
        extends ApiKeyPolicy("/twilio/video/compositions.composition-hooks/update")

    /** Recording composition hooks. */
    case object VideoCompositionsCompositionHooksDelete
        extends ApiKeyPolicy("/twilio/video/compositions.composition-hooks/delete")

    /** Recording composition hooks. */
    case object VideoCompositionsCompositionHooksAll
        extends ApiKeyPolicy("/twilio/video/compositions.composition-hooks/*")

    override val values: IndexedSeq[ApiKeyPolicy] = findValues
  }
}
