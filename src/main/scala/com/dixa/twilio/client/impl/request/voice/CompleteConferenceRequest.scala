package com.dixa.twilio.client.impl.request.voice

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, StatusCodes}
import akka.stream.Materializer
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.request.voice.ConferenceJsonResp.TwilioConferenceJsonResp
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.model.voice.TwilioConference
import io.circe.generic.auto._
import org.scalactic.TypeCheckedTripleEquals._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

private[impl] object CompleteConferenceRequest {

  def apply(
      connSettings: TwilioConnectionSettings,
      conference: TwilioConference
  )(
      implicit http: HttpExt,
      materializer: Materializer,
      executionContext: ExecutionContext
  ): Future[TwilioConference] = {
    val req = TwilioPath(
      ApiSubDomain.Api,
      HttpMethods.POST,
      s"/2010-04-01/Accounts/${conference.accountSid}/Conferences/${conference.sid}.json"
    )
      .createHttpRequest(connSettings)
      .withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, "Status=completed"))
    http.singleRequest(req).flatMap { resp =>
      if (resp.status !== StatusCodes.OK) {
        throw new IllegalStateException(
          s"Could not close conference: $conference, due to getting status code ${resp.status} from Twilio"
        )
      }
      resp.entity.toStrict(connSettings.timeouts.requestEntityTimeout).map { entity =>
        val entityString = HttpEntityString(entity.data.utf8String)
        val decoded      = entityString.parseUnsafe[TwilioConferenceJsonResp]()
        decoded.toModel
      }
    }
  }
}
