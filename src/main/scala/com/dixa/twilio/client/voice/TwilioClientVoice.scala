package com.dixa.twilio.client.voice

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.model.voice.TwilioConference
import com.dixa.twilio.client.model.voice.TwilioConference.TwilioConferenceWithParticipants

import scala.concurrent.Future

trait TwilioClientVoice {
  def fetchAllConferencesWithParticipants(
      connSettings: TwilioConnectionSettings,
      statusFilter: Option[TwilioConference.Status]
  ): Flow[TwilioAccount.Sid, TwilioConferenceWithParticipants, NotUsed]

  def completeConference(
      connSettings: TwilioConnectionSettings,
      conference: TwilioConference
  ): Future[TwilioConference]
}
