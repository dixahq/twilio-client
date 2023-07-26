package com.dixa.twilio.client.impl.voice

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.voice.RecordingDeleteRequestExecutor
import com.dixa.twilio.client.voice.RecordingDeleteRequestExecutor.{
  RecordingDeleteRequest,
  RecordingDeleteRequestException
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}

import scala.concurrent.ExecutionContext

private[client] class RecordingDeleteRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends RecordingDeleteRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: RecordingDeleteRequest
  ): Either[RecordingDeleteRequestException, HttpRequest] = {
    createHttpRequestFor(
      s"""/${apiVersion.twilioString}/Accounts/${req.accountSid}/Recordings/${req.sid}.json""",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): RecordingDeleteRequestException.Api =
    RecordingDeleteRequestException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): RecordingDeleteRequestException.Unspecified =
    RecordingDeleteRequestException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: RecordingDeleteRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[RecordingDeleteRequestException, Unit] = {
    httpResponse.status match {
      case StatusCodes.NoContent => Right(())
      case StatusCodes.NotFound  => buildResultForNotFoundResponse(req, entity)
      case _ => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
    }
  }

  private def buildResultForNotFoundResponse(
      req: RecordingDeleteRequest,
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => RecordingDeleteRequestException.Unspecified(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(
              RecordingDeleteRequestException.RecordingNotFound(req.accountSid, req.sid)
            )
          case other =>
            Left(
              RecordingDeleteRequestException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represent. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}
