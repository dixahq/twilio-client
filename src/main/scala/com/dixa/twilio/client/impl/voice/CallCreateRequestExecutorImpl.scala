package com.dixa.twilio.client.impl.voice

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpMethod, HttpMethods, HttpRequest, HttpResponse}
import akka.stream.Materializer
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString}
import com.dixa.twilio.client.voice.CallCreateRequestExecutor
import com.dixa.twilio.model.voice.Call

import scala.concurrent.ExecutionContext

private[client] class CallCreateRequestExecutorImpl()(
  implicit override protected val http: HttpExt,
  override protected val materializer: Materializer,
  override protected val executionContext: ExecutionContext,
  apiVersion: ApiVersion
) extends CallCreateRequestExecutor {

//  import CallCreateRequestExecutorImpl._

//  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api
//
//  override protected def method: HttpMethod = HttpMethods.POST
//
//  override protected def createHttpReq(connSettings: TwilioConnectionSettings, req: CallUpdateRequestExecutor.CallUpdateRequest): Either[CallUpdateRequestExecutor.CallUpdateException, HttpRequest] = ???
//
//    override protected def parseHttpResponse(request: CallUpdateRequestExecutor.CallUpdateRequest, httpRequest: HttpRequest, httpResponse: HttpResponse, entity: HttpEntityString): Either[CallUpdateRequestExecutor.CallUpdateException, Call] = ???
//
//  override protected def mapApiException(apiException: ApiException): CallUpdateException.Api = ???
//
//  override protected def createUnspecifiedException(msg: Option[String], cause: Option[Throwable]): CallUpdateException.Unspecified = ???
}
