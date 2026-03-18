// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.model.callback.CallbackUrl.MessageStatusCallback
import com.dixa.twilio.model.messaging.TwilioMessagingService.{
  FallbackWebhook,
  FriendlyName,
  InboundRequestWebhook,
  UseInboundWebhookOnNumber
}
import com.dixa.twilio.model.messaging.TwilioMessagingService

import scala.concurrent.Future

trait TwilioClientMessaging {

  def servicesRead: ServicesReadRequestExecutor

  def serviceCreate(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.ServiceCreateRequest
  ): Future[TwilioMessagingService]

  /** Add a phone number to a messaging service.
    *
    * The create is only in context of messaging. So it takes an existing phone number and creates
    * it / adds it in a messaging service.
    */
  def phoneNumberCreate: PhoneNumberCreateRequestExecutor

  /** Delete a phone number from a messaging service.
    *
    * The delete is only in context of messaging. So it takes an existing phonenumber and delete it
    * / removes it from a messaging service.
    */
  def phoneNumberDelete: PhoneNumberDeleteRequestExecutor

  def messageSend: MessageSendRequestExecutor

  /** Lists the media resources from a given account and message sid.
    */
  def mediaResourceRead: MessageMediaResourceReadRequestExecutor

  /** Returns a Source of messages from a given account, can be filtered based on to and/or from
    * phone number and sent date
    */
  def messageResourceRead: MessageResourceReadRequestExecutor

  /** Returns channel sender for a given messenger service integration with third party based on a
    * channel sender sid
    */
  def channelsSendersFetch: ChannelsSendersFetchRequestExecutor

  /** Creates a channel sender for a specific channel
    */
  def channelsSendersCreate: ChannelsSendersCreateRequestExecutor

  /** Send channel sender verification code
    */
  def channelsSendersVerification: ChannelsSendersVerificationRequestExecutor

  /** Deletes a channel sender for a specific channel
    */
  def channelsSendersDelete: ChannelsSendersDeleteRequestExecutor

  /** Lists channel senders, optionally filtered by sender_id
    */
  def channelsSendersList: ChannelsSendersListRequestExecutor

  /** Sends a WhatsApp typing indicator
    */
  def typingIndicatorSend: TypingIndicatorSendRequestExecutor
}

object TwilioClientMessaging {

  final case class ServiceCreateRequest(
      friendlyName: FriendlyName,
      inboundRequestWebhook: Option[InboundRequestWebhook],
      fallbackWebhook: Option[FallbackWebhook],
      statusCallback: Option[MessageStatusCallback],
      useInboundWebhookOnNumber: UseInboundWebhookOnNumber
  )
}
