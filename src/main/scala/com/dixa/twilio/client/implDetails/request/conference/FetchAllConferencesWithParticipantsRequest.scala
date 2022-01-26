package com.dixa.twilio.client.implDetails.request.conference

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpMethods
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, Sink}
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.implDetails.TwilioUri.TwilioPath
import com.dixa.twilio.client.implDetails.request.conference.ConferenceJsonResp.TwilioConferenceJsonResp
import com.dixa.twilio.client.implDetails.{
  ApiSubDomain,
  HttpEntityString,
  TwilioPagingFlow,
  TwilioUri
}
import com.dixa.twilio.client.model.TwilioConference.TwilioConferenceWithParticipants
import com.dixa.twilio.client.model.{TwilioAccount, TwilioCallSid, TwilioConference}
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

private[implDetails] object FetchAllConferencesWithParticipantsRequest {

  def apply(
      connSettings: TwilioConnectionSettings,
      statusFilter: Option[TwilioConference.Status]
  )(
      implicit http: HttpExt,
      materializer: Materializer,
      executionContext: ExecutionContext
  ): Flow[TwilioAccount, TwilioConferenceWithParticipants, NotUsed] = Flow[TwilioAccount]
    .flatMapMerge(
      connSettings.parallelFactor.asInt,
      account =>
        {
          val statusParam = statusFilter.map(f => s"Status=${f.twilioApiStringRep}&").getOrElse("")
          val initPath = TwilioPath(
            ApiSubDomain.Api,
            HttpMethods.GET,
            s"/2010-04-01/Accounts/${account.sid}/Conferences.json?${statusParam}PageSize=1000"
          )
          TwilioPagingFlow.createPagingSrc(connSettings, initPath)
        }
          .map(entityToConferenceJsonRep)
          .mapConcat(identity)
    )
    // Just fetch all participants into memory. In most cases there will only be 2, and Twilios
    // max is 250, so it should be no problem fitting them all into memory at the same time
    .mapAsync(connSettings.parallelFactor.asInt) { confJs =>
      val fut = TwilioPagingFlow
        .createPagingSrc(
          connSettings,
          TwilioUri
            .autoDetect(confJs.subresource_uris.participants, HttpMethods.GET, ApiSubDomain.Api)
        )
        .map(entityToParticipantList)
        .mapConcat(identity)
        .toMat(Sink.seq)(Keep.right)
        .run()
      fut.map { participantList =>
        confJs.toModel(participantList)
      }
    }

  private final case class TwilioConferenceOuterJsonRep(
      conferences: Vector[TwilioConferenceJsonResp]
  )

  private def entityToConferenceJsonRep(entity: HttpEntityString): Seq[TwilioConferenceJsonResp] = {
    val decoded = entity.parseUnsafe[TwilioConferenceOuterJsonRep]()
    decoded.conferences
  }

  // Only mapped the fields that we actually need for now, there is a lot more
  // info in these responses, that we could map once needed.
  private final case class TwilioConferenceParticipantJsonRep(
      status: String,
      call_sid: String
  )
  private final case class TwilioConferenceParticipantOuterJsonRep(
      participants: Vector[TwilioConferenceParticipantJsonRep]
  )

  private def entityToParticipantList(
      entity: HttpEntityString
  ): Seq[TwilioConference.Participant] = {
    val decoded = entity.parseUnsafe[TwilioConferenceParticipantOuterJsonRep]()
    decoded.participants.map { jsonRep =>
      TwilioConference.Participant(
        TwilioCallSid(jsonRep.call_sid),
        TwilioConference.ParticipantStatus.fromTwilioStringStatus(jsonRep.status)
      )
    }
  }
}
