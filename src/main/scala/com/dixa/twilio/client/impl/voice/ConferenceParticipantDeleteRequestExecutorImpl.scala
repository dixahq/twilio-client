package com.dixa.twilio.client.impl.voice

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, DefaultApiErrorEntityJsonRep, HttpEntityString}
import com.dixa.twilio.client.voice.ConferenceParticipantDeleteRequestExecutor
import com.dixa.twilio.client.voice.ConferenceParticipantDeleteRequestExecutor.{
  ConferenceParticipantDeleteException,
  ConferenceParticipantDeleteRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.Funit

import scala.concurrent.ExecutionContext

private[client] class ConferenceParticipantDeleteRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ConferenceParticipantDeleteRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ConferenceParticipantDeleteRequestExecutor.ConferenceParticipantDeleteRequest
  ): Either[ConferenceParticipantDeleteException, HttpRequest] = {

    createHttpRequestFor(
      s"/2010-04-01/Accounts/${req.accountSid}/Conference/${req.conferenceSid}/Participant/${req.callSid}.json",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): ConferenceParticipantDeleteException.Api =
    ConferenceParticipantDeleteException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ConferenceParticipantDeleteException.Unspecified =
    ConferenceParticipantDeleteException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: ConferenceParticipantDeleteRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[ConferenceParticipantDeleteException, Funit] = httpResponse.status match {
    case StatusCodes.NoContent => Right(Funit)
    case StatusCodes.NotFound  => buildResultForNotFoundResponse(req, entity)
    case _ => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
  }

  private def buildResultForNotFoundResponse(
      req: ConferenceParticipantDeleteRequest,
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => ConferenceParticipantDeleteException.Unspecified(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(ConferenceParticipantDeleteException.CallNotFound(req.conferenceSid, req.callSid))
          case other =>
            Left(
              ConferenceParticipantDeleteException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represent. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}
