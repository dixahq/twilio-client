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

package com.dixa.twilio.client.phonenumber

trait TwilioClientPhoneNumber {

  /** List all incoming phonenumbers as a Source.
    *
    * A incoming phonenumber is a number that is active in twilio, and is useable for voice
    * communication. Typically also called an ActiveNumber
    *
    * The optional filter will be applied at Twilio side if set. See doc on
    * [[com.dixa.twilio.model.phonenumber.TwilioIncomingPhoneNumber.PhoneNumberFilter]] for details.
    */
  def incomingPhoneNumberList: IncomingNumbersReadRequestExecutor

  /** Delete an incoming phone number from twilio account.
    *
    * An IncomingPhoneNumber instance resource represents a Twilio phone number provisioned from
    * Twilio, ported or hosted to Twilio.
    *
    * @see
    *   https://www.twilio.com/docs/phone-numbers/api/incomingphonenumber-resource#delete-an-incomingphonenumber-resource
    */
  def incomingPhoneNumberDelete: IncomingPhoneNumberDeleteRequestExecutor

  /** Lists active phone numbers for a particular Twilio subaccount as a Source.
    *
    * An active phone number is a number that is active in twilio, and is usable for voice
    * communication. Typically also called an IncomingNumber.
    *
    * The optional filter will be applied at Twilio side if set.
    */
  def activePhoneNumberList: ActiveNumbersReadRequestExecutor

  /** Lists outgoing caller ID's for a particular Twilio subaccount as a Source.
    *
    * A Outgoing caller ID represents a single verified number that may be used as a caller ID when
    * making outgoing calls
    *
    * The optional filter will be applied at Twilio side if set.
    */
  def outgoingCallerIdList: OutgoingCallerIdReadRequestExecutor

  /** Deletes outgoing caller ID for a particular Twilio subaccount
    *
    * A Outgoing caller ID represents a single verified number that may be used as a caller ID when
    * making outgoing calls
    */
  def outgoingCallerIdDelete: OutgoingCallerIdDeleteRequestExecutor

  /** Creates outgoing caller ID for a particular Twilio sub account
    *
    * A Outgoing caller ID represents a single verified number that may be used as a caller ID when
    * making outgoing calls
    *
    * @see
    *   https://www.twilio.com/docs/voice/api/outgoing-caller-ids#http-post
    */
  def outgoingCallerIdCreate: OutgoingCallerIdCreateRequestExecutor

  /** Fetch the routing region for a phone number.
    *
    * Uses Twilio's Voice Routing API to determine which region handles voice traffic for the given
    * number.
    *
    * @see
    *   https://www.twilio.com/docs/global-infrastructure/inbound-processing-region-api-phone-number#fetch-a-phonenumbers-current-inbound-processing-region-configuration
    */
  def phoneNumberRoutingVoiceRead: PhoneNumberRoutingVoiceReadRequestExecutor
}
