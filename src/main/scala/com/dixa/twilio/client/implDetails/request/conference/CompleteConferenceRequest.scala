package com.dixa.twilio.client.implDetails.request.conference

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, StatusCodes}
import akka.stream.Materializer
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.implDetails.HttpEntityString
import com.dixa.twilio.client.implDetails.request.conference.ConferenceJsonResp.TwilioConferenceJsonResp
import com.dixa.twilio.client.model.TwilioConference
import io.circe.generic.auto._
import org.scalactic.TypeCheckedTripleEquals._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

private[implDetails] object CompleteConferenceRequest {

  def apply(
      connSettings: TwilioConnectionSettings,
      conference: TwilioConference
  )(
      implicit http: HttpExt,
      materializer: Materializer,
      executionContext: ExecutionContext
  ): Future[TwilioConference] = {
    val req = connSettings
      .createBaseRequest(
        HttpMethods.POST,
        s"/2010-04-01/Accounts/${conference.accountSid}/Conferences/${conference.sid}.json"
      )
      .withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, "Status=completed"))
    http.singleRequest(req).flatMap { resp =>
      if (resp.status !== StatusCodes.OK) {
        throw new IllegalStateException(
          s"Could not close conference: $conference, due to getting status code ${resp.status} from Twilio"
        )
      }
      resp.entity.toStrict(30.seconds).map { entity =>
        val entityString = HttpEntityString(entity.data.utf8String)
        val decoded      = entityString.parseUnsafe[TwilioConferenceJsonResp]()
        decoded.toModel
      }
    }
  }

}
