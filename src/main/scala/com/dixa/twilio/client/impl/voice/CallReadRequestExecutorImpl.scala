package com.dixa.twilio.client.impl.voice

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.voice.CallReadRequestExecutor
import com.dixa.twilio.client.voice.CallReadRequestExecutor.CallReadException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Call

import scala.concurrent.ExecutionContext

class CallReadRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends CallReadRequestExecutor {

  import CallReadRequestExecutorImpl._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: CallReadRequestExecutor.CallReadRequest
  ): Either[
    CallReadRequestExecutor.CallReadException,
    HttpRequest
  ] = {
    val params = QueryParamBuilder.empty
      .withOptionalParam(toParamKey, req.to)
      .withOptionalParam(fromParamKey, req.from)
      .withOptionalParam(parentCallSidParamKey, req.parentCallSid)
      .withOptionalParam(statusParamKey, req.status)
      .withOptionalDateParam(startTimeParamKey, req.startTimeAfter)
      .withOptionalDateParam(startTimeParamKey, req.startTimeBefore)
      .withOptionalDateParam(endTimeParamKey, req.endTimeAfter)
      .withOptionalDateParam(endTimeParamKey, req.endTimeBefore)
      .build

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Calls.json$params",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): CallReadException.Api =
    CallReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): CallReadException.Unspecified =
    CallReadException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: CallReadRequestExecutor.CallReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[
    CallReadRequestExecutor.CallReadException,
    Call
  ]] = {
    responseEntity.parse[CallListJsonRep]() match {
      case Left(ex) =>
        List(
          Left(
            CallReadException.Unspecified(
              Some(ex.cause.getMessage),
              Some(ex.cause)
            )
          )
        )
      case Right(listJsonRep) => listJsonRep.calls.map { _.toModel }.map { Right(_) }
    }

  }

}

private object CallReadRequestExecutorImpl {
  private val toParamKey            = "To"
  private val fromParamKey          = "From"
  private val parentCallSidParamKey = "ParentCallSid"
  private val statusParamKey        = "Status"
  private val startTimeParamKey     = "StartTime"
  private val endTimeParamKey       = "EndTime"
}
