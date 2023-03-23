package com.dixa.twilio.client.impl.voice

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{
  ContentTypes,
  HttpEntity,
  HttpMethod,
  HttpMethods,
  HttpRequest,
  HttpResponse,
  StatusCodes
}
import akka.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, DefaultApiErrorEntityJsonRep, HttpEntityString}
import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
import com.dixa.twilio.client.voice.CallUpdateRequestExecutor.{
  CallUpdateException,
  CallUpdateRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Call
import io.circe.generic.auto._
import org.scalactic.TypeCheckedTripleEquals._

import scala.collection.mutable
import scala.concurrent.ExecutionContext

private[client] class CallUpdateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends CallUpdateRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: CallUpdateRequestExecutor.CallUpdateRequest
  ): Either[CallUpdateException, HttpRequest] = {
    val postParamBuilder = new mutable.StringBuilder()
    req.twiml.foreach(twiml => postParamBuilder.append(s"Twiml=${twiml.xmlCompact}&"))
    req.url.foreach(url => postParamBuilder.append(s"Url=$url&"))
    val postParamLastCharIndex = postParamBuilder.length - 1
    if (postParamBuilder.charAt(postParamLastCharIndex) === '&')
      postParamBuilder.deleteCharAt(postParamLastCharIndex)
    val postParam = postParamBuilder.toString()

    createHttpRequestFor(
      s"/2010-04-01/Accounts/${req.accountSid}/Calls/${req.sid}.json",
      connSettings
    ).map(_.withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, postParam)))
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
  ): Either[CallUpdateException, Call] = httpResponse.status match {
    case StatusCodes.OK       => parseEntityAs[CallJsonRep](entity).map(_.toModel)
    case StatusCodes.NotFound => buildResultForNotFoundResponse(req, entity)
    case _                    => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
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
