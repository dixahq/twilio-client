package com.dixa.twilio.client.impl.voice

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
import com.dixa.twilio.client.voice.CallUpdateRequestExecutor.{
  CallUpdateException,
  CallUpdateRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Call

import scala.concurrent.ExecutionContext

private[client] class CallUpdateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends CallUpdateRequestExecutor {

  import CallUpdateRequestExecutorImpl._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: CallUpdateRequestExecutor.CallUpdateRequest
  ): Either[CallUpdateException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withOptionalParam(urlParamKey, req.url)
      .withOptionalParam(methodParamKey, req.method)
      .withOptionalParam(statusParamKey, req.status)
      .withOptionalParam(fallbackUrlParamKey, req.fallbackUrl)
      .withOptionalParam(fallbackMethodParamKey, req.fallbackMethod)
      .withOptionalParam(statusCallbackParamKey, req.statusCallback)
      .withOptionalParam(statusCallbackMethodParamKey, req.statusCallbackMethod)
      .withOptionalParam(timeLimitParamKey, req.timeLimit)

    val paramsWithTwilio =
      req.twiml
        .map(t => params.withParam(twimlParamKey, t.xmlCompact))
        .getOrElse(params)
        .buildForPostParams

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Calls/${req.sid}.json",
      connSettings
    ).map(
      _.withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, paramsWithTwilio))
    )
  }

  override protected def mapApiException(apiException: ApiException): CallUpdateException.Api =
    CallUpdateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): CallUpdateException.Unspecified = CallUpdateException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: CallUpdateRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[CallUpdateException, Call] = {
    httpResponse.status match {
      case StatusCodes.OK         => parseEntityAs[CallJsonRep](entity).map(_.toModel)
      case StatusCodes.NotFound   => buildResultForNotFoundResponse(req, entity)
      case StatusCodes.BadRequest => buildResultForBadRequest(req, entity)
      case _ => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
    }
  }

  private def buildResultForBadRequest(
      req: CallUpdateRequestExecutor.CallUpdateRequest,
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => CallUpdateException.Unspecified(e))
      .flatMap { decoded =>
        decoded.code match {
          case 21220L =>
            Left(CallUpdateException.RedirectNotAllowedIllegalCallState(req.accountSid, req.sid))
          case other =>
            Left(
              CallUpdateException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represent. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }

  private def buildResultForNotFoundResponse(req: CallUpdateRequest, entity: HttpEntityString) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => CallUpdateException.Unspecified(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(CallUpdateException.CallNotFound(req.accountSid, req.sid))
          case other =>
            Left(
              CallUpdateException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represent. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}

private object CallUpdateRequestExecutorImpl {
  private val urlParamKey                  = "Url"
  private val methodParamKey               = "Method"
  private val statusParamKey               = "Status"
  private val fallbackUrlParamKey          = "FallbackUrl"
  private val fallbackMethodParamKey       = "FallbackMethod"
  private val statusCallbackParamKey       = "StatusCallback"
  private val statusCallbackMethodParamKey = "StatusCallbackMethod"
  private val twimlParamKey                = "Twiml"
  private val timeLimitParamKey            = "TimeLimit"

}
