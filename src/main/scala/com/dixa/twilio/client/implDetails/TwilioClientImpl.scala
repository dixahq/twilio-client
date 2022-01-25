package com.dixa.twilio.client.implDetails

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.scaladsl.{Flow, Source}
import com.dixa.twilio.client
import com.dixa.twilio.client.{
  RequestParallelFactor,
  TwilioClient,
  TwilioClientAccount,
  TwilioClientConference
}
import com.dixa.twilio.client.implDetails.request.account.FetchAllAccountsRequest
import com.dixa.twilio.client.implDetails.request.conference.{
  CompleteConferenceRequest,
  FetchAllConferencesWithParticipantsRequest
}
import com.dixa.twilio.client.model.TwilioConference.TwilioConferenceWithParticipants
import com.dixa.twilio.client.model.{TwilioAccount, TwilioConference}

import scala.concurrent.{ExecutionContext, Future}

/** Default implementation of a TwilioClient.
  *
  * To not make this class way to big and cluttered, each method is implemented in its own object.
  * So all that this class has to do, is to find the right object and call it. See the scaladoc on
  * the package object of [[com.dixa.twilio.client.implDetails.request]]
  */
private[client] final class TwilioClientImpl()(
    implicit actorSystem: ActorSystem,
    executionContext: ExecutionContext
) extends TwilioClient {

  private implicit val http: HttpExt = Http()

  override val account: TwilioClientAccount = new TwilioClientAccountImpl()

  override val conference: TwilioClientConference = new TwilioClientConferenceImpl()
}
