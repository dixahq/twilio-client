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

package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

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
