package com.dixa.twilio.client.impl.messaging

// Response example: https://www.twilio.com/docs/sms/send-messages
private[messaging] final case class MessageSendRespJsonRep(
    account_sid: String,
    api_version: String,
    body: String,
    date_created: String,
    date_sent: String,
    date_updated: String,
    direction: String,
    error_code: String,
    error_message: String,
    from: String,
    messaging_service_sid: String,
    num_media: String,
    num_segments: String,
    price: String,
    price_unit: String,
    sid: String,
    status: String,
    subresource_uris: String,
    to: String,
    uri: String
)
