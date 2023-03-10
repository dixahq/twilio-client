package com.dixa.twilio.client.impl.voice

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.impl.{
  ApiSubDomain,
  DefaultApiErrorEntityJsonRep,
  HttpEntityString,
  QueryParamBuilder
}
import com.dixa.twilio.client.voice.QueueUpdateRequestExecutor
import com.dixa.twilio.client.voice.QueueUpdateRequestExecutor.{
  QueueUpdateException,
  QueueUpdateRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Queue
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

private[client] class QueueUpdateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends QueueUpdateRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: QueueUpdateRequestExecutor.QueueUpdateRequest
  ): Either[QueueUpdateException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withParam("AccountSid", req.accountSid)
      .withParam("Sid", req.sid.toString)
      .withOptionalParam("FriendlyName", req.friendlyName)
      .withOptionalParam("MaxSize", req.maxSize)
      .buildForPostParams

    createHttpRequestFor(
      s"/2010-04-01/Accounts/${req.accountSid}/Queues/${req.sid}.json",
      connSettings
    ).map(_.withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params)))
  }

  override protected def mapApiException(apiException: ApiException): QueueUpdateException.Api =
    QueueUpdateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): QueueUpdateException.Unspecified = QueueUpdateException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: QueueUpdateRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[QueueUpdateException, Queue] = httpResponse.status match {
    case StatusCodes.OK       => parseEntityAs[QueueJsonRep](entity).map(_.toModel)
    case StatusCodes.NotFound => buildResultForNotFoundResponse(req, entity)
    case _                    => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
  }

  private def buildResultForNotFoundResponse(req: QueueUpdateRequest, entity: HttpEntityString) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => QueueUpdateException.Unspecified(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(QueueUpdateException.QueueNotFound(req.accountSid, req.sid))
          case other =>
            Left(
              QueueUpdateException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represent. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}
