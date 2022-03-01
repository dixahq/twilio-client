package com.dixa.twilio.client.impl.messaging

// Response example: https://www.twilio.com/docs/sms/send-messages
private[messaging] final case class MessageSendRespJsonRep(
    sid: String,
    date_created: String,
    date_updated: String,
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
    error_code: Option[String],
    error_message: Option[String],
    uri: String,
    subresource_uris: SubresourceUris
)
private[messaging] final case class SubresourceUris(media: String)

//private[messaging] final case class MessageSendRespJsonRep(
//                                                            account_sid: String,
//                                                            body: String,
//                                                            date_created: String,
//                                                            date_sent: String,
//                                                            date_updated: String,
//                                                            direction: String,
//                                                            error_code: String,
//                                                            error_message: String,
//                                                            from: String,
//                                                            messaging_service_sid: String,
//                                                            num_media: String,
//                                                            num_segments: String,
//                                                            price: String,
//                                                            price_unit: String,
//                                                            sid: String,
//                                                            status: String,
//                                                            subresource_uris: String,
//                                                            to: String,
//                                                            uri: String
//                                                          )
