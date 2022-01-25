package com.dixa.twilio.client

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Source}
import com.dixa.twilio.client.implDetails.TwilioClientImpl
import com.dixa.twilio.client.model.TwilioConference.TwilioConferenceWithParticipants
import com.dixa.twilio.client.model.{TwilioAccount, TwilioConference, TwilioConnectionSettings}

import scala.concurrent.{ExecutionContext, Future}

trait TwilioClient {
  def fetchAllAccounts(connSettings: TwilioConnectionSettings): Source[TwilioAccount, NotUsed]

  def fetchAllConferencesWithParticipants(
      connSettings: TwilioConnectionSettings,
      statusFilter: Option[TwilioConference.Status]
  ): Flow[TwilioAccount, TwilioConferenceWithParticipants, NotUsed]

  def completeConference(
      connSettings: TwilioConnectionSettings,
      conference: TwilioConference
  ): Future[TwilioConference]
}

object TwilioClient {
  def defaultImpl()(
      implicit actorSystem: ActorSystem,
      executionContext: ExecutionContext
  ): TwilioClient = new TwilioClientImpl()
}
