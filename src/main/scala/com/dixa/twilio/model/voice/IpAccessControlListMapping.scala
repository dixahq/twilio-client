package com.dixa.twilio.model.voice

import com.dixa.twilio.model.iam.TwilioAccount

/** Represent a mapping between a [[SipDomain]] and a [[IpAccessControlList]].
  *
  * At point of writing this, the Twilio documentation does not corospond exactly to how their API
  * actually represent this. Because of this, this class diverde a bit from the Twilio doc on these
  * points:
  *   - This resource does not have it's own sid. The sid parameter is always the value of the
  *     SipDomains, and as such this class calls it SipDomainSid.
  *   - Twilio does not seem to record individual timestamp on the mappings, and instead the
  *     resource always returns the timestamps of the parent SipDomain resource. Because of that,
  *     this class has not timestamps.
  *   - There is no friendly name, as twilio does not record a friendly name specific for this sub
  *     resource. The friendly name returned by the Twilio API is the friendly name of the linked
  *     [[IpAccessControlList]]
  *
  * @see
  *   https://www.twilio.com/docs/voice/sip/api/sip-ipaccesscontrollistmapping-resource
  */
final case class IpAccessControlListMapping(
    accountSid: TwilioAccount.Sid,
    sipDomainSid: SipDomain.Sid,
    ipAccessControlListSid: IpAccessControlList.Sid
)
