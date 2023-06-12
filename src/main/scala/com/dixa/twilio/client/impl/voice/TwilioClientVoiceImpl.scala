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
  CallUpdateRequestExecutor,
  ConferenceParticipantDeleteRequestExecutor,
  ConferenceParticipantReadRequestExecutor,
  ConferenceParticipantUpdateRequestExecutor,
  ConferenceReadRequestExecutor,
  ConferenceUpdateRequestExecutor,
  QueueUpdateRequestExecutor,
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

  override val callUpdate: CallUpdateRequestExecutor = new CallUpdateRequestExecutorImpl()

  override val queueUpdate: QueueUpdateRequestExecutor = new QueueUpdateRequestExecutorImpl()

  override val conferenceRead: ConferenceReadRequestExecutor =
    new ConferenceReadRequestExecutorImpl()

  override val conferenceUpdate: ConferenceUpdateRequestExecutor =
    new ConferenceUpdateRequestExecutorImpl()

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
}
