package com.dixa.twilio.client.voice

import akka.NotUsed
import akka.stream.scaladsl.Flow
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
}
