package com.dixa.twilio.client.implDetails

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.scaladsl.{Flow, Source}
import com.dixa.twilio.client.TwilioClient
import com.dixa.twilio.client.implDetails.request.account.FetchAllAccountsRequest
import com.dixa.twilio.client.implDetails.request.conference.{
  CompleteConferenceRequest,
  FetchAllConferencesWithParticipantsRequest
}
import com.dixa.twilio.client.model.TwilioConference.TwilioConferenceWithParticipants
import com.dixa.twilio.client.model.{TwilioAccount, TwilioConference, TwilioConnectionSettings}

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

  import TwilioClientImpl._

  private implicit val http: HttpExt = Http()

  override def fetchAllAccounts(
      connSettings: TwilioConnectionSettings
  ): Source[TwilioAccount, NotUsed] = FetchAllAccountsRequest(connSettings)

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

private object TwilioClientImpl {

  /** Some operation runs different layers of paralellism in there streams, and for these using
    * around half the cores, with a minimum of 2 for each is a valid guess.
    */
  private implicit val parallelism: request.RequestParallelFactor = {
    val halfCpuMin2 = Runtime.getRuntime.availableProcessors() match {
      case numberOfCores if numberOfCores < 4 => 2
      case numberOfCores                      => numberOfCores / 2
    }
    request.RequestParallelFactor(halfCpuMin2)
  }
}
