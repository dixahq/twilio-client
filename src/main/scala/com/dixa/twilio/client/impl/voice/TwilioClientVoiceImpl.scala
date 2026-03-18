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

package com.dixa.twilio.client.impl.voice

import org.apache.pekko.NotUsed
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Flow
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.impl.ApiVersion
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Conference
import com.dixa.twilio.model.voice.Conference.ConferenceWithParticipants
import com.dixa.twilio.client.voice.{
  CallCreateRequestExecutor,
  CallFetchRequestExecutor,
  CallReadRequestExecutor,
  CallRecordingCreateRequestExecutor,
  CallRecordingReadRequestExecutor,
  CallRecordingUpdateRequestExecutor,
  CallUpdateRequestExecutor,
  ConferenceParticipantDeleteRequestExecutor,
  ConferenceParticipantReadRequestExecutor,
  ConferenceParticipantUpdateRequestExecutor,
  ConferenceReadRequestExecutor,
  ConferenceRecordingReadRequestExecutor,
  ConferenceRecordingUpdateRequestExecutor,
  ConferenceUpdateRequestExecutor,
  IpAccessControlListCreateRequestExecutor,
  IpAccessControlListMappingCreateRequestExecutor,
  IpAccessControlListReadRequestExecutor,
  QueueFetchRequestExecutor,
  QueueUpdateRequestExecutor,
  RecordingDeleteRequestExecutor,
  RecordingFetchRequestExecutor,
  RecordingReadRequestExecutor,
  SipDomainCreateRequestExecutor,
  SipDomainReadRequestExecutor,
  SipIpAddressCreateRequestExecutor,
  SipIpAddressDeleteRequestExecutor,
  SipIpAddressFetchRequestExecutor,
  SipIpAddressReadRequestExecutor,
  SipIpAddressUpdateRequestExecutor,
  TwilioClientVoice
}

import scala.concurrent.ExecutionContext

private[impl] final class TwilioClientVoiceImpl()(
    implicit materializer: Materializer,
    executionContext: ExecutionContext,
    httpExt: HttpExt
) extends TwilioClientVoice {

  private implicit val apiVersion: ApiVersion = ApiVersion.`2010-04-01`

  override val callCreate: CallCreateRequestExecutor = new CallCreateRequestExecutorImpl()

  override val callFetch: CallFetchRequestExecutor = new CallFetchRequestExecutorImpl()

  override val callUpdate: CallUpdateRequestExecutor = new CallUpdateRequestExecutorImpl()

  override val callRead: CallReadRequestExecutor = new CallReadRequestExecutorImpl()

  override val callRecordingCreate: CallRecordingCreateRequestExecutor =
    new CallRecordingCreateRequestExecutorImpl()

  override val callRecordingRead: CallRecordingReadRequestExecutor =
    new CallRecordingReadRequestExecutorImpl()

  override val callRecordingUpdate: CallRecordingUpdateRequestExecutor =
    new CallRecordingUpdateRequestExecutorImpl()

  override val queueUpdate: QueueUpdateRequestExecutor = new QueueUpdateRequestExecutorImpl()

  override val queueFetch: QueueFetchRequestExecutor = new QueueFetchRequestExecutorImpl()

  override val conferenceRead: ConferenceReadRequestExecutor =
    new ConferenceReadRequestExecutorImpl()

  override val conferenceUpdate: ConferenceUpdateRequestExecutor =
    new ConferenceUpdateRequestExecutorImpl()

  override val conferenceRecordingRead: ConferenceRecordingReadRequestExecutor =
    new ConferenceRecordingReadRequestExecutorImpl()

  override val conferenceRecordingUpdate: ConferenceRecordingUpdateRequestExecutor =
    new ConferenceRecordingUpdateRequestExecutorImpl()

  override val conferenceParticipantsRead: ConferenceParticipantReadRequestExecutor =
    new ConferenceParticipantReadRequestExecutorImpl()

  override val conferenceParticipantUpdate: ConferenceParticipantUpdateRequestExecutor =
    new ConferenceParticipantUpdateRequestExecutorImpl()

  override val conferenceParticipantDelete: ConferenceParticipantDeleteRequestExecutor =
    new ConferenceParticipantDeleteRequestExecutorImpl()

  override def fetchAllConferencesWithParticipants(
      connSettings: TwilioConnectionSettings,
      statusFilter: Option[Conference.Status]
  ): Flow[TwilioAccount.Sid, ConferenceWithParticipants, NotUsed] =
    FetchAllConferencesWithParticipantsRequest(connSettings, statusFilter)

  override val recordingFetch: RecordingFetchRequestExecutor =
    new RecordingFetchRequestExecutorImpl()

  override val recordingRead: RecordingReadRequestExecutor = new RecordingReadRequestExecutorImpl()

  override val recordingDelete: RecordingDeleteRequestExecutor =
    new RecordingDeleteRequestExecutorImpl()

  override val sipDomainCreate: SipDomainCreateRequestExecutor =
    new SipDomainCreateRequestExecutorImpl()

  override val sipDomainRead: SipDomainReadRequestExecutor = new SipDomainReadRequestExecutorImpl()

  override val ipAccessControlListCreate: IpAccessControlListCreateRequestExecutor =
    new IpAccessControlListCreateRequestExecutorImpl()

  override val ipAccessControlListRead: IpAccessControlListReadRequestExecutor =
    new IpAccessControlListReadRequestExecutorImpl()

  override val sipIpAddressCreate: SipIpAddressCreateRequestExecutor =
    new SipIpAddressCreateRequestExecutorImpl()

  override val sipIpAddressFetch: SipIpAddressFetchRequestExecutor =
    new SipIpAddressFetchRequestExecutorImpl()

  override val sipIpAddressRead: SipIpAddressReadRequestExecutor =
    new SipIpAddressReadRequestExecutorImpl()

  override val sipIpAddressUpdate: SipIpAddressUpdateRequestExecutor =
    new SipIpAddressUpdateRequestExecutorImpl()

  override val sipIpAddressDelete: SipIpAddressDeleteRequestExecutor =
    new SipIpAddressDeleteRequestExecutorImpl()

  override val ipAccessControlListMappingCreate: IpAccessControlListMappingCreateRequestExecutor =
    new IpAccessControlListMappingCreateRequestExecutorImpl()
}
