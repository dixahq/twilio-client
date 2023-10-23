package com.dixa.twilio.client.impl.voice

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
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
  IpAccessControlListReadRequestExecutor,
  QueueFetchRequestExecutor,
  QueueUpdateRequestExecutor,
  RecordingDeleteRequestExecutor,
  RecordingFetchRequestExecutor,
  RecordingReadRequestExecutor,
  SipDomainCreateRequestExecutor,
  SipDomainReadRequestExecutor,
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

  override def sipDomainCreate: SipDomainCreateRequestExecutor =
    new SipDomainCreateRequestExecutorImpl()

  override def sipDomainRead: SipDomainReadRequestExecutor = new SipDomainReadRequestExecutorImpl()

  override def ipAccessControlListCreate: IpAccessControlListCreateRequestExecutor =
    new IpAccessControlListCreateRequestExecutorImpl()

  override def ipAccessControlListRead: IpAccessControlListReadRequestExecutor =
    new IpAccessControlListReadRequestExecutorImpl()
}
