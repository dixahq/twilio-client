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

package com.dixa.twilio.client.general

trait TwilioClientGeneral {

  /** Create a Usage Trigger.
    *
    * @see
    *   https://www.twilio.com/docs/usage/api/usage-trigger#create-a-usagetrigger-resource
    */
  def usageTriggerCreate: UsageTriggerCreateRequestExecutor

  /** Read all Usage Triggers.
    *
    * @see
    *   https://www.twilio.com/docs/usage/api/usage-trigger#read-multiple-usagetrigger-resources
    */
  def usageTriggerRead: UsageTriggerReadRequestExecutor

  /** Delete Usage Triggers.
    *
    * @see
    *   https://www.twilio.com/docs/usage/api/usage-trigger#delete-a-usagetrigger-resource
    */
  def usageTriggerDelete: UsageTriggerDeleteRequestExecutor

  /** Create an Application (TwimlApp).
    *
    * @see
    *   https://www.twilio.com/docs/usage/api/applications#create-an-application-resource
    */
  def applicationCreate: ApplicationCreateRequestExecutor

  /** Delete an Application (TwimlApp)
    *
    * If this application's sid is assigned to any IncomingPhoneNumber resources as a
    * VoiceApplicationSid or SmsApplicationSid it will be removed.
    *
    * @see
    *   https://www.twilio.com/docs/usage/api/applications#delete-an-application-resource
    */
  def applicationDelete: ApplicationDeleteRequestExecutor

  /** Read all applications (TwimlApps) from a subaccount.
    *
    * @see
    *   https://www.twilio.com/docs/usage/api/applications#read-multiple-application-resources
    */
  def applicationRead: ApplicationReadRequestExecutor
}
