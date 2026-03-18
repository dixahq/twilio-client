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

package com.dixa.twilio.client.voice

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.Flow
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Conference
import com.dixa.twilio.model.voice.Conference.ConferenceWithParticipants

trait TwilioClientVoice {

  def callCreate: CallCreateRequestExecutor

  def callFetch: CallFetchRequestExecutor

  def callUpdate: CallUpdateRequestExecutor

  def callRead: CallReadRequestExecutor

  def callRecordingCreate: CallRecordingCreateRequestExecutor

  def callRecordingRead: CallRecordingReadRequestExecutor

  def callRecordingUpdate: CallRecordingUpdateRequestExecutor

  def queueUpdate: QueueUpdateRequestExecutor

  def queueFetch: QueueFetchRequestExecutor

  def conferenceRead: ConferenceReadRequestExecutor

  def conferenceUpdate: ConferenceUpdateRequestExecutor

  def conferenceRecordingRead: ConferenceRecordingReadRequestExecutor

  def conferenceRecordingUpdate: ConferenceRecordingUpdateRequestExecutor

  def conferenceParticipantsRead: ConferenceParticipantReadRequestExecutor

  def conferenceParticipantUpdate: ConferenceParticipantUpdateRequestExecutor

  def conferenceParticipantDelete: ConferenceParticipantDeleteRequestExecutor

  def fetchAllConferencesWithParticipants(
      connSettings: TwilioConnectionSettings,
      statusFilter: Option[Conference.Status]
  ): Flow[TwilioAccount.Sid, ConferenceWithParticipants, NotUsed]

  def recordingFetch: RecordingFetchRequestExecutor

  def recordingRead: RecordingReadRequestExecutor

  def recordingDelete: RecordingDeleteRequestExecutor

  def sipDomainCreate: SipDomainCreateRequestExecutor

  /** Read all sip domains of account
    *
    * @see
    *   https://www.twilio.com/docs/voice/sip/api/sip-domain-resource#read-multiple-sipdomain-resources
    */
  def sipDomainRead: SipDomainReadRequestExecutor

  /** See [[IpAccessControlListCreateRequestExecutor]] */
  def ipAccessControlListCreate: IpAccessControlListCreateRequestExecutor

  /** See [[IpAccessControlListReadRequestExecutor]] */
  def ipAccessControlListRead: IpAccessControlListReadRequestExecutor

  /** See [[SipIpAddressCreateRequestExecutor]] */
  def sipIpAddressCreate: SipIpAddressCreateRequestExecutor

  /** See [[SipIpAddressFetchRequestExecutor]] */
  def sipIpAddressFetch: SipIpAddressFetchRequestExecutor

  /** See [[SipIpAddressReadRequestExecutor]] */
  def sipIpAddressRead: SipIpAddressReadRequestExecutor

  /** See [[SipIpAddressUpdateRequestExecutor]] */
  def sipIpAddressUpdate: SipIpAddressUpdateRequestExecutor

  /** See [[SipIpAddressDeleteRequestExecutor]] */
  def sipIpAddressDelete: SipIpAddressDeleteRequestExecutor

  /** See [[IpAccessControlListMappingCreateRequestExecutor]] */
  def ipAccessControlListMappingCreate: IpAccessControlListMappingCreateRequestExecutor
}
