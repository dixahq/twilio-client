package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.iam.TwilioAccount

import java.time.Instant

case class MessageResource(
    sid: Message.Sid,
    dateCreated: Option[Instant],
    dateUpdated: Option[Instant],
    dateSent: Option[Instant],
    accountSid: TwilioAccount.Sid,
    to: MessageRecipient,
    from: MessageSender,
    messagingServiceSid: Option[TwilioMessagingService.Sid],
    body: MessageBody,
    status: MessageStatus,
    numSegments: MessageNumSegments,
    numMedia: Int,
    direction: MessageDirection,
    price: Option[MessagePrice],
    error: Option[MessageError],
)
