package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.TwilioClientPickler.{Reader, macroR}

// Response example: https://www.twilio.com/docs/sms/send-messages
// Message properties/Response entity in more detail: https://www.twilio.com/docs/sms/api/message-resource#message-properties
private[messaging] final case class MessageJsonRep(
    sid: String,
    date_created: Option[String] = None,
    date_updated: Option[String] = None,
    date_sent: Option[String] = None,
    account_sid: String,
    to: String,
    from: String,
    messaging_service_sid: Option[String] = None,
    body: String,
    status: String,
    num_segments: String,
    num_media: String,
    direction: String,
    api_version: String,
    price: Option[String] = None,
    price_unit: Option[String] = None,
    error_code: Option[Int] = None,
    error_message: Option[String] = None,
    uri: String,
//    subresource_uris: SubresourceUris
)
//private[messaging] final case class SubresourceUris(media: String)

private[messaging] object MessageJsonRep {

  implicit val messageJsonRepReader: Reader[MessageJsonRep] = macroR[MessageJsonRep]
}
