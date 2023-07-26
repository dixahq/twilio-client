package com.dixa.twilio.client.impl.voice

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.impl.{
  ApiSubDomain,
  ApiVersion,
  DefaultApiErrorEntityJsonRep,
  HttpEntityString,
  QueryParamBuilder
}
import com.dixa.twilio.client.voice.CallRecordingUpdateRequestExecutor
import com.dixa.twilio.client.voice.CallRecordingUpdateRequestExecutor.{
  CallRecordingUpdateException,
  CallRecordingUpdateRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Recording

import scala.concurrent.ExecutionContext

private[client] class CallRecordingUpdateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends CallRecordingUpdateRequestExecutor {

  import CallRecordingUpdateRequestExecutorImpl._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: CallRecordingUpdateRequest
  ): Either[CallRecordingUpdateException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withParam(statusParamKey, req.status)
      .withOptionalParam(pauseBehaviorParamKey, req.pauseBehavior)
      .buildForPostParams

    createHttpRequestFor(
      s"""/${apiVersion.twilioString}/Accounts/${req.accountSid}/Calls/${req.callSid}/Recordings/${req.sid
          .getOrElse("Twilio.CURRENT")}.json""",
      connSettings
    ).map(_.withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params)))
  }

  override protected def mapApiException(
      apiException: ApiException
  ): CallRecordingUpdateException.Api =
    CallRecordingUpdateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): CallRecordingUpdateException.Unspecified = CallRecordingUpdateException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: CallRecordingUpdateRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[CallRecordingUpdateException, Recording] = httpResponse.status match {
    case StatusCodes.OK       => parseEntityAs[RecordingJsonRep](entity).map(_.toModel)
    case StatusCodes.NotFound => buildResultForNotFoundResponse(req, entity)
    case _                    => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
  }

  private def buildResultForNotFoundResponse(
      req: CallRecordingUpdateRequest,
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => {
        println(e.getMessage)
        println(e.getCause.getMessage)
        CallRecordingUpdateException.Unspecified(e)
      })
      .flatMap { decoded =>
        println(decoded)
        decoded.code match {
          case 20404L =>
            Left(
              CallRecordingUpdateException.RecordingNotFound(req.accountSid, req.sid, req.callSid)
            )
          case other =>
            Left(
              CallRecordingUpdateException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represent. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}

private object CallRecordingUpdateRequestExecutorImpl {
  private val statusParamKey        = "Status"
  private val pauseBehaviorParamKey = "PauseBehavior"
}
