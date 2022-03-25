package com.dixa.twilio.client.impl.messaging

// Response example: https://www.twilio.com/docs/sms/send-messages
// Message properties/Response entity in more detail: https://www.twilio.com/docs/sms/api/message-resource#message-properties
private[messaging] final case class MessageJsonRep(
    sid: String,
    date_created: Option[String],
    date_updated: Option[String],
    date_sent: Option[String],
    account_sid: String,
    to: String,
    from: String,
    messaging_service_sid: Option[String],
    body: String,
    status: String,
    num_segments: String,
    num_media: String,
    direction: String,
    api_version: String,
    price: Option[String],
    price_unit: Option[String],
    error_code: Option[Int],
    error_message: Option[String],
    uri: String,
    subresource_uris: SubresourceUris
)
private[messaging] final case class SubresourceUris(media: String)
