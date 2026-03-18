// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
