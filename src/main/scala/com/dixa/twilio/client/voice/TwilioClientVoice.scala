package com.dixa.twilio.client.voice

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Conference
import com.dixa.twilio.model.voice.Conference.ConferenceWithParticipants

trait TwilioClientVoice {

  def callCreate: CallCreateRequestExecutor

  def callUpdate: CallUpdateRequestExecutor

  def callRead: CallReadRequestExecutor

  def queueUpdate: QueueUpdateRequestExecutor

  def queueFetch: QueueFetchRequestExecutor

  def conferenceRead: ConferenceReadRequestExecutor

  def conferenceUpdate: ConferenceUpdateRequestExecutor

  def conferenceParticipantsRead: ConferenceParticipantReadRequestExecutor

  def conferenceParticipantUpdate: ConferenceParticipantUpdateRequestExecutor

  def conferenceParticipantDelete: ConferenceParticipantDeleteRequestExecutor

  def fetchAllConferencesWithParticipants(
      connSettings: TwilioConnectionSettings,
      statusFilter: Option[Conference.Status]
  ): Flow[TwilioAccount.Sid, ConferenceWithParticipants, NotUsed]
}
