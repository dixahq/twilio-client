package com.dixa.twilio.client.impl.voice

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpMethods
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, Sink}
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.impl.voice.ConferenceJsonRep.TwilioConferenceJsonResp
import com.dixa.twilio.client.impl._
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Conference
import com.dixa.twilio.model.voice.Conference.ConferenceWithParticipants
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

import scala.annotation.nowarn
import scala.concurrent.ExecutionContext

private[impl] object FetchAllConferencesWithParticipantsRequest {

  @nowarn // Haven's figured out how to handel this like a MultipleRequestExecutor
  def apply(
      connSettings: TwilioConnectionSettings,
      statusFilter: Option[Conference.Status]
  )(
      implicit http: HttpExt,
      materializer: Materializer,
      executionContext: ExecutionContext,
      apiVersion: ApiVersion
  ): Flow[TwilioAccount.Sid, ConferenceWithParticipants, NotUsed] = Flow[TwilioAccount.Sid]
    .flatMapMerge(
      connSettings.parallelFactor.asInt,
      accountSid =>
        {
          val statusParam = statusFilter.map(f => s"Status=${f.twilioString}&").getOrElse("")
          val initPath = TwilioUri.createPathUnsafe(
            ApiSubDomain.Api,
            HttpMethods.GET,
            s"/${apiVersion.twilioString}/Accounts/$accountSid/Conferences.json?${statusParam}PageSize=1000"
          )
          TwilioPagingFlow.createPagingSrc(connSettings, initPath)
        }
          .map(entityToConferenceJsonRep)
          .mapConcat(identity)
    )
    // Just fetch all participants into memory. In most cases there will only be 2, and Twilios
    // max is 250, so it should be no problem fitting them all into memory at the same time
    .mapAsync(connSettings.parallelFactor.asInt) { confJs =>
      @nowarn // Haven's figured out how to handel this like a MultipleRequestExecutor
      val fut = TwilioPagingFlow
        .createPagingSrc(
          connSettings,
          TwilioUri
            .autoDetectUnsafe(
              confJs.subresource_uris.participants,
              HttpMethods.GET,
              ApiSubDomain.Api
            )
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

  private implicit val twilioConferenceOuterJsonRepReader: Reader[TwilioConferenceOuterJsonRep] =
    macroR[TwilioConferenceOuterJsonRep]

  private def entityToConferenceJsonRep(entity: HttpEntityString): Seq[TwilioConferenceJsonResp] = {
    val decoded = entity.parse[TwilioConferenceOuterJsonRep]().toTry.get
    decoded.conferences
  }

  private def entityToParticipantList(
      entity: HttpEntityString
  ): Seq[Conference.Participant] = {
    val decoded = entity.parse[ParticipantListJsonRep]()
    decoded.toTry.get.participants.map { _.toModel }
  }
}
