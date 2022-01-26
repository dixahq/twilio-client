package com.dixa.twilio.client.implDetails

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.dixa.twilio.client.implDetails.request.voice.{
  CompleteConferenceRequest,
  FetchAllConferencesWithParticipantsRequest
}
import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.model.voice.TwilioConference
import com.dixa.twilio.client.model.voice.TwilioConference.TwilioConferenceWithParticipants
import com.dixa.twilio.client.{TwilioClientVoice, TwilioConnectionSettings}

import scala.concurrent.{ExecutionContext, Future}

private[implDetails] final class TwilioClientVoiceImpl()(
    implicit materializer: Materializer,
    executionContext: ExecutionContext,
    httpExt: HttpExt
) extends TwilioClientVoice {

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
