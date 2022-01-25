package com.dixa.twilio.client.implDetails

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.stream.scaladsl.Flow
import com.dixa.twilio.client.{
  RequestParallelFactor,
  TwilioClientConference,
  TwilioConnectionSettings
}
import com.dixa.twilio.client.implDetails.request.conference.{
  CompleteConferenceRequest,
  FetchAllConferencesWithParticipantsRequest
}
import com.dixa.twilio.client.model.{TwilioAccount, TwilioConference}
import com.dixa.twilio.client.model.TwilioConference.TwilioConferenceWithParticipants

import scala.concurrent.{ExecutionContext, Future}

private[implDetails] final class TwilioClientConferenceImpl()(
    implicit actorSystem: ActorSystem,
    executionContext: ExecutionContext,
    httpExt: HttpExt
) extends TwilioClientConference {

  override def fetchAllConferencesWithParticipants(
      connSettings: TwilioConnectionSettings,
      statusFilter: Option[TwilioConference.Status]
  ): Flow[TwilioAccount, TwilioConferenceWithParticipants, NotUsed] =
    FetchAllConferencesWithParticipantsRequest(connSettings, statusFilter)

  override def completeConference(
      connSettings: TwilioConnectionSettings,
      conference: TwilioConference
  ): Future[TwilioConference] = CompleteConferenceRequest(connSettings, conference)
}
