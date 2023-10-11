package com.dixa.twilio.client.impl.voice

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.impl.{
  ApiSubDomain,
  ApiVersion,
  DefaultApiErrorEntityJsonRep,
  HttpEntityString
}
import com.dixa.twilio.client.voice.CallFetchRequestExecutor
import com.dixa.twilio.client.voice.CallFetchRequestExecutor.{CallFetchException, CallFetchRequest}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Call

import scala.concurrent.ExecutionContext

private[client] class CallFetchRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends CallFetchRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: CallFetchRequestExecutor.CallFetchRequest
  ): Either[CallFetchException, HttpRequest] = {
    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Calls/${req.sid}.json",
      connSettings
    )
  }

  override protected def mapApiException(apiException: ApiException): CallFetchException.Api =
    CallFetchException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): CallFetchException.Unspecified = CallFetchException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: CallFetchRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[CallFetchException, Call] = httpResponse.status match {
    case StatusCodes.OK       => parseEntityAs[CallJsonRep](entity).map(_.toModel)
    case StatusCodes.NotFound => buildResultForNotFoundResponse(req, entity)
    case _                    => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
  }

  private def buildResultForNotFoundResponse(req: CallFetchRequest, entity: HttpEntityString) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => CallFetchException.Unspecified(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(CallFetchException.CallNotFound(req.accountSid, req.sid))
          case other =>
            Left(
              CallFetchException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represents. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}
