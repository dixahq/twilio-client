package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.iam.TwilioAccount.Sid

import java.time.Instant

/** Represents the details/reference that identifies a media resource in twilio.
  * @see
  *   https://www.twilio.com/docs/sms/api/media-resource
  */
final case class MediaResourceReference(
    sid: Media.Sid,
    accountSid: Sid,
    parentSid: MessageSid,
    contentType: String,
    dateCreated: Instant,
    dateUpdated: Instant,
    resourceUrl: MediaResourceUrl
)
