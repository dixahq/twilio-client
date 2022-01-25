package com.dixa.twilio.client

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.dixa.twilio.client.model.TwilioConference.TwilioConferenceWithParticipants
import com.dixa.twilio.client.model.{TwilioAccount, TwilioConference, TwilioConnectionSettings}

import scala.concurrent.Future

trait TwilioClientConference {
  def fetchAllConferencesWithParticipants(
      connSettings: TwilioConnectionSettings,
      statusFilter: Option[TwilioConference.Status]
  ): Flow[TwilioAccount, TwilioConferenceWithParticipants, NotUsed]

  def completeConference(
      connSettings: TwilioConnectionSettings,
      conference: TwilioConference
  ): Future[TwilioConference]
}
