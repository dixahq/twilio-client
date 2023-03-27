package com.dixa.twilio.client.impl.voice

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Conference
import com.dixa.twilio.model.voice.Conference.ConferenceWithParticipants
import com.dixa.twilio.client.voice.{
  CallUpdateRequestExecutor,
  ConferenceParticipantDeleteRequestExecutor,
  ConferenceParticipantReadRequestExecutor,
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

  override val callUpdate: CallUpdateRequestExecutor = new CallUpdateRequestExecutorImpl()

  override val queueUpdate: QueueUpdateRequestExecutor = new QueueUpdateRequestExecutorImpl()

  override val conferenceRead: ConferenceReadRequestExecutor =
    new ConferenceReadRequestExecutorImpl()

  override val conferenceUpdate: ConferenceUpdateRequestExecutor =
    new ConferenceUpdateRequestExecutorImpl()

  override def conferenceParticipantsRead: ConferenceParticipantReadRequestExecutor =
    new ConferenceParticipantReadRequestExecutorImpl()

  override def conferenceParticipantDelete: ConferenceParticipantDeleteRequestExecutor =
    new ConferenceParticipantDeleteRequestExecutorImpl()

  override def fetchAllConferencesWithParticipants(
      connSettings: TwilioConnectionSettings,
      statusFilter: Option[Conference.Status]
  ): Flow[TwilioAccount.Sid, ConferenceWithParticipants, NotUsed] =
    FetchAllConferencesWithParticipantsRequest(connSettings, statusFilter)
}
