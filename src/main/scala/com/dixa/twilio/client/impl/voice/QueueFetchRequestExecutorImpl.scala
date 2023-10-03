package com.dixa.twilio.client.impl.voice

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.voice.QueueFetchRequestExecutor
import com.dixa.twilio.client.voice.QueueFetchRequestExecutor.{
  QueueFetchException,
  QueueFetchRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Queue

import scala.concurrent.ExecutionContext

private[client] class QueueFetchRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends QueueFetchRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: QueueFetchRequestExecutor.QueueFetchRequest
  ): Either[QueueFetchException, HttpRequest] = {
    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Queues/${req.sid}.json",
      connSettings
    )
  }

  override protected def mapApiException(apiException: ApiException): QueueFetchException.Api =
    QueueFetchException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): QueueFetchException.Unspecified = QueueFetchException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: QueueFetchRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[QueueFetchException, Queue] = httpResponse.status match {
    case StatusCodes.OK       => parseEntityAs[QueueJsonRep](entity).map(_.toModel)
    case StatusCodes.NotFound => buildResultForNotFoundResponse(req, entity)
    case _                    => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
  }

  private def buildResultForNotFoundResponse(req: QueueFetchRequest, entity: HttpEntityString) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => QueueFetchException.Unspecified(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(QueueFetchException.QueueNotFound(req.accountSid, req.sid))
          case other =>
            Left(
              QueueFetchException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represents. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}
